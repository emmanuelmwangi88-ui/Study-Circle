package com.deepseek.studycircle.models

import com.google.gson.annotations.SerializedName

data class CloudinaryResponse(
    val url: String? = null,
    @SerializedName("secure_url")
    val secureUrl: String? = null,
    @SerializedName("public_id")
    val publicId: String? = null,
)
