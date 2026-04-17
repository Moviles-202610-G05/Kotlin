package com.example.foodgram.services.tracker

import com.example.foodgram.models.tracker.MealAnalysis
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun analyzeImage(
        @Header("Authorization") token: String,
        @Header("HTTP-Referer") referer: String = "https://foodgram.com",
        @Header("X-Title") title: String = "FoodGram",
        @Body request: OpenRouterRequest
    ): OpenRouterResponse
}

data class OpenRouterRequest(
    val model: String = "google/gemini-2.0-flash-001",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: List<MessageContent>
)

data class MessageContent(
    val type: String,
    val text: String? = null,
    @com.google.gson.annotations.SerializedName("image_url") val imageUrl: ImageUrl? = null
)

data class ImageUrl(
    val url: String // Base64: "data:image/jpeg;base64,{base64_image}"
)

data class OpenRouterResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val content: String
)

object OpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: OpenRouterApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenRouterApi::class.java)
}
