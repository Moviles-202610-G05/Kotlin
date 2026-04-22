package com.example.foodgram.viewmodels.restaurants

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.restaurants.MenuItem
import com.example.foodgram.models.restaurants.ReviewRestaurant
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var restaurant by mutableStateOf<MapRestaurant?>(null)
        private set

    var menuItems by mutableStateOf<List<MenuItem>>(emptyList())
        private set

    var reviews by mutableStateOf<List<ReviewRestaurant>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadRestaurantDetail(restaurantId: String, initialData: MapRestaurant? = null) {
        if (restaurantId.isBlank() || restaurantId == "0") {
            isLoading = false
            return
        }
        viewModelScope.launch {
            isLoading = true

            // Set initial data if available to draw the card/view immediately
            if (initialData != null) {
                restaurant = initialData
            }

            // If we don't have restaurant details, fetch them first
            if (restaurant == null) {
                try {
                    val snapshot = db.collection("restaurants").document(restaurantId)
                        .get()
                        .await()
                    restaurant = snapshot.toObject(MapRestaurant::class.java)?.copy(id = snapshot.id)
                } catch (e: Exception) {
                    restaurant = null
                }
            }
            
            val restaurantName = restaurant?.name ?: ""
            
            if (restaurantName.isNotEmpty()) {
                // Fetch menu and reviews in parallel using async to speed up loading
                val menuDeferred = async {
                    try {
                        db.collection("menu")
                            .whereEqualTo("restaurant", restaurantName)
                            .get()
                            .await()
                            .toObjects(MenuItem::class.java)
                    } catch (e: Exception) {
                        emptyList<MenuItem>()
                    }
                }

                val reviewsDeferred = async {
                    try {
                        db.collection("reviews")
                            .whereEqualTo("restaurant", restaurantName)
                            .get()
                            .await()
                            .toObjects(ReviewRestaurant::class.java)
                    } catch (e: Exception) {
                        emptyList<ReviewRestaurant>()
                    }
                }

                // Wait for both to complete
                menuItems = menuDeferred.await()
                reviews = reviewsDeferred.await()
            }

            isLoading = false
        }
    }
}
