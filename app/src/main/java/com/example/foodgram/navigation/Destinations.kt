package com.example.foodgram.navigation

import kotlinx.serialization.Serializable

// Use 'object' for screens with no data
@Serializable
object Home

@Serializable
object SignUp

// Use 'data class' if you need to pass arguments (e.g., a post ID)
@Serializable
object MainFeed