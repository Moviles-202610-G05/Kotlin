package com.example.foodgram.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val username: String,
    val userId: String,
    val restaurantName: String,
    val restaurantId: String,
    val profilePhoto: String,
    val description: String,
    val photoUrl: String,
    val likesCount: Long,
    val commentsCount: Long,
    val createdAt: Long?, // Timestamp as long
    val isLiked: Boolean
)
