package com.example.foodgram.services.tracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.foodgram.models.tracker.MealAnalysis
import com.example.foodgram.models.tracker.MealHistoryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TrackerFacade(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val gson: Gson = Gson()
) {

    suspend fun analyzeMeal(context: Context, imageUri: Uri): MealAnalysis {
        val base64Image = encodeImageToBase64(context, imageUri)
        val request = OpenRouterRequest(
            messages = listOf(
                Message(
                    role = "system",
                    content = listOf(MessageContent(type = "text", text = SYSTEM_PROMPT))
                ),
                Message(
                    role = "user",
                    content = listOf(
                        MessageContent(
                            type = "text",
                            text = "Analyze this meal. Return ONLY the JSON object. No conversational text. No markdown blocks."
                        ),
                        MessageContent(
                            type = "image_url",
                            imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                        )
                    )
                )
            )
        )

        val response = OpenRouterClient.api.analyzeImage(API_KEY, request = request)
        val content = response.choices.firstOrNull()?.message?.content ?: ""
        val jsonString = extractAndNormalizeJson(content)
        return gson.fromJson(jsonString, MealAnalysis::class.java)
    }

    suspend fun saveMealToHistory(
        userEmail: String,
        userId: String,
        result: MealAnalysis,
        imageUri: Uri
    ) {
        val fileName = "AIimages/user_$userId/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imageUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        val firstComponent = result.components.firstOrNull()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val timestampStr = sdf.format(Date())

        val mealData = hashMapOf(
            "userEmail" to userEmail,
            "timestamp" to timestampStr,
            "dishName" to result.dishName,
            "totalCalories" to result.totalCalories,
            "totalProteinG" to result.macronutrientsTotals.proteinG,
            "totalCarbsG" to result.macronutrientsTotals.carbsG,
            "totalFatG" to result.macronutrientsTotals.fatG,
            "imagePath" to downloadUrl,
            "confidence" to result.confidence,
            "food" to (firstComponent?.food ?: ""),
            "estimated_portion" to (firstComponent?.portion ?: ""),
            "protein_g" to (firstComponent?.proteinG ?: 0.0),
            "carbs_g" to (firstComponent?.carbsG ?: 0.0),
            "fat_g" to (firstComponent?.fatG ?: 0.0)
        )

        db.collection("meals")
            .add(mealData)
            .await()
    }

    suspend fun fetchMealHistory(userEmail: String): List<MealHistoryItem> {
        val result = db.collection("meals")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .await()

        return result.map { doc ->
            MealHistoryItem(
                id = doc.id,
                userEmail = doc.getString("userEmail") ?: "",
                timestamp = doc.getString("timestamp") ?: "",
                dishName = doc.getString("dishName") ?: "",
                totalCalories = doc.getDouble("totalCalories") ?: 0.0,
                totalProteinG = doc.getDouble("totalProteinG") ?: 0.0,
                totalCarbsG = doc.getDouble("totalCarbsG") ?: 0.0,
                totalFatG = doc.getDouble("totalFatG") ?: 0.0,
                imagePath = doc.getString("imagePath") ?: ""
            )
        }.sortedByDescending { it.timestamp }
    }

    private fun encodeImageToBase64(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = 2 // Reducir a la mitad para ahorrar memoria al decodificar
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                ?: throw IllegalStateException("Unable to decode image")
            
            val outputStream = ByteArrayOutputStream()
            
            // Redimensionar a un máximo de 1024px para la IA (suficiente calidad, menos RAM)
            val maxDimension = 1024
            val scale = maxOf(bitmap.width, bitmap.height).toFloat() / maxDimension
            val targetWidth = if (scale > 1) (bitmap.width / scale).toInt() else bitmap.width
            val targetHeight = if (scale > 1) (bitmap.height / scale).toInt() else bitmap.height

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            
            // Liberar memoria del original si es diferente
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()
            scaledBitmap.recycle() // Liberar memoria del escalado
            
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw IllegalStateException("Error processing image: ${e.localizedMessage}")
        }
    }

    private fun extractAndNormalizeJson(rawContent: String): String {
        var cleanContent = rawContent.trim()
        if (cleanContent.contains("```")) {
            val match = Regex("""```(?:json)?\s*(\{.*?\})\s*```""", RegexOption.DOT_MATCHES_ALL)
                .find(cleanContent)
            if (match != null) {
                cleanContent = match.groupValues[1]
            }
        }

        val start = cleanContent.indexOf('{')
        val end = cleanContent.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            throw IllegalStateException("The AI response did not contain a valid JSON object")
        }

        var jsonString = cleanContent.substring(start, end + 1)
        val numericKeys = listOf(
            "estimated_weight_g", "calories", "protein_g", "carbs_g", "fat_g",
            "total_calories", "protein", "carbohydrates", "fat"
        )
        numericKeys.forEach { key ->
            val regex = Regex("""("$key"\s*:\s*)([^,}]+)""")
            jsonString = jsonString.replace(regex) { matchResult ->
                val prefix = matchResult.groupValues[1]
                val rawValue = matchResult.groupValues[2]
                val numberMatch = Regex("""(\d+[.,]?\d*)""").find(rawValue)
                val cleanValue = numberMatch?.value?.replace(",", ".") ?: "0.0"
                "$prefix$cleanValue"
            }
        }
        return jsonString
    }

    companion object {
        private val API_KEY = "Bearer ${com.example.foodgram.BuildConfig.OPENROUTER_API_KEY}"

        private val SYSTEM_PROMPT = """
            You are a nutrition analysis expert. Your task is to identify the dish in the image and provide a detailed nutritional breakdown.

            Strict JSON Rule: Return ONLY a valid JSON object. Do not include any text before or after the JSON. Do not include Markdown code blocks (```json).

            Follow these rules:
            1. Identify every visible food component.
            2. Estimate portion size and calories for each.
            3. Calculate totals for calories and macronutrients (protein, carbs, fat).
            4. Numeric fields (weight, calories, protein, carbs, fat) MUST be numbers only.
               Do NOT include units like "g", "kcal", or "%" inside the numeric values.

            JSON Format:
            {
              "dish_name": "",
              "components": [
                {
                  "food": "",
                  "estimated_portion": "",
                  "estimated_weight_g": 0,
                  "calories": 0,
                  "protein_g": 0,
                  "carbs_g": 0,
                  "fat_g": 0
                }
              ],
              "total_calories": 0,
              "macronutrients_totals": {
                "protein_g": 0,
                "carbs_g": 0,
                "fat_g": 0
              },
              "macronutrient_distribution_percent": {
                "protein": 0,
                "carbohydrates": 0,
                "fat": 0
              },
              "confidence": "low | medium | high"
            }
        """.trimIndent()
    }
}