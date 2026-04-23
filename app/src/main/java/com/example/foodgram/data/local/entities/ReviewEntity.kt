package com.example.foodgram.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val restaurant: String,
    val name: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val avatar: String,
    val avatarColor: String,
    val createdAt: Long? // Timestamp as long
)
