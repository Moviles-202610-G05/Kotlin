package com.example.foodgram.models.auth

import com.google.firebase.firestore.PropertyName

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val cuisine: String = "",
    val description: String = "",
    @get:PropertyName("restaurantImage") @set:PropertyName("restaurantImage") var restaurantImage: String = "",
    val price: String = "$$",
    val rating: Double = 5.0,
    @get:PropertyName("nuberReviews") @set:PropertyName("nuberReviews") var nuberReviews: Int = 0,
    val distance: String = "0 km",
    val time: String = "20-30 min",
    val lat: Double = 4.60971,
    val long: Double = -74.08175,
    val spots: Int = 100,
    val numberOfSeats: Int = 100,
    val badge: String = "NEW",
    val badge2: String = "WELCOME",
    val ownerEmail: String = "",
    val phone: String = "",
    val address: String = ""
)
