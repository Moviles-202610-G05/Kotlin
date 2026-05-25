package com.example.foodgram.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.foodgram.data.local.entities.PendingMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingMealDao {
    @Insert
    suspend fun insert(meal: PendingMealEntity): Long

    @Query("SELECT * FROM pending_meals WHERE synced = 0 ORDER BY id ASC")
    suspend fun getPending(): List<PendingMealEntity>

    @Query("SELECT COUNT(*) FROM pending_meals WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE pending_meals SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)

    @Query("DELETE FROM pending_meals WHERE synced = 1")
    suspend fun deleteSynced()
}
