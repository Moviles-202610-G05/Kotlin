package com.example.foodgram.models.restaurants

import com.example.foodgram.views.restaurants.MapRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RestaurantRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getRestaurants(): List<MapRestaurant> {
        return try {
            // Try "Restaurants" first
            val snapshot = db.collection("restaurants")
                .get()
                .await()
            
            var list = snapshot.toObjects(MapRestaurant::class.java)


            // Filter out any invalid documents
            list.filter { it.name.isNotBlank() && (it.lat != 0.0 || it.position.geopoint.latitude != 0.0) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCategories(): List<String> {
        return try {
            val snapshot = db.collection("categories")
                .get()
                .await()
            snapshot.documents.map { it.getString("name") ?: "" }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
