package com.deepseek.studycircle.models

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val role: String = "user",
    val bio: String = "",
    val imageUri: String = "",
    val credits: Long = 0,
    val reputation: Double = 0.0,
    val isFirstLogin: Boolean = true,
    val lastLogin: Long = 0,
    val studyTimeMillis: Long = 0,
    val bookmarks: Map<String, Boolean>? = emptyMap(),
    val unlockedResources: Map<String, Boolean>? = emptyMap(),
    val joinedGroups: Map<String, Boolean>? = emptyMap(),
    val badges: List<String>? = emptyList(),
    val expertise: Map<String, Double>? = emptyMap()
)

@Keep
@IgnoreExtraProperties
data class Review(
    val id: String = "",
    val userId: String = "",
    val user: String = "",
    val userImage: String = "",
    val rating: Double = 0.0,
    val text: String = "",
    val date: String = ""
)

@Keep
@IgnoreExtraProperties
data class CreditTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Long = 0,
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = ""
)

@Keep
@IgnoreExtraProperties
data class UploadMaterial(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileType: String = "PDF",
    val author: String = "",
    val authorId: String = "",
    val authorImage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val cost: Long = 0,
    val rating: Double = 5.0,
    val downloadCount: Int = 0,
    val fileSize: String = "0.0 MB",
    val pages: Int = 1
)

@Keep
@IgnoreExtraProperties
data class WhiteboardAnswer(
    val id: String = "",
    val userName: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val voters: Map<String, Int>? = emptyMap() // userId -> (1 for up, -1 for down)
)
