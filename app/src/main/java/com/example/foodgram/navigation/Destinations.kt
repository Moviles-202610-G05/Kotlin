package com.example.foodgram.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class RestaurantDetail(val id: String)

@Serializable
object Search

@Serializable
object Profile

@Serializable
object Menu

@Serializable
object Tracker

@Serializable
data class RestaurantsMap(val restaurantId: String? = null)

@Serializable
object Map

@Serializable
object Login

@Serializable
object SignUp

@Serializable
object Register

@Serializable
object StudentRegister

@Serializable
object RestaurantRegister

@Serializable
object MenuRegister

@Serializable
object MainFeed

@Serializable
object PersonalInfo

@Serializable
object ForgotPassword

@Serializable
object NutritionGoals


@Serializable
data class UserReviews(val userId: String)

@Serializable
object Saved

@Serializable
data class PostComments(val postId: String)