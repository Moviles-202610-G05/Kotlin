package com.example.foodgram.viewmodels.search

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.RestaurantCacheStrategy
import com.example.foodgram.data.local.entities.RestaurantEntity
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val cacheStrategy = RestaurantCacheStrategy(AppDatabase.getDatabase(application).restaurantDao())

    var restaurants by mutableStateOf<List<MapRestaurant>>(emptyList())
        private set
    var categories by mutableStateOf<List<String>>(listOf("All"))
        private set
    var isLoading by mutableStateOf(false)
        private set
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("All")

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true

            if (cacheStrategy.isFresh()) {
                val local = cacheStrategy.getAll()
                if (local.isNotEmpty()) {
                    restaurants = local.map { it.toMapRestaurant() }
                    isLoading = false
                    return@launch
                }
            }

            try {
                val snapshot = db.collection("restaurants").get().await()
                val remote = snapshot.documents
                    .mapNotNull { it.toObject(MapRestaurant::class.java)?.copy(id = it.id) }
                    .filter { it.name.isNotBlank() }
                if (remote.isNotEmpty()) {
                    restaurants = remote
                    cacheStrategy.put(remote.map { it.toEntity() })
                }

                val catSnapshot = db.collection("categories").get().await()
                val remoteCategories = catSnapshot.documents
                    .mapNotNull { it.getString("name") }
                    .filter { it.isNotBlank() }
                categories = if (remoteCategories.isEmpty()) {
                    listOf("All", "Burgers", "Pizza", "Sushi", "Italian", "Fast Food")
                } else {
                    listOf("All") + remoteCategories
                }
            } catch (_: Exception) {
                val stale = cacheStrategy.getAll()
                if (stale.isNotEmpty() && restaurants.isEmpty()) {
                    restaurants = stale.map { it.toMapRestaurant() }
                }
            }

            isLoading = false
        }
    }

    fun getFilteredRestaurants(): List<MapRestaurant> {
        return restaurants.filter { r ->
            val matchesCategory = selectedCategory.equals("All", ignoreCase = true) ||
                    r.cuisine.contains(selectedCategory, ignoreCase = true) ||
                    r.tags.any { it.contains(selectedCategory, ignoreCase = true) }
            val matchesSearch = searchQuery.isBlank() ||
                    r.name.contains(searchQuery, ignoreCase = true) ||
                    r.cuisine.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    private fun MapRestaurant.toEntity() = RestaurantEntity(
        id = id, name = name, rating = rating, badge = badge, badge2 = badge2,
        cuisine = cuisine, description = description, direction = direction,
        distance = distance, image = image, lat = lat, long = long,
        nuberReviews = nuberReviews, price = price, spots = spots, spotsA = spotsA,
        numberOfSeats = numberOfSeats, seatsOccupied = seatsOccupied, tags = tags, time = time
    )

    private fun RestaurantEntity.toMapRestaurant() = MapRestaurant(
        id = id, name = name, rating = rating, badge = badge, badge2 = badge2,
        cuisine = cuisine, description = description, direction = direction,
        distance = distance, image = image, lat = lat, long = long,
        nuberReviews = nuberReviews, price = price, spots = spots, spotsA = spotsA,
        numberOfSeats = numberOfSeats, seatsOccupied = seatsOccupied, tags = tags, time = time
    )
}
