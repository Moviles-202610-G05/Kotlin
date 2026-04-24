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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            // 1. Iniciamos en el Main Thread para bloquear la UI inmediatamente
            isLoading = true 
            errorMessage = null
            analysisResult = null

            try {
                // 2. Saltamos a un hilo de IO para el procesamiento pesado de la IA y fotos
                val result = withContext(Dispatchers.IO) {
                    trackerFacade.analyzeMeal(context, imageUri)
                }
                
                // 3. Volvemos al Main (automático) para guardar el resultado
                analysisResult = result
                currentImageUri = imageUri
            } catch (e: Exception) {
                errorMessage = "Analysis failed: ${e.localizedMessage}"
            } finally {
                // 4. Desbloqueamos el botón en el Main
                isLoading = false
            }
        }
    }

    fun saveMealToHistory(onSuccess: () -> Unit) {
        // 1. Evitar duplicados: Si ya está guardando o no hay resultados, salir.
        if (isSaving || analysisResult == null) return

        val userEmail = UserSession.currentUserEmail ?: return
        val result = analysisResult ?: return
        val imageUri = currentImageUri ?: return

        viewModelScope.launch {
            isSaving = true
            try {
                val userId = UserSession.currentUserUid ?: "unknown"
                
                // 2. Ejecutar el guardado (Firestore/Storage) en hilo de IO
                withContext(Dispatchers.IO) {
                    trackerFacade.saveMealToHistory(userEmail, userId, result, imageUri)
                }

                // 3. Limpiar el resultado actual para que no se pueda volver a guardar el mismo
                analysisResult = null 
                currentImageUri = null

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
