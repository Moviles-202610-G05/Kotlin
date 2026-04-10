package com.example.foodgram.viewmodels.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.restaurants.RestaurantRepository
import com.example.foodgram.views.restaurants.MapRestaurant
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {
    private val repository = RestaurantRepository()

    var restaurants by mutableStateOf<List<MapRestaurant>>(emptyList())
    // Fixed list of default categories
    val categories = listOf("All", "Burgers", "Pizza", "Sushi", "Italian", "Fast Food")
    var isLoading by mutableStateOf(false)

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Fetch restaurants from repository
                val fetchedRestaurants = repository.getRestaurants()
                restaurants = fetchedRestaurants
            } catch (e: Exception) {
                // handle error
            } finally {
                isLoading = false
            }
        }
    }
}
