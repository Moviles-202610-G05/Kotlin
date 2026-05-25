package com.example.foodgram.viewmodels.map

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.cache.FastCache
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.RestaurantCacheStrategy
import com.example.foodgram.data.local.entities.RestaurantEntity
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val restaurantDao = database.restaurantDao()
    private val fastCache = FastCache
    private val cacheStrategy = RestaurantCacheStrategy(restaurantDao)

    var restaurants by mutableStateOf<List<MapRestaurant>>(emptyList())
        private set

    var categories by mutableStateOf<List<String>>(listOf("All"))
        private set

    var selectedCategory by mutableStateOf("All")
    var searchQuery by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    // True when the displayed list comes from Room cache rather than a live Firestore response.
    // Drives the "Showing cached data" banner in the UI.
    var isShowingCachedData by mutableStateOf(false)
        private set

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true

            // ── Layer 1: FastCache (in-memory, zero latency) ──────────────────────
            val memCached = fastCache.get("restaurants") as? List<MapRestaurant>
            if (memCached != null) {
                restaurants = memCached
            }

            // ── Layer 2: Room TTL check (cache-aside strategy) ────────────────────
            // If Room data is within TTL, serve it without touching Firestore.
            // Dispatchers.IO is handled inside cacheStrategy.
            if (cacheStrategy.isFresh()) {
                val local = cacheStrategy.getAll()
                if (local.isNotEmpty()) {
                    restaurants = local.map { it.toMapRestaurant() }
                    fastCache.put("restaurants", restaurants)
                    isShowingCachedData = true
                    isLoading = false
                    return@launch          // ← skip Firestore entirely
                }
            }

            // ── Layer 3: Firestore (cache is stale or empty) ──────────────────────
            try {
                val snapshot = db.collection("restaurants").get().await()
                val remote = snapshot.documents
                    .mapNotNull { it.toObject(MapRestaurant::class.java)?.copy(id = it.id) }
                    .filter { it.name.isNotBlank() }

                if (remote.isNotEmpty()) {
                    restaurants = remote
                    fastCache.put("restaurants", remote)
                    // Stamp every row with current time — resets the TTL clock
                    cacheStrategy.put(remote.map { it.toEntity() })
                    isShowingCachedData = false
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
                // Offline or network error: fall back to stale Room data if any
                val stale = cacheStrategy.getAll()
                if (stale.isNotEmpty() && restaurants.isEmpty()) {
                    restaurants = stale.map { it.toMapRestaurant() }
                    fastCache.put("restaurants", restaurants)
                }
                // Mark as cached regardless of freshness when we couldn't reach Firestore
                if (restaurants.isNotEmpty()) isShowingCachedData = true
            }

            isLoading = false
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

    fun getFilteredRestaurants(userLocation: LatLng): List<MapRestaurant> {
        val filtered = restaurants.filter { r ->
            val matchesCategory = selectedCategory.equals("All", ignoreCase = true) ||
                    r.cuisine.contains(selectedCategory, ignoreCase = true) ||
                    r.tags.any { it.contains(selectedCategory, ignoreCase = true) }

            val matchesSearch = searchQuery.isBlank() ||
                    r.name.contains(searchQuery, ignoreCase = true) ||
                    r.cuisine.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
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
