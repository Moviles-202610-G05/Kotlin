package com.example.foodgram.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val badge: String,
    val badge2: String?,
    val cuisine: String,
    val description: String,
    val direction: String,
    val distance: String,
    val image: String,
    val lat: Double,
    val long: Double,
    val nuberReviews: Long,
    val price: String,
    val spots: Long,
    val spotsA: Long,
    val numberOfSeats: Long,
    val seatsOccupied: Long,
    val tags: List<String>,
    val time: String,
    val cachedAt: Long = 0L
)
