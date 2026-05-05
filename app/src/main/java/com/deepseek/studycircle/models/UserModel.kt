package com.deepseek.studycircle.models

import androidx.compose.ui.graphics.vector.ImageVector

// ----- Data Models -----
data class User(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    val role: String = "user",
    val bio: String = "",
    val imageUri: String = "",
    val credits: Int = 0,
    val reputation: Double = 0.0,
    val isFirstLogin: Boolean = true,
    val bookmarks: Map<String, Boolean> = emptyMap()
)

data class Resource(
    val id: Int,
    val title: String,
    val author: String,
    val authorBadge: String,
    val type: String,
    val pages: Int,
    val size: String,
    val downloads: Int,
    val rating: Double,
    val cost: Int,
    val category: String = "",
    val isBookmarked: Boolean = false,
    val fileUrl: String = ""
)

data class Review(
    val id: Int = 0,
    val user: String = "",
    val rating: Double = 0.0,
    val text: String = "",
    val date: String = ""
)


data class Tutor(
    val name: String = "",
    val expertise: String = "",
    val rating: Double = 0.0,
    val sessions: Int = 0,
    val creditsPer15Min: Int = 0,
    val imageUrl: String = ""
)

data class Session(
    val id: Int = 0,
    val title: String = "",
    val student: String = "",
    val dateTime: String = "",
    val topic: String = "",
    val isConfirmed: Boolean = false,
    val isLive: Boolean = false,
    val zoomLink: String = "",
    val thumbnailUrl: String = ""
)

data class Notification(
    val id: Int,
    val title: String,
    val time: String,
    val description: String,
    val icon: ImageVector
)

data class BadgeItem(
    val name: String,
    val level: Int,
    val icon: ImageVector
)

data class StudyGroup(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val members: Int = 0,
    val dailyPosts: Int = 0
)

data class CreditTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0,
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = ""
)
