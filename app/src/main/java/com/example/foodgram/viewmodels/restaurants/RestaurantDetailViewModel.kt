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
import com.example.foodgram.services.notifications.AvailabilityNotificationService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RestaurantDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val menuItemDao = database.menuItemDao()
    private val reviewDao = database.reviewDao()
    private val fastCache = FastCache
    private var restaurantListener: ListenerRegistration? = null
    private var refreshJob: Job? = null
    private var currentRestaurantId: String? = null

    var restaurant by mutableStateOf<MapRestaurant?>(null)
        private set

    var menuItems by mutableStateOf<List<MenuItem>>(emptyList())
        private set

    var reviews by mutableStateOf<List<ReviewRestaurant>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    override fun onCleared() {
        restaurantListener?.remove()
        refreshJob?.cancel()
        currentRestaurantId?.let { id ->
            FirebaseMessaging.getInstance().unsubscribeFromTopic("restaurant_availability_$id")
        }
        super.onCleared()
    }

    fun loadRestaurantDetail(restaurantId: String, initialData: MapRestaurant? = null) {
        if (restaurantId.isBlank() || restaurantId == "0") {
            isLoading = false
            return
        }

        // 1. Unsubscribe from previous restaurant if any
        currentRestaurantId?.let { oldId ->
            if (oldId != restaurantId) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("restaurant_availability_$oldId")
            }
        }
        currentRestaurantId = restaurantId

        // 2. Subscribe to new restaurant availability updates
        FirebaseMessaging.getInstance().subscribeToTopic("restaurant_availability_$restaurantId")

        // 3. Listen for refresh signals while this VM is active
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            AvailabilityNotificationService.refreshSignals.collectLatest { id ->
                if (id == restaurantId) {
                    refreshAvailability(id)
                }
            }
        }

        // Setup real-time listener for restaurant capacity updates (Secondary/Backup)
        restaurantListener?.remove()
        restaurantListener = db.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                restaurant = snapshot.toObject(MapRestaurant::class.java)?.copy(id = snapshot.id)
            }

        viewModelScope.launch {
            isLoading = true
            // ... (rest of the loading logic remains same)

            // Set initial data if available
            if (initialData != null) {
                restaurant = initialData
            }

            // 1. FastCache check
            val cachedMenu = fastCache.get("menu_$restaurantId") as? List<MenuItem>
            val cachedReviews = fastCache.get("reviews_$restaurantId") as? List<ReviewRestaurant>
            if (cachedMenu != null) menuItems = cachedMenu
            if (cachedReviews != null) reviews = cachedReviews

            // If we don't have restaurant details, fetch them first (listener will eventually update too)
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

    private fun refreshAvailability(restaurantId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("restaurants").document(restaurantId).get().await()
                restaurant = snapshot.toObject(MapRestaurant::class.java)?.copy(id = snapshot.id)
            } catch (e: Exception) {
                // Log or handle error
            }
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
