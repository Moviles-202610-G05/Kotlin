package com.example.foodgram.models.restaurants

import com.example.foodgram.views.restaurants.MapRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun getRestaurantById(id: String): MapRestaurant? {
        return try {
            val snapshot = db.collection("restaurants").document(id)
                .get()
                .await()
            snapshot.toObject(MapRestaurant::class.java)
        } catch (e: Exception) {
            null
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

    suspend fun getRestaurantMenu(restaurantId: String): List<MenuItem> {
        return try {
            val snapshot = db.collection("restaurants").document(restaurantId)
                .collection("menu")
                .get()
                .await()
            snapshot.toObjects(MenuItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRestaurantReviews(restaurantId: String): List<RestaurantReview> {
        return try {
            val snapshot = db.collection("restaurants").document(restaurantId)
                .collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(RestaurantReview::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
