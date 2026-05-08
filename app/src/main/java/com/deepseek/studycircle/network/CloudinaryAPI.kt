package com.deepseek.studycircle.network

import com.deepseek.studycircle.models.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CloudinaryAPI {
    @Multipart
    @POST("v1_1/{dnt3lcyoj}/auto/upload")
    suspend fun uploadFile(
        @Path("dnt3lcyoj") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): retrofit2.Response<CloudinaryResponse>
}
