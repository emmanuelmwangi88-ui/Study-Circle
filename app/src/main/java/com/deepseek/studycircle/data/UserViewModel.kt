package com.deepseek.studycircle.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.studycircle.models.CreditTransaction
import com.deepseek.studycircle.models.Review
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserViewModel : ViewModel() {
    private val mAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("Users")
    private val reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews")
    private val transactionsRef = FirebaseDatabase.getInstance().getReference("Transactions")

    private val _userData = mutableStateOf<User?>(null)
    val userData: State<User?> = _userData

    private val _resourceReviews = mutableStateListOf<Review>()
    val resourceReviews: List<Review> = _resourceReviews

    private val _userTransactions = mutableStateListOf<CreditTransaction>()
    val userTransactions: List<CreditTransaction> = _userTransactions

    init {
        fetchUserData()
        fetchUserTransactions()
    }

    fun fetchUserData() {
        val userId = mAuth.currentUser?.uid ?: return
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                _userData.value = user
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun fetchUserTransactions() {
        val userId = mAuth.currentUser?.uid ?: return
        transactionsRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _userTransactions.clear()
                for (transSnapshot in snapshot.children) {
                    val transaction = transSnapshot.getValue(CreditTransaction::class.java)
                    if (transaction != null) {
                        _userTransactions.add(transaction)
                    }
                }
                _userTransactions.sortByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun performTransaction(
        type: CreditCalculator.TransactionType,
        customAmount: Int? = null,
        description: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val userId = mAuth.currentUser?.uid ?: return
        val currentCredits = _userData.value?.credits ?: 0
        
        if (!CreditCalculator.canAfford(currentCredits, type, customAmount)) {
            onComplete(false)
            return
        }

        val amount = customAmount ?: CreditCalculator.getAmountForType(type)
        val newBalance = CreditCalculator.calculateNewBalance(currentCredits, type, customAmount)

        // Update credits
        database.child(userId).child("credits").setValue(newBalance)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Record transaction
                    val transId = transactionsRef.child(userId).push().key ?: System.currentTimeMillis().toString()
                    val transaction = CreditTransaction(
                        id = transId,
                        amount = amount,
                        type = type.name,
                        timestamp = System.currentTimeMillis(),
                        description = description ?: type.description
                    )
                    transactionsRef.child(userId).child(transId).setValue(transaction)
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
    }

    // Legacy method for backward compatibility
    fun addCredits(amount: Int, onComplete: (Boolean) -> Unit = {}) {
        performTransaction(CreditCalculator.TransactionType.OTHER, amount, "Credit Adjustment", onComplete)
    }

    fun toggleBookmark(resourceId: Int, isBookmarked: Boolean, onComplete: (Boolean) -> Unit = {}) {
        val userId = mAuth.currentUser?.uid ?: return
        database.child(userId).child("bookmarks").child(resourceId.toString()).setValue(isBookmarked)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun fetchReviews(resourceId: Int) {
        reviewsRef.child(resourceId.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _resourceReviews.clear()
                for (reviewSnapshot in snapshot.children) {
                    val review = reviewSnapshot.getValue(Review::class.java)
                    if (review != null) {
                        _resourceReviews.add(review)
                    }
                }
                _resourceReviews.reverse() // Newest first
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun submitReview(context: Context, resourceId: Int, rating: Float, reviewText: String, onComplete: (Boolean) -> Unit) {
        val reviewId = System.currentTimeMillis().toString()
        val userName = _userData.value?.name ?: "Anonymous"
        val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        
        val reviewData = Review(
            id = reviewId.hashCode(),
            user = userName,
            rating = rating.toDouble(),
            text = reviewText,
            date = currentDate
        )

        reviewsRef.child(resourceId.toString()).child(reviewId).setValue(reviewData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                }
                onComplete(task.isSuccessful)
            }
    }

    fun uploadFileToCloudinary(context: Context, fileUri: Uri, onComplete: (String?) -> Unit) {
        val file = uriToFile(context, fileUri) ?: return
        val requestFile = file.asRequestBody("*/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val uploadPreset = "studycircle_preset".toRequestBody("text/plain".toMediaTypeOrNull())

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.cloudinaryApi.uploadImage(body, uploadPreset)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        onComplete(response.body()?.secure_url)
                    } else {
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(null) }
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val tempFile = File(context.cacheDir, "temp_file_" + System.currentTimeMillis())
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: Exception) {
            return null
        }
    }

    fun updateUserProfile(context: Context, name: String, bio: String, imageUri: String, onComplete: (Boolean) -> Unit) {
        val userId = mAuth.currentUser?.uid ?: return
        val updates = mapOf("name" to name, "bio" to bio, "imageUri" to imageUri)
        database.child(userId).updateChildren(updates).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}
