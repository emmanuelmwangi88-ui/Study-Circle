package com.deepseek.studycircle.models

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class StudyGroup(
    val id: Any? = null, // Flexible ID to handle both Long and String from DB
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val createdBy: String = "",
    val members: Map<String, Boolean>? = emptyMap()
)
