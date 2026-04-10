package com.example.foodgram.viewmodels.restaurants

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.restaurants.MenuItem
import com.example.foodgram.models.restaurants.RestaurantRepository
import com.example.foodgram.models.restaurants.RestaurantReview
import com.example.foodgram.views.restaurants.MapRestaurant
import kotlinx.coroutines.launch

class RestaurantDetailViewModel(
    private val repository: RestaurantRepository = RestaurantRepository()
) : ViewModel() {

    var restaurant by mutableStateOf<MapRestaurant?>(null)
        private set

    var menuItems by mutableStateOf<List<MenuItem>>(emptyList())
        private set

    var reviews by mutableStateOf<List<RestaurantReview>>(emptyList())
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
            // Use initial data if available to show header immediately
            if (initialData != null) {
                restaurant = initialData
            }
            
            // In a real app, you might fetch the full restaurant object by ID here if not provided
            if (restaurant == null) {
                restaurant = repository.getRestaurantById(restaurantId)
            }
            
            menuItems = repository.getRestaurantMenu(restaurantId)
            reviews = repository.getRestaurantReviews(restaurantId)
            isLoading = false
        }
    }
}
