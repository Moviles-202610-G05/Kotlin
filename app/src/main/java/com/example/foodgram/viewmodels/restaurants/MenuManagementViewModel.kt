package com.example.foodgram.viewmodels.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.restaurants.MenuItem
import com.example.foodgram.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest

data class MenuManagementUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val emptySeats: Int = 0,
    val totalSeats: Int = 0,
    val occupiedSeats: Int = 0,
    val isLoading: Boolean = false,
    val restaurantId: String? = null,
    val restaurantName: String? = null
)

class MenuManagementViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(MenuManagementUiState())
    val uiState: StateFlow<MenuManagementUiState> = _uiState
    private var restaurantListener: ListenerRegistration? = null

    init {
        loadRestaurantData()
    }

    override fun onCleared() {
        restaurantListener?.remove()
        super.onCleared()
    }

    private fun loadRestaurantData() {
        val userEmail = UserSession.currentUserEmail ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Find the user first to get their restaurantName
                val userSnapshot = firestore.collection("user")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()
                
                val userDoc = userSnapshot.documents.firstOrNull()
                val restaurantNameFromUser = userDoc?.getString("restaurantName")

                if (restaurantNameFromUser != null) {
                    val restaurantSnapshot = firestore.collection("restaurants")
                        .whereEqualTo("name", restaurantNameFromUser)
                        .get()
                        .await()
                    
                    val restaurantDoc = restaurantSnapshot.documents.firstOrNull()
                    if (restaurantDoc != null) {
                        setupRealtimeRestaurantListener(restaurantDoc.id)
                        loadMenuItems(restaurantNameFromUser)
                    }
                } else {
                    // Fallback to ownerEmail
                    val restaurantSnapshot = firestore.collection("restaurants")
                        .whereEqualTo("ownerEmail", userEmail)
                        .get()
                        .await()
                    
                    val restaurantDoc = restaurantSnapshot.documents.firstOrNull()
                    if (restaurantDoc != null) {
                        val name = restaurantDoc.getString("name") ?: ""
                        setupRealtimeRestaurantListener(restaurantDoc.id)
                        loadMenuItems(name)
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading restaurant data: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun setupRealtimeRestaurantListener(restaurantId: String) {
        restaurantListener?.remove()
        restaurantListener = firestore.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                
                val emptySeats = snapshot.getLong("spotsA")?.toInt() ?: 0
                val totalSeats = snapshot.getLong("numberOfSeats")?.toInt() ?: 0
                val occupiedSeats = snapshot.getLong("seatsOccupied")?.toInt() ?: 0
                val name = snapshot.getString("name") ?: ""

                _uiState.value = _uiState.value.copy(
                    restaurantId = restaurantId,
                    restaurantName = name,
                    emptySeats = emptySeats,
                    totalSeats = totalSeats,
                    occupiedSeats = occupiedSeats
                )
            }
    }

    private fun loadMenuItems(restaurantName: String) {
        viewModelScope.launch {
            try {
                val menuSnapshot = firestore.collection("menu")
                    .whereEqualTo("restaurant", restaurantName)
                    .get()
                    .await()
                
                val items = menuSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
                }
                
                _uiState.value = _uiState.value.copy(menuItems = items)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateStock(itemId: String, inStock: Boolean) {
        val availability = if (inStock) 1 else 0
        viewModelScope.launch {
            try {
                firestore.collection("menu").document(itemId)
                    .update("availability", availability)
                    .await()
                
                val updatedItems = _uiState.value.menuItems.map {
                    if (it.id == itemId) it.copy(availability = availability) else it
                }
                _uiState.value = _uiState.value.copy(menuItems = updatedItems)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updatePrice(itemId: String, newPrice: String) {
        viewModelScope.launch {
            try {
                firestore.collection("menu").document(itemId)
                    .update("price", newPrice)
                    .await()
                
                val updatedItems = _uiState.value.menuItems.map {
                    if (it.id == itemId) it.copy(price = newPrice) else it
                }
                _uiState.value = _uiState.value.copy(menuItems = updatedItems)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("menu").document(itemId)
                    .delete()
                    .await()
                
                val updatedItems = _uiState.value.menuItems.filter { it.id != itemId }
                _uiState.value = _uiState.value.copy(menuItems = updatedItems)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateOccupiedSeats(newCount: Int) {
        val uiStateValue = _uiState.value
        val restaurantId = uiStateValue.restaurantId ?: return
        if (newCount < 0 || newCount > uiStateValue.totalSeats) return
        
        val newAvailable = uiStateValue.totalSeats - newCount

        viewModelScope.launch {
            try {
                firestore.collection("restaurants").document(restaurantId)
                    .update(
                        "seatsOccupied", newCount,
                        "spotsA", newAvailable
                    )
                    .await()
                
                _uiState.value = _uiState.value.copy(
                    occupiedSeats = newCount,
                    emptySeats = newAvailable
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateEmptySeats(newCount: Int) {
        val uiStateValue = _uiState.value
        val restaurantId = uiStateValue.restaurantId ?: return
        if (newCount < 0 || newCount > uiStateValue.totalSeats) return
        
        val newOccupied = uiStateValue.totalSeats - newCount

        viewModelScope.launch {
            try {
                firestore.collection("restaurants").document(restaurantId)
                    .update(
                        "spotsA", newCount,
                        "seatsOccupied", newOccupied
                    )
                    .await()
                
                _uiState.value = _uiState.value.copy(
                    emptySeats = newCount,
                    occupiedSeats = newOccupied
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
