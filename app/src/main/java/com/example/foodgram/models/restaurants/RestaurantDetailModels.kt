package com.example.foodgram.models.restaurants

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.PropertyName
import com.example.foodgram.models.map.PointPosition
import com.google.firebase.Timestamp

data class MapRestaurant(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val badge: String = "",
    val badge2: String? = null,
    val cuisine: String = "",
    val description: String = "",
    val direction: String = "",
    val distance: String = "",
    val image: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
    @get:PropertyName("nuberReviews") @set:PropertyName("nuberReviews") var nuberReviews: Long = 0,
    val price: String = "",
    val spots: Long = 0,
    val spotsA: Long = 0,
    val numberOfSeats: Long = 0,
    val seatsOccupied: Long = 0,
    val tags: List<String> = emptyList(),
    val time: String = "",
    val position: PointPosition = PointPosition()
) {
    val location: LatLng get() = if (lat != 0.0 && long != 0.0) {
        LatLng(lat, long)
    } else {
        LatLng(position.geopoint.latitude, position.geopoint.longitude)
    }
}

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val description: String = "",
    val image: String = "",
    val imageUri: android.net.Uri? = null,
    val restaurant: String = "",
    val availability: Int = 1, // 1 for inStock, 0 for outOfStock
    val isUploading: Boolean = false
) {
    val inStock: Boolean get() = availability == 1
}

data class ReviewRestaurant(
    val id: String = "",
    val restaurant: String = "",
    val userId: String = "",
    val name: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val date: String = "",
    val avatar: String = "",
    val avatarColor: String = "#CCCCCC",
    val createdAt: Timestamp? = null
)