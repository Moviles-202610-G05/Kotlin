package com.example.foodgram.viewmodels.map

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.cache.FastCache
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.entities.RestaurantEntity
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val restaurantDao = database.restaurantDao()
    private val fastCache = FastCache

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
            
            // 1. FastCache
            val cached = fastCache.get("restaurants") as? List<MapRestaurant>
            if (cached != null) {
                restaurants = cached
            }

            // 2. Room
            val local = restaurantDao.getAllRestaurants().first()
            if (local.isNotEmpty() && restaurants.isEmpty()) {
                restaurants = local.map { it.toMapRestaurant() }
            }

            try {
                // 3. Firebase
                val snapshot = db.collection("restaurants").get().await()
                val remoteRestaurants = snapshot.documents.mapNotNull { it.toObject(MapRestaurant::class.java)?.copy(id = it.id) }
                    .filter { it.name.isNotBlank() }

                if (remoteRestaurants.isNotEmpty()) {
                    restaurants = remoteRestaurants
                    fastCache.put("restaurants", remoteRestaurants)
                    
                    // Sync Room
                    restaurantDao.deleteAll()
                    restaurantDao.insertRestaurants(remoteRestaurants.map { it.toEntity() })
                }

                val categoriesSnapshot = db.collection("categories").get().await()
                val remoteCategories = categoriesSnapshot.documents.mapNotNull { it.getString("name") }.filter { it.isNotBlank() }
                
                categories = if (remoteCategories.isEmpty()) {
                    listOf("All", "Burgers", "Pizza", "Sushi", "Italian", "Fast Food")
                } else {
                    listOf("All") + remoteCategories
                }
            } catch (e: Exception) {
                // Keep existing data
            }
            isLoading = false
        }
    }

    private fun MapRestaurant.toEntity() = RestaurantEntity(
        id = id,
        name = name,
        rating = rating,
        badge = badge,
        badge2 = badge2,
        cuisine = cuisine,
        description = description,
        direction = direction,
        distance = distance,
        image = image,
        lat = lat,
        long = long,
        nuberReviews = nuberReviews,
        price = price,
        spots = spots,
        spotsA = spotsA,
        numberOfSeats = numberOfSeats,
        seatsOccupied = seatsOccupied,
        tags = tags,
        time = time
    )

    private fun RestaurantEntity.toMapRestaurant() = MapRestaurant(
        id = id,
        name = name,
        rating = rating,
        badge = badge,
        badge2 = badge2,
        cuisine = cuisine,
        description = description,
        direction = direction,
        distance = distance,
        image = image,
        lat = lat,
        long = long,
        nuberReviews = nuberReviews,
        price = price,
        spots = spots,
        spotsA = spotsA,
        numberOfSeats = numberOfSeats,
        seatsOccupied = seatsOccupied,
        tags = tags,
        time = time
    )

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
