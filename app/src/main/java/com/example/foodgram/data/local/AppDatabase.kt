package com.example.foodgram.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.foodgram.data.local.dao.*
import com.example.foodgram.data.local.entities.*

@Database(
    entities = [
        RestaurantEntity::class,
        MenuItemEntity::class,
        PostEntity::class,
        ReviewEntity::class,
        PendingMealEntity::class,
        SavedRestaurantEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun postDao(): PostDao
    abstract fun reviewDao(): ReviewDao
    abstract fun pendingMealDao(): PendingMealDao
    abstract fun savedRestaurantDao(): SavedRestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Adds cachedAt column; existing rows get 0 (treated as "never cached" → stale immediately)
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE restaurants ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saved_restaurants` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`restaurantId` TEXT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`rating` REAL NOT NULL, " +
                    "`image` TEXT NOT NULL, " +
                    "`cuisine` TEXT NOT NULL, " +
                    "`price` TEXT NOT NULL, " +
                    "`badge` TEXT NOT NULL, " +
                    "`userEmail` TEXT NOT NULL, " +
                    "`savedAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                    "`index_saved_restaurants_restaurantId_userEmail` " +
                    "ON `saved_restaurants` (`restaurantId`, `userEmail`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "foodgram_database"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
