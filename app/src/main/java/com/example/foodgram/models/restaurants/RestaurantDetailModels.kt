package com.example.foodgram.models.restaurants

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.PropertyName
import com.example.foodgram.models.map.PointPosition


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
    val position: PointPosition = PointPosition()
) {
    val location: LatLng get() = if (lat != 0.0 && long != 0.0) {
        LatLng(lat, long)
    } else {
        LatLng(position.geopoint.latitude, position.geopoint.longitude)
    }
}
