package com.example.foodgram.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.foodgram.data.local.dao.*
import com.example.foodgram.data.local.entities.*

@Database(
    entities = [
        RestaurantEntity::class,
        MenuItemEntity::class,
        PostEntity::class,
        ReviewEntity::class,
        PendingMealEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun postDao(): PostDao
    abstract fun reviewDao(): ReviewDao
    abstract fun pendingMealDao(): PendingMealDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Adds cachedAt column; existing rows get 0 (treated as "never cached" → stale immediately)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE restaurants ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodgram_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
