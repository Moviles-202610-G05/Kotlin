package com.example.foodgram.viewmodels.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.entities.SavedRestaurantEntity
import com.example.foodgram.models.restaurants.MapRestaurant
import com.example.foodgram.utils.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).savedRestaurantDao()
    private val userEmail get() = UserSession.currentUserEmail ?: ""

    val savedItems: StateFlow<List<SavedRestaurantEntity>> = dao
        .getByUser(userEmail)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val savedIds: StateFlow<Set<String>> = savedItems
        .map { list -> list.map { it.restaurantId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun toggleSave(restaurant: MapRestaurant) {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.isSaved(restaurant.id, userEmail)) {
                dao.delete(restaurant.id, userEmail)
            } else {
                dao.insert(
                    SavedRestaurantEntity(
                        restaurantId = restaurant.id,
                        name = restaurant.name,
                        rating = restaurant.rating,
                        image = restaurant.image,
                        cuisine = restaurant.cuisine,
                        price = restaurant.price,
                        badge = restaurant.badge,
                        userEmail = userEmail
                    )
                )
            }
        }
    }

    fun unsave(restaurantId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(restaurantId, userEmail)
        }
    }
}
