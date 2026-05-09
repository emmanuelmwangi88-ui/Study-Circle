package com.deepseek.studycircle.data

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.studycircle.models.*
import com.deepseek.studycircle.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.agora.CallBack
import io.agora.MessageListener
import io.agora.chat.ChatClient
import io.agora.chat.ChatMessage as AgoraMessage
import io.agora.chat.TextMessageBody
import io.agora.chat.Conversation as AgoraConversation
import io.agora.chat.GroupOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class UserViewModel : ViewModel() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    // --- CLOUDINARY CONFIGURATION ---
    private val cloudinaryCloudName = "dnt3lcyoj"
    private val cloudinaryUploadPreset = "studycircle"
    // --------------------------------

    private val _userData = mutableStateOf<User?>(null)
    val userData: State<User?> = _userData

    private val _userTransactions = mutableStateListOf<CreditTransaction>()
    val userTransactions: List<CreditTransaction> = _userTransactions

    private val _resourceReviews = mutableStateListOf<Review>()
    val resourceReviews: List<Review> = _resourceReviews

    private val _whiteboardAnswers = mutableStateListOf<WhiteboardAnswer>()
    val whiteboardAnswers: List<WhiteboardAnswer> = _whiteboardAnswers

    private val _whiteboardQuestionUrl = mutableStateOf<String?>(null)
    val whiteboardQuestionUrl: State<String?> = _whiteboardQuestionUrl

    private val _whiteboardExpiresAt = mutableStateOf<Long?>(null)
    val whiteboardExpiresAt: State<Long?> = _whiteboardExpiresAt

    val allMaterials = mutableStateListOf<UploadMaterial>()
    val allSessions = mutableStateListOf<Session>()

    private val _allStudyGroups = MutableStateFlow<List<StudyGroup>>(emptyList())
    val allStudyGroups: StateFlow<List<StudyGroup>> = _allStudyGroups

    private val _userStudyGroups = MutableStateFlow<List<StudyGroup>>(emptyList())
    val userStudyGroups: StateFlow<List<StudyGroup>> = _userStudyGroups

    private val _groupMessages = mutableStateMapOf<String, List<ChatMessage>>()
    val groupMessages: Map<String, List<ChatMessage>> = _groupMessages

    private val chatListeners = mutableMapOf<String, Pair<DatabaseReference, ValueEventListener>>()

    private var sessionStartTime: Long = System.currentTimeMillis()
    private var lastUid: String? = null
    private var sessionCheckId: String? = null
    private var lastCheckedBonusSession: String? = null

    private var userListener: Pair<DatabaseReference, ValueEventListener>? = null
    private var transListener: Pair<Query, ValueEventListener>? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid != lastUid) {
            handleUserSwitch(currentUid)
        }
    }

    private val agoraMessageListener = object : MessageListener {
        override fun onMessageReceived(messages: List<AgoraMessage>) {
            messages.forEach { msg ->
                if (msg.chatType == AgoraMessage.ChatType.GroupChat) {
                    val groupId = msg.to
                    val chatMsg = mapAgoraMessageToChatMessage(msg)
                    val currentList = _groupMessages[groupId]?.toMutableList() ?: mutableListOf()
                    currentList.add(chatMsg)
                    _groupMessages[groupId] = currentList.distinctBy { it.id }.sortedBy { it.timestamp }
                }
            }
        }
        override fun onCmdMessageReceived(messages: List<AgoraMessage>) {}
        override fun onMessageRead(messages: List<AgoraMessage>) {}
        override fun onMessageDelivered(messages: List<AgoraMessage>) {}
        @Suppress("DEPRECATION")
        override fun onMessageRecalled(messages: List<AgoraMessage>) {}
        override fun onMessageChanged(message: AgoraMessage, change: Any?) {}
    }

    init {
        auth.addAuthStateListener(authStateListener)
        startGlobalListeners()
        startStudyTimeTracker()
        startWhiteboardExpirationWatcher()
        ChatClient.getInstance().chatManager().addMessageListener(agoraMessageListener)
        
        auth.currentUser?.uid?.let { 
            if (lastUid == null) handleUserSwitch(it)
        }
    }

    private fun handleUserSwitch(newUid: String?) {
        clearUserSpecificData()
        lastUid = newUid
        
        if (newUid != null) {
            sessionCheckId = UUID.randomUUID().toString()
            lastCheckedBonusSession = null
            startUserSpecificListeners(newUid)
            ensureAgoraLoggedIn(newUid)
        }
    }

    private fun ensureAgoraLoggedIn(uid: String) {
        if (!ChatClient.getInstance().isLoggedIn) {
            @Suppress("DEPRECATION")
            ChatClient.getInstance().login(uid, uid, object : CallBack {
                override fun onSuccess() {
                    ChatClient.getInstance().chatManager().loadAllConversations()
                    Log.d("AgoraChat", "Auto-login successful")
                }
                override fun onError(code: Int, error: String?) {
                    Log.e("AgoraChat", "Auto-login failed: $error")
                }
                override fun onProgress(progress: Int, status: String?) {}
            })
        }
    }

    private fun startUserSpecificListeners(uid: String) {
        val userRef = database.child("users").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val user = snapshot.getValue(User::class.java)
                    _userData.value = user
                    if (user != null) {
                        checkAndAwardBonuses(uid, user)
                        updateUserStudyGroups(user.joinedGroups)
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Error parsing user data: ${e.message}")
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        userRef.addValueEventListener(listener)
        userListener = userRef to listener

        val transQuery = database.child("transactions").orderByChild("userId").equalTo(uid)
        val tListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _userTransactions.clear()
                val list = mutableListOf<CreditTransaction>()
                for (data in snapshot.children) {
                    try {
                        data.getValue(CreditTransaction::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Trans mapping error: ${e.message}")
                    }
                }
                _userTransactions.addAll(list.sortedByDescending { it.timestamp })
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        transQuery.addValueEventListener(tListener)
        transListener = transQuery to tListener
    }

    private fun startGlobalListeners() {
        database.child("materials").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allMaterials.clear()
                snapshot.children.forEach { data ->
                    try {
                        data.getValue(UploadMaterial::class.java)?.let { allMaterials.add(it) }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Material mapping error: ${e.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("sessions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allSessions.clear()
                snapshot.children.forEach { data ->
                    try {
                        data.getValue(Session::class.java)?.let { allSessions.add(it) }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Session mapping error: ${e.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("whiteboard_answers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _whiteboardAnswers.clear()
                snapshot.children.forEach { data ->
                    try {
                        data.getValue(WhiteboardAnswer::class.java)?.let { answer ->
                            _whiteboardAnswers.add(answer)
                        }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Whiteboard mapping error: ${e.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("whiteboard_question").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)
                val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java) ?: 0L
                val currentTime = System.currentTimeMillis()
                
                if (imageUrl != null) {
                    if (expiresAt > currentTime) {
                        _whiteboardQuestionUrl.value = imageUrl
                        _whiteboardExpiresAt.value = expiresAt
                    } else {
                        clearWhiteboardContent()
                    }
                } else {
                    _whiteboardQuestionUrl.value = null
                    _whiteboardExpiresAt.value = null
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("study_groups").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groups = snapshot.children.mapNotNull { data ->
                    try {
                        data.getValue(StudyGroup::class.java)
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "StudyGroup mapping error for ${data.key}: ${e.message}")
                        null
                    }
                }
                _allStudyGroups.value = groups
                _userData.value?.let { updateUserStudyGroups(it.joinedGroups) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun startWhiteboardExpirationWatcher() {
        viewModelScope.launch {
            while (true) {
                val expiresAt = _whiteboardExpiresAt.value
                if (expiresAt != null && System.currentTimeMillis() >= expiresAt) {
                    clearWhiteboardContent()
                }
                delay(10000) // Check every 10 seconds
            }
        }
    }

    private fun updateUserStudyGroups(joinedGroups: Map<String, Boolean>?) {
        val joinedGroupIds = joinedGroups?.keys ?: emptySet()
        _userStudyGroups.value = _allStudyGroups.value.filter { it.id.toString() in joinedGroupIds }
        
        joinedGroupIds.forEach { groupId ->
            if (!chatListeners.containsKey(groupId)) {
                startChatListener(groupId)
            }
        }
    }

    private fun startChatListener(groupId: String) {
        // We load local history from Agora
        val conversation = ChatClient.getInstance().chatManager().getConversation(groupId, AgoraConversation.ConversationType.GroupChat, true)
        val agoraMessages = conversation.allMessages
        _groupMessages[groupId] = agoraMessages.map { mapAgoraMessageToChatMessage(it) }.sortedBy { it.timestamp }

        // Also keep Firebase listener as backup or for specific metadata
        val chatRef = database.child("group_chats").child(groupId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fbMessages = snapshot.children.mapNotNull { data ->
                    try { data.getValue(ChatMessage::class.java) } catch (_: Exception) { null }
                }
                val currentAgora = _groupMessages[groupId] ?: emptyList()
                _groupMessages[groupId] = (currentAgora + fbMessages).distinctBy { it.id }.sortedBy { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        chatRef.addValueEventListener(listener)
        chatListeners[groupId] = chatRef to listener
    }

    private fun mapAgoraMessageToChatMessage(msg: AgoraMessage): ChatMessage {
        val body = msg.body as? TextMessageBody
        return ChatMessage(
            id = msg.msgId,
            senderId = msg.from,
            senderName = msg.getStringAttribute("senderName", "User"),
            senderImage = msg.getStringAttribute("senderImage", ""),
            text = body?.message ?: "",
            timestamp = msg.msgTime,
            fileUrl = msg.getStringAttribute("fileUrl", null),
            fileType = msg.getStringAttribute("fileType", null)
        )
    }

    private fun checkAndAwardBonuses(uid: String, user: User) {
        if (lastCheckedBonusSession == sessionCheckId) return
        lastCheckedBonusSession = sessionCheckId

        val currentTime = System.currentTimeMillis()
        var bonusAmount: Long
        var bonusType: CreditCalculator.TransactionType

        if (user.isFirstLogin) {
            database.child("transactions")
                .orderByChild("userId")
                .equalTo(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val hasReceivedSignupBonus = snapshot.children.any {
                            it.getValue(CreditTransaction::class.java)?.type == CreditCalculator.TransactionType.SIGNUP_BONUS.name
                        }

                        if (!hasReceivedSignupBonus) {
                            bonusAmount = CreditCalculator.WELCOME_BONUS
                            bonusType = CreditCalculator.TransactionType.SIGNUP_BONUS
                            
                            val updates = mutableMapOf<String, Any>(
                                "credits" to ServerValue.increment(bonusAmount),
                                "isFirstLogin" to false,
                                "lastLogin" to currentTime
                            )
                            
                            // Award Welcome Badge
                            val currentBadges = user.badges?.toMutableList() ?: mutableListOf()
                            if (!currentBadges.contains("learner")) {
                                currentBadges.add("learner")
                                updates["badges"] = currentBadges
                            }

                            database.child("users").child(uid).updateChildren(updates).addOnSuccessListener {
                                recordTransaction(uid, bonusAmount, bonusType)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            val oneDayMillis = 24 * 60 * 60 * 1000
            if (user.lastLogin == 0L || currentTime - user.lastLogin > oneDayMillis) {
                bonusAmount = CreditCalculator.DAILY_LOGIN_BONUS
                bonusType = CreditCalculator.TransactionType.DAILY_LOGIN
                
                val updates = mapOf(
                    "credits" to ServerValue.increment(bonusAmount),
                    "lastLogin" to currentTime
                )
                database.child("users").child(uid).updateChildren(updates).addOnSuccessListener {
                    recordTransaction(uid, bonusAmount, bonusType)
                }
            }
        }
    }

    private fun recordTransaction(userId: String, amount: Long, type: CreditCalculator.TransactionType) {
        val transId = database.child("transactions").push().key ?: UUID.randomUUID().toString()
        val transaction = CreditTransaction(
            id = transId,
            userId = userId,
            amount = amount,
            type = type.name,
            timestamp = System.currentTimeMillis(),
            description = type.description
        )
        database.child("transactions").child(transId).setValue(transaction)
    }

    private fun clearUserSpecificData() {
        userListener?.let { (ref, listener) -> ref.removeEventListener(listener) }
        transListener?.let { (query, listener) -> query.removeEventListener(listener) }
        chatListeners.values.forEach { (ref, listener) -> ref.removeEventListener(listener) }
        
        userListener = null
        transListener = null
        chatListeners.clear()
        
        _userData.value = null
        _userTransactions.clear()
        _resourceReviews.clear()
        _userStudyGroups.value = emptyList()
        _groupMessages.clear()
        sessionCheckId = null
        lastCheckedBonusSession = null
    }

    fun postWhiteboardAnswer(text: String) {
        val user = _userData.value ?: return
        val answerId = database.child("whiteboard_answers").push().key ?: UUID.randomUUID().toString()
        val answer = WhiteboardAnswer(
            id = answerId,
            userName = user.name,
            userId = user.uid,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        database.child("whiteboard_answers").child(answerId).setValue(answer)
    }

    fun setWhiteboardQuestion(imageUrl: String) {
        // Countdown starts when image is put: 60 minutes from now
        val expiresAt = System.currentTimeMillis() + (60 * 60 * 1000)
        val data = mapOf(
            "imageUrl" to imageUrl,
            "timestamp" to ServerValue.TIMESTAMP,
            "expiresAt" to expiresAt
        )
        database.child("whiteboard_question").setValue(data)
    }

    fun clearWhiteboardContent() {
        database.child("whiteboard_question").removeValue()
        database.child("whiteboard_answers").removeValue()
        _whiteboardQuestionUrl.value = null
        _whiteboardExpiresAt.value = null
        _whiteboardAnswers.clear()
    }

    private fun startStudyTimeTracker() {
        viewModelScope.launch {
            while (true) {
                delay(60000) 
                updateStudyTime()
            }
        }
    }

    private fun updateStudyTime() {
        val uid = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()
        val sessionDuration = now - sessionStartTime
        sessionStartTime = now

        database.child("users").child(uid).child("studyTimeMillis").setValue(ServerValue.increment(sessionDuration))
    }

    fun saveMaterial(title: String, category: String, description: String, url: String, fileType: String, onComplete: (Boolean) -> Unit) {
        val user = _userData.value ?: run {
            onComplete(false)
            return
        }
        val materialId = database.child("materials").push().key ?: UUID.randomUUID().toString()
        val material = UploadMaterial(
            id = materialId,
            title = title,
            author = user.name,
            authorId = user.uid,
            authorImage = user.imageUri,
            category = category,
            description = description,
            fileUrl = url,
            fileType = fileType.uppercase(),
            cost = CreditCalculator.UPLOAD_REWARD,
            timestamp = System.currentTimeMillis()
        )
        database.child("materials").child(materialId).setValue(material).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Award Contributor Badge if not already owned
                val currentBadges = user.badges?.toMutableList() ?: mutableListOf()
                if (!currentBadges.contains("contributor")) {
                    currentBadges.add("contributor")
                    database.child("users").child(user.uid).child("badges").setValue(currentBadges)
                }
            }
            onComplete(task.isSuccessful)
        }
    }

    /**
     * Uploads a file to Cloudinary.
     * Returns: Triple(URL, FileType, ErrorMessage)
     */
    fun uploadFileToCloudinary(context: Context, fileUri: Uri, onComplete: (String?, String?, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"
            
            val file = getFileFromUri(context, fileUri, extension)
            if (file == null || !file.exists()) {
                withContext(Dispatchers.Main) { onComplete(null, null, "Could not read local file") }
                return@launch
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", "upload.$extension", requestFile)
            val uploadPreset = cloudinaryUploadPreset.toRequestBody("text/plain".toMediaTypeOrNull())

            try {
                val response = RetrofitClient.cloudinaryApi.uploadFile(cloudinaryCloudName, body, uploadPreset)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        onComplete(response.body()?.secureUrl, extension.uppercase(), null)
                    } else {
                        onComplete(null, null, "Upload failed")
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onComplete(null, null, "Network error") }
            } finally {
                if (file.exists()) file.delete()
            }
        }
    }

    fun performTransaction(type: CreditCalculator.TransactionType, customAmount: Long? = null, description: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val user = _userData.value ?: return
        
        val amount = customAmount ?: CreditCalculator.getAmountForType(type)
        
        if (user.credits + amount < 0) {
            onComplete(false)
            return
        }

        val transactionId = database.child("transactions").push().key ?: UUID.randomUUID().toString()
        val transaction = CreditTransaction(
            id = transactionId,
            userId = uid,
            amount = amount,
            type = type.name,
            description = description,
            timestamp = System.currentTimeMillis()
        )

        database.child("users").child(uid).child("credits").setValue(ServerValue.increment(amount)).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                database.child("transactions").child(transactionId).setValue(transaction).addOnCompleteListener {
                    onComplete(it.isSuccessful)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun toggleBookmark(resourceId: String, isBookmarked: Boolean, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        database.child("users").child(uid).child("bookmarks").child(resourceId).setValue(if (isBookmarked) true else null)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun createSession(title: String, topic: String, onComplete: (Session?) -> Unit) {
        val user = _userData.value ?: return
        val sessionId = UUID.randomUUID().toString()
        val session = Session(
            id = sessionId,
            title = title,
            topic = topic,
            student = user.name,
            creatorId = user.uid,
            dateTime = "Just now",
            isLive = true
        )
        database.child("sessions").child(sessionId).setValue(session).addOnCompleteListener {
            if (it.isSuccessful) onComplete(session) else onComplete(null)
        }
    }

    fun deleteSession(sessionId: String, onComplete: (Boolean) -> Unit) {
        database.child("sessions").child(sessionId).removeValue().addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    fun updateUserProfile(name: String, bio: String, imageUri: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "name" to name,
            "bio" to bio,
            "imageUri" to imageUri
        )
        database.child("users").child(uid).updateChildren(updates).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri, extension: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_file_${System.currentTimeMillis()}.$extension")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (_: Exception) {
            null
        }
    }

    fun createStudyGroup(name: String, description: String, category: String) {
        val user = _userData.value ?: return
        val safeUid = user.uid.replace(".", "_")
        val groupId = database.child("study_groups").push().key ?: return
        val group = StudyGroup(
            id = groupId,
            name = name,
            description = description,
            category = category,
            createdBy = user.uid,
            members = mapOf(safeUid to true)
        )
        database.child("study_groups").child(groupId).setValue(group)
        joinStudyGroup(groupId)
        
        // Register group in Agora Chat
        Thread {
            try {
                ChatClient.getInstance().groupManager().createGroup(name, description, arrayOf(user.uid), "Study group", GroupOptions())
            } catch (_: Exception) {}
        }.start()
    }

    fun joinStudyGroup(groupId: String) {
        val uid = auth.currentUser?.uid ?: return
        val safeUid = uid.replace(".", "_")
        database.child("study_groups").child(groupId).child("members").child(safeUid).setValue(true)
        database.child("users").child(uid).child("joinedGroups").child(groupId).setValue(true)
        
        // Join in Agora
        Thread {
            try { ChatClient.getInstance().groupManager().joinGroup(groupId) } catch (_: Exception) {}
        }.start()
    }

    fun getGroupById(groupId: String): Flow<StudyGroup?> {
        return allStudyGroups.map { groups ->
            groups.find { it.id.toString() == groupId }
        }
    }

    fun sendChatMessage(groupId: String, text: String, fileUrl: String? = null, fileType: String? = null) {
        val user = _userData.value ?: return
        
        // 1. Send via Agora Chat
        @Suppress("DEPRECATION")
        val message = AgoraMessage.createTxtSendMessage(text, groupId)
        message.chatType = AgoraMessage.ChatType.GroupChat
        message.setAttribute("senderName", user.name)
        message.setAttribute("senderImage", user.imageUri)
        fileUrl?.let { message.setAttribute("fileUrl", it) }
        fileType?.let { message.setAttribute("fileType", it) }
        
        ChatClient.getInstance().chatManager().sendMessage(message)

        // 2. Also save to Firebase as backup/history
        val messageId = message.msgId
        val chatMessage = ChatMessage(
            id = messageId,
            senderId = user.uid,
            senderName = user.name,
            senderImage = user.imageUri,
            text = text,
            timestamp = System.currentTimeMillis(),
            fileUrl = fileUrl,
            fileType = fileType
        )
        database.child("group_chats").child(groupId).child(messageId).setValue(chatMessage)
        
        // Update local state immediately for better UX
        val currentList = _groupMessages[groupId]?.toMutableList() ?: mutableListOf()
        currentList.add(chatMessage)
        _groupMessages[groupId] = currentList.distinctBy { it.id }.sortedBy { it.timestamp }
    }

    fun fetchReviews(resourceId: String) {
        database.child("reviews").child(resourceId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _resourceReviews.clear()
                snapshot.children.forEach { data ->
                    try {
                        data.getValue(Review::class.java)?.let { _resourceReviews.add(it) }
                    } catch (e: Exception) {
                        Log.e("UserViewModel", "Review mapping error: ${e.message}")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun submitReview(resourceId: String, rating: Float, text: String, onComplete: (Boolean) -> Unit) {
        val user = _userData.value ?: return
        val reviewId = database.child("reviews").child(resourceId).push().key ?: return
        val review = Review(
            id = reviewId,
            userId = user.uid,
            user = user.name,
            userImage = user.imageUri,
            rating = rating.toDouble(),
            text = text,
            date = "Just now"
        )
        database.child("reviews").child(resourceId).child(reviewId).setValue(review).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        ChatClient.getInstance().chatManager().removeMessageListener(agoraMessageListener)
        clearUserSpecificData()
    }
}
