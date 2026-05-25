package com.example.foodgram.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_restaurants",
    indices = [Index(value = ["restaurantId", "userEmail"], unique = true)]
)
data class SavedRestaurantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: String,
    val name: String,
    val rating: Double,
    val image: String,
    val cuisine: String,
    val price: String,
    val badge: String,
    val userEmail: String,
    val savedAt: Long = System.currentTimeMillis()
)
