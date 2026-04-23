package com.example.foodgram.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: String,
    val category: String,
    val description: String,
    val image: String,
    val restaurant: String,
    val inStock: Boolean
)
