package com.example.foodgram.services.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FoodgramMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Check if message contains data payload
        remoteMessage.data.isNotEmpty().let {
            val restaurantId = remoteMessage.data["restaurantId"]
            val type = remoteMessage.data["type"]
            
            if (type == "AVAILABILITY_UPDATE" && restaurantId != null) {
                Log.d("FCM", "Received availability update signal for: $restaurantId")
                serviceScope.launch {
                    AvailabilityNotificationService.signalRefresh(restaurantId)
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Normally we would upload this to our backend
    }
}
