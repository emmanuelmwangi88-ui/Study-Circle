package com.deepseek.studycircle.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.mutableStateListOf

val trendingResources = listOf(
    Resource("1", "Organic Chemistry Cheat Sheet", "Dr. Aris V.", "Gold", "GOLD", "PDF", 12, "4.2 MB", 1200, 4.9, 250, "Chemistry", 10L, isBookmarked = false, fileUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"),
    Resource(
        "2",
        "Advanced Calculus III Master Notes",
        "Prof. Miller",
        "Gold",
        "TOP",
        "DOCX",
        34,
        "8.1 MB",
        840,
        4.8,
        300,
        "Mathematics",
        20L,
        isBookmarked = true,
        fileUrl = "https://example.com/math.docx"
    ),
    Resource("3", "Quantum Mechanics 101: Summary", "Sarah K.", "Gold", "VERIFIED", "PPTX", 18, "5.6 MB", 2100, 4.7, 200, "Physics", 15L, isBookmarked = false, fileUrl = "https://example.com/phys.pptx")
)

val allResources = trendingResources

val tutors = listOf(
    Tutor("Dr. Aris Thorne", "Advanced Thermodynamics & Fluid Mechanics", 4.9, 215, 200, "https://images.unsplash.com/photo-1544717297-fa154da09f9b?auto=format&fit=crop&q=80&w=200", "PhD", 50, 10),
    Tutor("Marcus Johnson", "Advanced Mathematics • Data Science", 4.9, 124, 200, "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&q=80&w=200", "MSc", 40, 20),
    Tutor("Sarah Jenkins", "Literature & Thesis Writing", 4.7, 89, 150, "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200", "MA", 30, 30),
    Tutor("Priya Sharma", "Organic Chemistry", 4.8, 178, 180, "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&q=80&w=200", "PhD", 60, 40)
)

val upcomingSessions = mutableStateListOf(
    Session(
        id = "1", 
        title = "Organic Reaction Mechanisms", 
        student = "Aris Thorne", 
        dateTime = "LIVE NOW", 
        topic = "Chemistry", 
        isConfirmed = true, 
        isLive = true, 
        thumbnailUrl = "https://images.unsplash.com/photo-1532187878418-9f110018dc51?auto=format&fit=crop&q=80&w=600"
    ),
    Session(
        id = "2", 
        title = "Advanced Algebra Hub", 
        student = "Marcus Lee", 
        dateTime = "TOMORROW · 10:30", 
        topic = "Mathematics", 
        isConfirmed = true, 
        isLive = false, 
        thumbnailUrl = ""
    )
)

val notificationsList = listOf(
    Notification(1, "Credit Alert", "2m ago", "You earned 50 credits for Biology Notes", Icons.Filled.Notifications),
    Notification(2, "Session Reminder", "now", "New booking from Sarah for Chemistry at 3 PM", Icons.Filled.Notifications),
    Notification(3, "Reputation Milestone", "1h ago", "Reputation score increased to 4.9 – “Master Contributor” unlocked", Icons.Filled.Star)
)

val badges = listOf(
    BadgeItem("Top Contributor", 5, Icons.Filled.Star),
    BadgeItem("Quick Responder", 3, Icons.Filled.Star),
    BadgeItem("Subject Expert", 4, Icons.Filled.Star),
    BadgeItem("Helpful Mentor", 2, Icons.Filled.Star)
)

val studyGroups = listOf(
    StudyGroup(1, "Organic Chemistry Study", "Discussion on reaction mechanisms and synthesis.", 156, 12, "Science"),
    StudyGroup(2, "Advanced Math Hub", "Solving complex calculus and linear algebra problems together.", 89, 5, "Mathematics"),
)

data class Session(
    val id: String = "",
    val title: String = "",
    val student: String = "",
    val creatorId: String = "",
    val dateTime: String = "",
    val topic: String = "",
    val isConfirmed: Boolean = false,
    val isLive: Boolean = false,
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
    val fileUrl: String
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
    val name: String,
    val level: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
data class StudyGroup(
    val id: Int,
    val name: String,
    val description: String,
    val members: Int,
    val dailyPosts: Int,
    val category: String = "General"
)
