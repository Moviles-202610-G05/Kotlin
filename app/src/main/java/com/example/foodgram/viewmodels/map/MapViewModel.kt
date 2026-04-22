package com.example.foodgram.viewmodels.map

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var restaurants by mutableStateOf<List<MapRestaurant>>(emptyList())
        private set

    var categories by mutableStateOf<List<String>>(listOf("All"))
        private set

    var selectedCategory by mutableStateOf("All")
    var isLoading by mutableStateOf(false)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val snapshot = db.collection("restaurants").get().await()
                restaurants = snapshot.documents.mapNotNull { it.toObject(MapRestaurant::class.java)?.copy(id = it.id) }
                    .filter { it.name.isNotBlank() }

                val categoriesSnapshot = db.collection("categories").get().await()
                val remoteCategories = categoriesSnapshot.documents.mapNotNull { it.getString("name") }.filter { it.isNotBlank() }
                
                categories = if (remoteCategories.isEmpty()) {
                    listOf("All", "Burgers", "Pizza", "Sushi", "Italian", "Fast Food")
                } else {
                    listOf("All") + remoteCategories
                }
            } catch (e: Exception) {
                restaurants = emptyList()
            }
            isLoading = false
        }
    }

    fun getFilteredRestaurants(userLocation: LatLng): List<MapRestaurant> {
        val filtered = if (selectedCategory.equals("All", ignoreCase = true)) {
            restaurants
        } else {
            restaurants.filter { restaurant ->
                restaurant.cuisine.contains(selectedCategory, ignoreCase = true) ||
                        restaurant.tags.any { it.contains(selectedCategory, ignoreCase = true) }
            }
        }
        return filtered.sortedBy { calculateDistanceMeters(userLocation, it.location) }
    }
}

private fun calculateDistanceMeters(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0]
}
