package com.deepseek.studycircle.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.studycircle.models.CreditTransaction
import com.deepseek.studycircle.models.Review
import com.deepseek.studycircle.models.Session
import com.deepseek.studycircle.models.UploadMaterial
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.models.WhiteboardAnswer
import com.deepseek.studycircle.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val _userData = mutableStateOf<User?>(null)
    val userData: State<User?> = _userData

    private val _userTransactions = mutableStateListOf<CreditTransaction>()
    val userTransactions: List<CreditTransaction> = _userTransactions

    private val _resourceReviews = mutableStateListOf<Review>()
    val resourceReviews: List<Review> = _resourceReviews

    private val _whiteboardAnswers = mutableStateListOf<WhiteboardAnswer>()
    val whiteboardAnswers: List<WhiteboardAnswer> = _whiteboardAnswers

    val allMaterials = mutableStateListOf<UploadMaterial>()
    val allSessions = mutableStateListOf<Session>()

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

    init {
        auth.addAuthStateListener(authStateListener)
        startGlobalListeners()
        startStudyTimeTracker()
        
        // Initial check for existing user
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
                    data.getValue(CreditTransaction::class.java)?.let { list.add(it) }
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
                    data.getValue(UploadMaterial::class.java)?.let { allMaterials.add(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("sessions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allSessions.clear()
                snapshot.children.forEach { data ->
                    data.getValue(Session::class.java)?.let { allSessions.add(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        database.child("whiteboard_answers").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _whiteboardAnswers.clear()
                val twelveHoursAgo = System.currentTimeMillis() - (12 * 60 * 60 * 1000)
                snapshot.children.forEach { data ->
                    data.getValue(WhiteboardAnswer::class.java)?.let { answer ->
                        if (answer.timestamp > twelveHoursAgo) {
                            _whiteboardAnswers.add(answer)
                        } else {
                            data.ref.removeValue()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkAndAwardBonuses(uid: String, user: User) {
        if (lastCheckedBonusSession == sessionCheckId) return
        lastCheckedBonusSession = sessionCheckId

        val currentTime = System.currentTimeMillis()
        var bonusAmount: Long = 0
        var bonusType: CreditCalculator.TransactionType? = null

        if (user.isFirstLogin) {
            bonusAmount = CreditCalculator.WELCOME_BONUS
            bonusType = CreditCalculator.TransactionType.SIGNUP_BONUS
            
            val updates = mapOf(
                "credits" to ServerValue.increment(bonusAmount),
                "isFirstLogin" to false,
                "lastLogin" to currentTime
            )
            database.child("users").child(uid).updateChildren(updates).addOnSuccessListener {
                recordTransaction(uid, bonusAmount, bonusType!!)
            }
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
                    recordTransaction(uid, bonusAmount, bonusType!!)
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
        userListener = null
        transListener = null
        
        _userData.value = null
        _userTransactions.clear()
        _resourceReviews.clear()
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

    override fun onCleared() {
        super.onCleared()
        try {
            auth.removeAuthStateListener(authStateListener)
            updateStudyTime()
            clearUserSpecificData()
        } catch (e: Exception) {}
    }

    fun fetchReviews(resourceId: String) {
        database.child("reviews").child(resourceId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _resourceReviews.clear()
                snapshot.children.forEach { data ->
                    data.getValue(Review::class.java)?.let { _resourceReviews.add(it) }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun submitReview(context: Context, resourceId: String, rating: Float, text: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return
        val reviewId = database.child("reviews").child(resourceId).push().key ?: return
        val review = Review(
            id = 0,
            user = user.displayName ?: "Anonymous",
            rating = rating.toDouble(),
            text = text,
            date = "Just now"
        )
        database.child("reviews").child(resourceId).child(reviewId).setValue(review).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    fun saveMaterial(title: String, category: String, description: String, url: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return
        val materialId = UUID.randomUUID().toString()
        val material = UploadMaterial(
            id = materialId,
            title = title,
            author = _userData.value?.name ?: "Anonymous",
            authorId = user.uid,
            category = category,
            description = description,
            fileUrl = url,
            cost = CreditCalculator.UPLOAD_REWARD, // Or some default
            timestamp = System.currentTimeMillis()
        )
        database.child("materials").child(materialId).setValue(material).addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    fun uploadMaterial(title: String, category: String, cost: Long, fileUri: Uri, context: Context, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return

        uploadFileToCloudinary(context, fileUri) { url ->
            if (url != null) {
                val materialId = UUID.randomUUID().toString()
                val material = UploadMaterial(
                    id = materialId,
                    title = title,
                    author = _userData.value?.name ?: "Anonymous",
                    authorId = user.uid,
                    category = category,
                    cost = cost,
                    fileUrl = url,
                    timestamp = System.currentTimeMillis()
                )
                database.child("materials").child(materialId).setValue(material).addOnCompleteListener {
                    if (it.isSuccessful) {
                        performTransaction(
                            type = CreditCalculator.TransactionType.UPLOAD,
                            description = "Uploaded material: $title"
                        ) { }
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            } else {
                onComplete(false)
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

    fun toggleBookmark(resourceId: String, isBookmarked: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).child("bookmarks").child(resourceId).setValue(isBookmarked)
    }

    fun createSession(title: String, topic: String, onComplete: (Session?) -> Unit) {
        val user = auth.currentUser ?: return
        val sessionId = UUID.randomUUID().toString()
        val session = Session(
            id = sessionId,
            title = title,
            topic = topic,
            student = _userData.value?.name ?: "Anonymous",
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

    fun uploadFileToCloudinary(context: Context, fileUri: Uri, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = getFileFromUri(context, fileUri)
            if (file == null || !file.exists()) {
                withContext(Dispatchers.Main) { onComplete(null) }
                return@launch
            }

            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val uploadPreset = "profile".toRequestBody("text/plain".toMediaTypeOrNull())

            try {
                val response = RetrofitClient.cloudinaryApi.uploadFile("dnt3lcyoj", body, uploadPreset)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) onComplete(response.body()?.secureUrl) else onComplete(null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(null) }
            } finally {
                if (file.exists()) file.delete()
            }
        }
    }

    fun updateUserProfile(context: Context, name: String, bio: String, imageUri: String, onComplete: (Boolean) -> Unit) {
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

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_file_${System.currentTimeMillis()}")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
