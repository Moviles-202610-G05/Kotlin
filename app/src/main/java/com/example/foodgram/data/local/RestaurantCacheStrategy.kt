package com.example.foodgram.data.local

import com.example.foodgram.data.local.dao.RestaurantDao
import com.example.foodgram.data.local.entities.RestaurantEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cache-aside strategy with TTL for restaurant data.
 *
 * Mirrors Flutter's Hive adapter pattern (RestaurantAdapter) but adds explicit
 * time-based invalidation so the Map and Search screens only call Firestore when
 * the local data is genuinely stale.
 *
 * Threading: all DAO calls are dispatched to Dispatchers.IO internally, so callers
 * can invoke these methods from any coroutine context without extra withContext wrapping.
 */
class RestaurantCacheStrategy(
    private val dao: RestaurantDao,
    val ttlMillis: Long = TTL_DEFAULT
) {
    /**
     * Returns true when every row in the cache was written within [ttlMillis].
     * Uses MIN(cachedAt) so a single stale row marks the whole cache as expired.
     */
    suspend fun isFresh(): Boolean = withContext(Dispatchers.IO) {
        val oldest = dao.getOldestCachedAt() ?: return@withContext false
        if (oldest == 0L) return@withContext false          // default sentinel → never cached
        System.currentTimeMillis() - oldest < ttlMillis
    }

    /** Read all cached rows from Room. */
    suspend fun getAll(): List<RestaurantEntity> = withContext(Dispatchers.IO) {
        dao.getAllRestaurantsDirect()
    }

    /**
     * Replace the entire cache with [restaurants], stamping every row with the
     * current wall-clock time. The delete + insert runs in a single IO dispatch
     * so callers never observe a partial state.
     */
    suspend fun put(restaurants: List<RestaurantEntity>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        dao.deleteAll()
        dao.insertRestaurants(restaurants.map { it.copy(cachedAt = now) })
    }

    /** Milliseconds remaining before the cache expires (negative means already expired). */
    suspend fun remainingTtlMillis(): Long = withContext(Dispatchers.IO) {
        val oldest = dao.getOldestCachedAt() ?: return@withContext -1L
        ttlMillis - (System.currentTimeMillis() - oldest)
    }

    companion object {
        const val TTL_DEFAULT = 30 * 60 * 1000L  // 30 minutes
    }
}
