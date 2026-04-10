package com.example.foodgram.models.restaurants

import com.google.firebase.Timestamp

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
