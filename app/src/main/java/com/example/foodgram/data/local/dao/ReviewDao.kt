package com.example.foodgram.data.local.dao

import androidx.room.*
import com.example.foodgram.data.local.entities.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE restaurant = :restaurantName")
    fun getReviewsByRestaurant(restaurantName: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("DELETE FROM reviews WHERE restaurant = :restaurantName")
    suspend fun deleteByRestaurant(restaurantName: String)
}
