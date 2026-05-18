package com.example.foodgram.services.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Singleton service that broadcasts signals to refresh restaurant data.
 * This can be triggered by FCM messages or manual refresh requests.
 */
object AvailabilityNotificationService {
    private val _refreshSignals = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val refreshSignals: SharedFlow<String> = _refreshSignals

    /**
     * Signals that a specific restaurant's data should be re-fetched.
     * @param restaurantId The ID of the restaurant that was updated.
     */
    suspend fun signalRefresh(restaurantId: String) {
        _refreshSignals.emit(restaurantId)
    }
}
