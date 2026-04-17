package com.example.foodgram.models.auth

import android.net.Uri

data class AuthMenuItem(
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val category: String = "",
    val imageUri: Uri? = null,
    val imageUrl: String = "",
    val restaurantName: String = ""
)
