package com.example.foodgram.viewmodels.tracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.tracker.MealAnalysis
import com.example.foodgram.models.tracker.MealHistoryItem
import com.example.foodgram.services.tracker.*
import com.example.foodgram.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

class TrackerViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val gson = Gson()

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var analysisResult by mutableStateOf<MealAnalysis?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    private var currentImageUri: Uri? = null

    var mealHistory = mutableStateOf<List<MealHistoryItem>>(emptyList())
    var isHistoryLoading by mutableStateOf(false)

    // Using a placeholder/free key from the Flutter repo - you should replace this with your own later
    private val apiKey = "Bearer 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"

    private val systemPrompt = """
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

    fun analyzeImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            analysisResult = null

            try {
                val base64Image = encodeImageToBase64(context, imageUri)
                val request = OpenRouterRequest(
                    messages = listOf(
                        Message(role = "system", content = listOf(MessageContent(type = "text", text = systemPrompt))),
                        Message(role = "user", content = listOf(
                            MessageContent(type = "text", text = "Analyze this meal. Return ONLY the JSON object. No conversational text. No markdown blocks."),
                            MessageContent(type = "image_url", imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64Image"))
                        ))
                    )
                )

                val response = OpenRouterClient.api.analyzeImage(apiKey, request = request)
                val content = response.choices.firstOrNull()?.message?.content ?: ""
                
                // Robust extraction: Handle Markdown code blocks and find the first '{' and last '}'
                var cleanContent = content.trim()
                if (cleanContent.contains("```")) {
                    // Try to extract content between ```json and ``` or just ``` and ```
                    val match = Regex("""```(?:json)?\s*(\{.*?\})\s*```""", RegexOption.DOT_MATCHES_ALL).find(cleanContent)
                    if (match != null) {
                        cleanContent = match.groupValues[1]
                    }
                }

                val start = cleanContent.indexOf('{')
                val end = cleanContent.lastIndexOf('}')
                
                if (start == -1 || end == -1 || end <= start) {
                    throw Exception("The AI response did not contain a valid JSON object. AI said: ${content.take(100)}...")
                }
                
                var jsonString = cleanContent.substring(start, end + 1)

                // Robustness: Specifically target numeric fields and strip non-numeric characters
                val numericKeys = listOf(
                    "estimated_weight_g", "calories", "protein_g", "carbs_g", "fat_g",
                    "total_calories", "protein", "carbohydrates", "fat"
                )
                numericKeys.forEach { key ->
                    // Match the key and its value until the next separator (comma or brace)
                    val regex = Regex("""("$key"\s*:\s*)([^,}]+)""")
                    jsonString = jsonString.replace(regex) { matchResult ->
                        val prefix = matchResult.groupValues[1]
                        val rawValue = matchResult.groupValues[2]
                        // Extract only the first numeric sequence (handles 123, 123.4, 123,4)
                        val numberMatch = Regex("""(\d+[.,]?\d*)""").find(rawValue)
                        val cleanValue = numberMatch?.value?.replace(",", ".") ?: "0.0"
                        "$prefix$cleanValue"
                    }
                }

                analysisResult = gson.fromJson(jsonString, MealAnalysis::class.java)
                currentImageUri = imageUri
            } catch (e: Exception) {
                errorMessage = "Analysis failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun encodeImageToBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        
        // Resize to reduce payload size for the API
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800 * bitmap.height / bitmap.width, true)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun saveMealToHistory(onSuccess: () -> Unit) {
        val userEmail = UserSession.currentUserEmail ?: return
        val result = analysisResult ?: return
        val imageUri = currentImageUri ?: return

        viewModelScope.launch {
            isSaving = true
            try {
                // 1. Upload image to Firebase Storage
                val userId = UserSession.currentUserUid ?: "unknown"
                val fileName = "AIimages/user_$userId/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)
                
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 2. Save meal data to Firestore with the image URL, matching the existing "meals" collection structure
                val firstComponent = result.components.firstOrNull()
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", java.util.Locale.getDefault())
                val timestampStr = sdf.format(java.util.Date())
                
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
                    // Flattened first component details as seen in the screenshot
                    "food" to (firstComponent?.food ?: ""),
                    "estimated_portion" to (firstComponent?.portion ?: ""),
                    "protein_g" to (firstComponent?.proteinG ?: 0.0),
                    "carbs_g" to (firstComponent?.carbsG ?: 0.0),
                    "fat_g" to (firstComponent?.fatG ?: 0.0)
                )

                db.collection("meals")
                    .add(mealData)
                    .await()

                fetchMealHistory()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to save: ${e.localizedMessage}"
            } finally {
                isSaving = false
            }
        }
    }

    fun fetchMealHistory() {
        val userEmail = UserSession.currentUserEmail ?: return
        isHistoryLoading = true

        db.collection("meals")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener { result ->
                val history = result.map { doc ->
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
                
                mealHistory.value = history
                isHistoryLoading = false
            }
            .addOnFailureListener { e ->
                isHistoryLoading = false
                errorMessage = "History fetch failed: ${e.localizedMessage}"
            }
    }
}
