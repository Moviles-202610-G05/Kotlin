package com.example.foodgram.data.local.dao

import androidx.room.*
import com.example.foodgram.data.local.entities.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants")
    suspend fun getAllRestaurantsDirect(): List<RestaurantEntity>

    @Query("SELECT * FROM restaurants WHERE id = :id")
    suspend fun getRestaurantById(id: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Query("DELETE FROM restaurants")
    suspend fun deleteAll()

    // Returns the oldest cachedAt timestamp in the table.
    // MIN means: if even the oldest row is within TTL, the whole cache is fresh.
    @Query("SELECT MIN(cachedAt) FROM restaurants")
    suspend fun getOldestCachedAt(): Long?
}
