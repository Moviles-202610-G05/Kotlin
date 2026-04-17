package com.example.foodgram.models.auth

import com.google.firebase.firestore.PropertyName

data class User(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val roll: UserRole = UserRole.ESTUDIANTE,
    val universityId: String? = null,
    val preferences: List<String> = emptyList(),
    val carrier: String = "",
    val ordersCount: Int = 0,
    val reviewsCount: Int = 0,
    val savedCount: Int = 0,
    val restaurantName: String? = null // Link to their restaurant if they are an owner
)

enum class UserRole {
    @PropertyName("ESTUDIANTE") ESTUDIANTE,
    @PropertyName("RESTAURANTE") RESTAURANTE,
    @PropertyName("ADMIN") ADMIN
}