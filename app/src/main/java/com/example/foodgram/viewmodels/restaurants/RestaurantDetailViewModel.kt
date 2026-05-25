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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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

    // MULTI-THREADING & EVENTUAL CONNECTIVITY STRATEGY
    fun submitReview(rating: Int, comment: String, userId: String, userName: String, avatarUrl: String?) {
        val currentRestaurant = restaurant ?: return
        val currentRestaurantId = currentRestaurant.id

        // Use Coroutines (Dispatchers.IO) for background thread operations
        viewModelScope.launch(Dispatchers.IO) {
            val newReview = ReviewRestaurant(
                id = UUID.randomUUID().toString(),
                restaurant = currentRestaurant.name,
                userId = userId,
                name = userName,
                rating = rating,
                comment = comment,
                date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date()),
                avatar = avatarUrl ?: "",
                avatarColor = "#FF9800", // Default FoodGram Orange
                createdAt = com.google.firebase.Timestamp.now()
            )

            // 1. OPTIMISTIC UI UPDATE & CACHING
            val updatedReviews = reviews.toMutableList()
            updatedReviews.add(0, newReview) // Add to top

            launch(Dispatchers.Main) {
                reviews = updatedReviews
            }
            fastCache.put("reviews_$currentRestaurantId", updatedReviews)

            // 2. LOCAL STORAGE (ROOM)
            reviewDao.insertReview(newReview.toEntity())

            // 3. EVENTUAL CONNECTIVITY / SYNC TO FIREBASE
            try {
                val batch = db.batch()

                // Save the review
                val reviewRef = db.collection("reviews").document(newReview.id)
                batch.set(reviewRef, newReview)

                // Update restaurant aggregated data (Average rating and total reviews)
                val restRef = db.collection("restaurants").document(currentRestaurantId)
                val newTotalReviews = currentRestaurant.nuberReviews + 1
                val newAverageRating = ((currentRestaurant.rating * currentRestaurant.nuberReviews) + rating) / newTotalReviews
                batch.update(restRef, "nuberReviews", newTotalReviews)
                batch.update(restRef, "rating", newAverageRating)

                // Update user review counter
                if (userId.isNotEmpty()) {
                    val userRef = db.collection("user").document(userId)
                    batch.update(userRef, "reviewsCount", FieldValue.increment(1))
                }

                batch.commit().await()
            } catch (e: Exception) {
                // If it fails, data is already saved locally in Room and will be read on next load.
                // A complete eventual connectivity system would flag this row in Room to retry later.
                e.printStackTrace()
            }
        }
    }

    fun loadRestaurantDetail(restaurantId: String, initialData: MapRestaurant? = null) {
        if (restaurantId.isBlank() || restaurantId == "0") {
            isLoading = false
            return
        }

        currentRestaurantId?.let { oldId ->
            if (oldId != restaurantId) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("restaurant_availability_$oldId")
            }
        }
        currentRestaurantId = restaurantId

        FirebaseMessaging.getInstance().subscribeToTopic("restaurant_availability_$restaurantId")

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            AvailabilityNotificationService.refreshSignals.collectLatest { id ->
                if (id == restaurantId) {
                    refreshAvailability(id)
                }
            }
        }

        restaurantListener?.remove()
        restaurantListener = db.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                restaurant = snapshot.toObject(MapRestaurant::class.java)?.copy(id = snapshot.id)
            }

        viewModelScope.launch {
            isLoading = true

            if (initialData != null) {
                restaurant = initialData
            }

            val cachedMenu = fastCache.get("menu_$restaurantId") as? List<MenuItem>
            val cachedReviews = fastCache.get("reviews_$restaurantId") as? List<ReviewRestaurant>
            if (cachedMenu != null) menuItems = cachedMenu
            if (cachedReviews != null) reviews = cachedReviews

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
                if (menuItems.isEmpty()) {
                    val localMenu = menuItemDao.getMenuItemsByRestaurant(restaurantName).first()
                    if (localMenu.isNotEmpty()) menuItems = localMenu.map { it.toMenuItem() }
                }
                if (reviews.isEmpty()) {
                    val localReviews = reviewDao.getReviewsByRestaurant(restaurantName).first()
                    if (localReviews.isNotEmpty()) reviews = localReviews.map { it.toReviewRestaurant() }
                }

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
        userId = userId,
        name = name,
        rating = rating,
        comment = comment,
        date = date,
        avatar = avatar,
        avatarColor = avatarColor,
        createdAt = createdAt?.seconds
    )

    private fun ReviewEntity.toReviewRestaurant() = ReviewRestaurant(
        id = id,
        restaurant = restaurant,
        userId = userId,
        name = name,
        rating = rating,
        comment = comment,
        date = date,
        avatar = avatar,
        avatarColor = avatarColor,
        createdAt = createdAt?.let { com.google.firebase.Timestamp(it, 0) }
    )
}