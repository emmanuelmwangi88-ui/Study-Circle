package com.deepseek.studycircle.models

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val fileUrl: String? = null,
    val fileType: String? = null
)
