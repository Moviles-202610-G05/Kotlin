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
import com.example.foodgram.services.tracker.TrackerFacade
import com.example.foodgram.utils.UserSession
import kotlinx.coroutines.launch

class TrackerViewModel(
    private val trackerFacade: TrackerFacade = TrackerFacade()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var analysisResult by mutableStateOf<MealAnalysis?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    private var currentImageUri: Uri? = null

    var mealHistory = mutableStateOf<List<MealHistoryItem>>(emptyList())
    var isHistoryLoading by mutableStateOf(false)

    fun analyzeImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            analysisResult = null

            try {
                analysisResult = trackerFacade.analyzeMeal(context, imageUri)
                currentImageUri = imageUri
            } catch (e: Exception) {
                errorMessage = "Analysis failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveMealToHistory(onSuccess: () -> Unit) {
        val userEmail = UserSession.currentUserEmail ?: return
        val result = analysisResult ?: return
        val imageUri = currentImageUri ?: return

        viewModelScope.launch {
            isSaving = true
            try {
                val userId = UserSession.currentUserUid ?: "unknown"
                trackerFacade.saveMealToHistory(userEmail, userId, result, imageUri)

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

        viewModelScope.launch {
            try {
                mealHistory.value = trackerFacade.fetchMealHistory(userEmail)
            } catch (e: Exception) {
                errorMessage = "History fetch failed: ${e.localizedMessage}"
            } finally {
                isHistoryLoading = false
            }
        }
    }
}
