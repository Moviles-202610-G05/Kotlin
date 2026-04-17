package com.example.foodgram.models.restaurants

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName


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
    val tags: List<String> = emptyList(),
    val time: String = "",
    val position: MapPosition = MapPosition()
) {
    val location: LatLng get() = if (lat != 0.0 && long != 0.0) {
        LatLng(lat, long)
    } else {
        LatLng(position.geopoint.latitude, position.geopoint.longitude)
    }
}

data class MapPosition(
    val geohash: String = "",
    val geopoint: GeoPoint = GeoPoint(0.0, 0.0)
)


data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0,
    val category: String = "",
    val image: String = "",
    val available: Boolean = true
)

data class RestaurantReview(
    val userId: String = "",
    val username: String = "",
    val rating: Long = 0,
    val comment: String = "",
    val createdAt: Timestamp? = null
)
