package com.example.foodgram.models.auth

data class Student(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val roll: UserRole = UserRole.ESTUDIANTE,
    val universityId: String = "",
    val preferences: List<String> = emptyList(),
    val carrier: String = "",
    val ordersCount: Int = 0,
    val reviewsCount: Int = 0,
    val savedCount: Int = 0
)
