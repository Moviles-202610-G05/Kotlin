package com.example.foodgram.models.tracker

data class MealHistoryItem(
    val id: String = "",
    val userEmail: String = "",
    val timestamp: String = "",
    val dishName: String = "",
    val totalCalories: Double = 0.0,
    val totalProteinG: Double = 0.0,
    val totalCarbsG: Double = 0.0,
    val totalFatG: Double = 0.0,
    val imagePath: String = ""
)
