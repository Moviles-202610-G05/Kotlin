package com.example.foodgram.services.tracker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.workDataOf
import android.util.Base64
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.models.tracker.MealAnalysis
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Equivalent to Flutter's MealAnalysisIsolate: runs entirely off the main thread,
 * survives app restart, and is constrained to run only when the network is available.
 *
 * Threading model:
 *   - doWork() runs on Dispatchers.IO (WorkManager default for CoroutineWorker)
 *   - AI inference switches to Dispatchers.Default (CPU-bound JSON parsing)
 *   - Firestore upload stays on Dispatchers.IO
 */
class PendingMealWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val dao = AppDatabase.getDatabase(context).pendingMealDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val facade = TrackerFacade()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val userEmail = inputData.getString(KEY_USER_EMAIL)
            ?: return@withContext Result.failure()

        val pendingMeals = dao.getPending()
        if (pendingMeals.isEmpty()) return@withContext Result.success()

        var successCount = 0
        for (meal in pendingMeals) {
            try {
                // CPU-bound parsing on Default, network call stays on IO via facade
                val analysis = withContext(Dispatchers.Default) {
                    analyzeWithRetry(meal.imageBase64)
                }

                val imageBytes = Base64.decode(meal.imageBase64, Base64.DEFAULT)
                val storageRef = storage.reference
                    .child("AIimages/offline/${UUID.randomUUID()}.jpg")
                storageRef.putBytes(imageBytes).await()
                val imageUrl = storageRef.downloadUrl.await().toString()

                val firstComponent = analysis.components.firstOrNull()
                val mealData = hashMapOf(
                    "userEmail" to userEmail,
                    "timestamp" to meal.timestamp,
                    "dishName" to analysis.dishName,
                    "totalCalories" to analysis.totalCalories,
                    "totalProteinG" to analysis.macronutrientsTotals.proteinG,
                    "totalCarbsG" to analysis.macronutrientsTotals.carbsG,
                    "totalFatG" to analysis.macronutrientsTotals.fatG,
                    "imagePath" to imageUrl,
                    "confidence" to analysis.confidence,
                    "food" to (firstComponent?.food ?: ""),
                    "estimated_portion" to (firstComponent?.portion ?: ""),
                    "protein_g" to (firstComponent?.proteinG ?: 0.0),
                    "carbs_g" to (firstComponent?.carbsG ?: 0.0),
                    "fat_g" to (firstComponent?.fatG ?: 0.0),
                    "syncedFromOffline" to true
                )
                firestore.collection("meals").add(mealData).await()
                dao.markSynced(meal.id)
                successCount++
            } catch (_: Exception) {
                // One meal failing doesn't abort the whole batch
            }
        }

        dao.deleteSynced()
        if (successCount > 0) Result.success() else Result.retry()
    }

    // Mirrors Flutter's 3-attempt retry loop in MealAnalysisIsolate
    private suspend fun analyzeWithRetry(base64Image: String): MealAnalysis =
        run {
            var lastException: Exception? = null
            repeat(3) { attempt ->
                try {
                    return@run facade.analyzeFromBase64(base64Image)
                } catch (e: Exception) {
                    lastException = e
                    if (attempt < 2) delay(3_000)
                }
            }
            throw lastException!!
        }

    companion object {
        const val KEY_USER_EMAIL = "user_email"
        private const val WORK_NAME = "pending_meal_sync"

        fun enqueue(context: Context, userEmail: String) {
            val request = OneTimeWorkRequestBuilder<PendingMealWorker>()
                .setInputData(workDataOf(KEY_USER_EMAIL to userEmail))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
