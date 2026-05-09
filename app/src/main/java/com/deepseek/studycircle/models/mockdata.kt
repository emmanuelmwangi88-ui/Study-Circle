package com.deepseek.studycircle.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.database.PropertyName

val trendingResources = mutableStateListOf<Resource>()
val allResources = trendingResources
val tutors = mutableStateListOf<Tutor>()
val upcomingSessions = mutableStateListOf<Session>()
val notificationsList = mutableStateListOf<Notification>()

val allAvailableBadges = listOf(
    BadgeItem("scholar", "Scholar", 1, Icons.Default.School),
    BadgeItem("contributor", "Top Contributor", 2, Icons.Default.Star),
    BadgeItem("hero", "Community Hero", 3, Icons.Default.EmojiEvents),
    BadgeItem("learner", "Quick Learner", 1, Icons.Default.Bolt),
    BadgeItem("helper", "Helpful Peer", 2, Icons.Default.Favorite)
)

val badges = mutableStateListOf<BadgeItem>() // For backward compatibility if needed

data class Session(
    val id: String = "",
    val title: String = "",
    val student: String = "",
    val creatorId: String = "",
    val dateTime: String = "",
    val topic: String = "",
    @get:PropertyName("isConfirmed") @set:PropertyName("isConfirmed") var isConfirmed: Boolean = false,
    @get:PropertyName("isLive") @set:PropertyName("isLive") var isLive: Boolean = false,
    val meetingId: String = "",
    val password: String = "",
    val thumbnailUrl: String = ""
)

data class Resource(
    val id: String,
    val title: String,
    val author: String,
    val authorBadge: String,
    val tag: String,
    val type: String,
    val pages: Int,
    val size: String,
    val downloads: Int,
    val rating: Double,
    val reviews: Int,
    val category: String,
    val cost: Long,
    val isBookmarked: Boolean,
    val fileUrl: String,
    val authorImage: String = ""
)

data class Tutor(
    val name: String,
    val subjects: String,
    val rating: Double,
    val reviews: Int,
    val price: Int,
    val imageUrl: String,
    val expertise: String,
    val creditsPer15Min: Int,
    val sessions: Int
)

data class Notification(
    val id: Int,
    val title: String,
    val time: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class BadgeItem(
    val id: String = "",
    val name: String,
    val level: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
