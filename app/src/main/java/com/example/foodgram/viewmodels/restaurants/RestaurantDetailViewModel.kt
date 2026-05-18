package com.example.foodgram.viewmodels.restaurants

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.cache.FastCache
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.entities.MenuItemEntity
import com.example.foodgram.data.local.entities.ReviewEntity
import com.example.foodgram.models.restaurants.MenuItem
import com.example.foodgram.models.restaurants.ReviewRestaurant
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val menuItemDao = database.menuItemDao()
    private val reviewDao = database.reviewDao()
    private val fastCache = FastCache

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

            // Set initial data if available
            if (initialData != null) {
                restaurant = initialData
            }

            // 1. FastCache check
            val cachedMenu = fastCache.get("menu_$restaurantId") as? List<MenuItem>
            val cachedReviews = fastCache.get("reviews_$restaurantId") as? List<ReviewRestaurant>
            if (cachedMenu != null) menuItems = cachedMenu
            if (cachedReviews != null) reviews = cachedReviews

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
            } else {
                // Periodically or on load, refresh restaurant data to get latest seats
                try {
                    val snapshot = db.collection("restaurants").document(restaurantId)
                        .get()
                        .await()
                    restaurant = snapshot.toObject(MapRestaurant::class.java)?.copy(id = snapshot.id)
                } catch (e: Exception) {
                    // Keep existing if fetch fails
                }
            }
            
            val restaurantName = restaurant?.name ?: ""
            
            if (restaurantName.isNotEmpty()) {
                // 2. Room check if empty
                if (menuItems.isEmpty()) {
                    val localMenu = menuItemDao.getMenuItemsByRestaurant(restaurantName).first()
                    if (localMenu.isNotEmpty()) menuItems = localMenu.map { it.toMenuItem() }
                }
                if (reviews.isEmpty()) {
                    val localReviews = reviewDao.getReviewsByRestaurant(restaurantName).first()
                    if (localReviews.isNotEmpty()) reviews = localReviews.map { it.toReviewRestaurant() }
                }

                // 3. Firebase fetch
                val menuDeferred = async {
                    try {
                        val items = db.collection("menu")
                            .whereEqualTo("restaurant", restaurantName)
                            .get()
                            .await()
                            .toObjects(MenuItem::class.java)
                        
                        if (items.isNotEmpty()) {
                            menuItems = items
                            fastCache.put("menu_$restaurantId", items)
                            menuItemDao.deleteByRestaurant(restaurantName)
                            menuItemDao.insertMenuItems(items.map { it.toEntity() })
                        }
                        items
                    } catch (e: Exception) {
                        emptyList<MenuItem>()
                    }
                }

                val reviewsDeferred = async {
                    try {
                        val remoteReviews = db.collection("reviews")
                            .whereEqualTo("restaurant", restaurantName)
                            .get()
                            .await()
                            .toObjects(ReviewRestaurant::class.java)
                        
                        if (remoteReviews.isNotEmpty()) {
                            reviews = remoteReviews
                            fastCache.put("reviews_$restaurantId", remoteReviews)
                            reviewDao.deleteByRestaurant(restaurantName)
                            reviewDao.insertReviews(remoteReviews.map { it.toEntity() })
                        }
                        remoteReviews
                    } catch (e: Exception) {
                        emptyList<ReviewRestaurant>()
                    }
                }

                menuDeferred.await()
                reviewsDeferred.await()
            }

            isLoading = false
        }
    }

    private fun MenuItem.toEntity() = MenuItemEntity(
        id = id,
        name = name,
        price = price,
        category = category,
        description = description,
        image = image,
        restaurant = restaurant,
        availability = availability
    )

    private fun MenuItemEntity.toMenuItem() = MenuItem(
        id = id,
        name = name,
        price = price,
        category = category,
        description = description,
        image = image,
        restaurant = restaurant,
        availability = availability
    )

    private fun ReviewRestaurant.toEntity() = ReviewEntity(
        id = id,
        restaurant = restaurant,
        name = name,
        rating = rating,
        comment = comment,
        date = date,
        avatar = avatar,
        avatarColor = avatarColor,
        createdAt = createdAt?.seconds // Store as seconds long
    )

    private fun ReviewEntity.toReviewRestaurant() = ReviewRestaurant(
        id = id,
        restaurant = restaurant,
        name = name,
        rating = rating,
        comment = comment,
        date = date,
        avatar = avatar,
        avatarColor = avatarColor,
        createdAt = createdAt?.let { com.google.firebase.Timestamp(it, 0) }
    )
}
