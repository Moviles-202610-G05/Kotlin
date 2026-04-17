package com.example.foodgram.models.ui

import android.net.Uri
import com.example.foodgram.views.auth.MenuItem

data class RestaurantRegisterForm(
    val ownerName: String = "",
    val restaurantName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val username: String = "",
    val password: String = "",
    val cuisineType: String = "",
    val description: String = "",
    val priceRange: String = "$$",
    val restaurantImageUri: Uri? = null
)
