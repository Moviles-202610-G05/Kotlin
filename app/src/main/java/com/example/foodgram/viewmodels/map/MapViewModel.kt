package com.example.foodgram.viewmodels.map // Matches your folder path

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Ensure these point to MODELS
import com.example.foodgram.models.restaurants.MapRestaurant
import com.example.foodgram.models.restaurants.RestaurantRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

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
            restaurants = repository.getRestaurants()

            val remoteCategories = repository.getCategories()
            categories = if (remoteCategories.isEmpty()) {
                listOf("All", "Burgers", "Pizza", "Sushi", "Italian", "Fast Food")
            } else {
                listOf("All") + remoteCategories
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
