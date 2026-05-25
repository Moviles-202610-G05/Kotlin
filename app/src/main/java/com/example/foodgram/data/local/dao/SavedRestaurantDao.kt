package com.example.foodgram.data.local.dao

import androidx.room.*
import com.example.foodgram.data.local.entities.SavedRestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRestaurantDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(saved: SavedRestaurantEntity)

    @Query("DELETE FROM saved_restaurants WHERE restaurantId = :restaurantId AND userEmail = :userEmail")
    suspend fun delete(restaurantId: String, userEmail: String)

    @Query("SELECT * FROM saved_restaurants WHERE userEmail = :userEmail ORDER BY savedAt DESC")
    fun getByUser(userEmail: String): Flow<List<SavedRestaurantEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_restaurants WHERE restaurantId = :restaurantId AND userEmail = :userEmail)")
    suspend fun isSaved(restaurantId: String, userEmail: String): Boolean

    @Query("SELECT COUNT(*) FROM saved_restaurants WHERE userEmail = :userEmail")
    fun countByUser(userEmail: String): Flow<Int>
}
