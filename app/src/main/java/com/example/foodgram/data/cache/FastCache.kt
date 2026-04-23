package com.example.foodgram.data.cache

import android.util.LruCache

object FastCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory * 15 / 100 // 15% of available RAM

    private val cache = object : LruCache<String, Any>(cacheSize) {
        override fun sizeOf(key: String, value: Any): Int {
            return 1 
        }
    }

    fun put(key: String, value: Any) {
        cache.put(key, value)
    }

    fun get(key: String): Any? {
        return cache.get(key)
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun clear() {
        cache.evictAll()
    }
}
