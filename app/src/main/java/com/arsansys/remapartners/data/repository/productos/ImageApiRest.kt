package com.arsansys.remapartners.data.repository.productos

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ImageApiRest {
    @Streaming
    @GET("/api/binary-image/{fileName}")
    suspend fun getImageBinary(@Path("fileName") fileName: String): Response<ResponseBody>
}

class ImageRepository(private val apiService: ImageApiRest) {
    suspend fun getImageBytes(imageUrl: String): ByteArray? {
        try {
            val fileName = imageUrl.substringAfterLast("/")

            val response = apiService.getImageBinary(fileName)
            if (response.isSuccessful) {
                return response.body()?.bytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}