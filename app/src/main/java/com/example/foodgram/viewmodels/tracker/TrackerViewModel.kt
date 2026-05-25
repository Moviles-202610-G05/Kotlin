package com.example.foodgram.viewmodels.tracker

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.models.tracker.MealAnalysis
import com.example.foodgram.models.tracker.MealHistoryItem
import com.example.foodgram.services.tracker.NetworkConnectivityObserver
import com.example.foodgram.services.tracker.PendingMealWorker
import com.example.foodgram.services.tracker.TrackerFacade
import com.example.foodgram.utils.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TrackerViewModel(
    application: Application,
    private val trackerFacade: TrackerFacade = TrackerFacade()
) : AndroidViewModel(application) {

    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var analysisResult by mutableStateOf<MealAnalysis?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var isOfflineSaved by mutableStateOf(false)

    private var currentImageUri: Uri? = null

    var mealHistory = mutableStateOf<List<MealHistoryItem>>(emptyList())
    var isHistoryLoading by mutableStateOf(false)

    // Reactive count of queued offline meals — drives the UI chip
    val pendingCount = AppDatabase.getDatabase(application)
        .pendingMealDao()
        .observePendingCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val connectivityObserver = NetworkConnectivityObserver(application)

    // Mutex prevents two concurrent offline saves from racing on the DB
    private val offlineSaveMutex = Mutex()

    init {
        // When the network comes back online and there are queued meals, fire WorkManager.
        // WorkManager itself enforces CONNECTED constraint, so enqueue is safe to call eagerly.
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online ->
                if (online && pendingCount.value > 0) {
                    UserSession.currentUserEmail?.let { email ->
                        PendingMealWorker.enqueue(getApplication(), email)
                    }
                }
            }
        }
    }

    fun analyzeImage(context: android.content.Context, imageUri: Uri) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            analysisResult = null

            try {
                val isOnline = connectivityObserver.isCurrentlyOnline()

                if (!isOnline) {
                    // Offline path: encode + queue, then return early.
                    // offlineSaveMutex guards against rapid double-taps writing duplicate rows.
                    offlineSaveMutex.withLock {
                        withContext(Dispatchers.IO) {
                            trackerFacade.saveOffline(context, imageUri)
                        }
                    }
                    isOfflineSaved = true
                    // Auto-dismiss the banner after 4 s
                    launch {
                        kotlinx.coroutines.delay(4_000)
                        isOfflineSaved = false
                    }
                    return@launch
                }

                // Online path (unchanged from before)
                val result = withContext(Dispatchers.IO) {
                    trackerFacade.analyzeMeal(context, imageUri)
                }
                analysisResult = result
                currentImageUri = imageUri
            } catch (e: Exception) {
                errorMessage = "Analysis failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveMealToHistory(onSuccess: () -> Unit) {
        if (isSaving || analysisResult == null) return

        val userEmail = UserSession.currentUserEmail ?: return
        val result = analysisResult ?: return
        val imageUri = currentImageUri ?: return

        viewModelScope.launch {
            isSaving = true
            try {
                val userId = UserSession.currentUserUid ?: "unknown"
                withContext(Dispatchers.IO) {
                    trackerFacade.saveMealToHistory(userEmail, userId, result, imageUri)
                }
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
