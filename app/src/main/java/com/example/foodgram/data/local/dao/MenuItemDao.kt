package com.example.foodgram.data.local.dao

import androidx.room.*
import com.example.foodgram.data.local.entities.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE restaurant = :restaurantName")
    fun getMenuItemsByRestaurant(restaurantName: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Query("DELETE FROM menu_items WHERE restaurant = :restaurantName")
    suspend fun deleteByRestaurant(restaurantName: String)
}
