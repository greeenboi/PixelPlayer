package com.theveloper.pixelplay.data.cache

import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache for streaming URLs with time-based expiration (10-minute TTL)
 */
@Singleton
class StreamUrlCache @Inject constructor() {
    
    private val cache = LruCache<String, CachedStreamUrl>(100)
    
    data class CachedStreamUrl(
        val url: String,
        val expiresAt: Long // System.currentTimeMillis() + expiration time
    )
    
    /**
     * Get cached stream URL for a track if it exists and hasn't expired
     * @param trackId The track ID
     * @return The cached stream URL or null if not found or expired
     */
    fun get(trackId: String): String? {
        val cached = cache.get(trackId) ?: return null
        
        if (System.currentTimeMillis() > cached.expiresAt) {
            cache.remove(trackId)
            return null
        }
        
        return cached.url
    }
    
    /**
     * Cache a stream URL with expiration time
     * @param trackId The track ID
     * @param url The stream URL
     * @param expiresInSeconds Time until expiration in seconds
     */
    fun put(trackId: String, url: String, expiresInSeconds: Int) {
        // Use minimum of API expiry or 10 minutes (600 seconds)
        val ttlSeconds = minOf(expiresInSeconds, 600)
        val ttlMillis = ttlSeconds * 1000L
        val expiresAt = System.currentTimeMillis() + ttlMillis
        
        cache.put(trackId, CachedStreamUrl(url, expiresAt))
    }
    
    /**
     * Clear all cached stream URLs
     */
    fun clear() {
        cache.evictAll()
    }
    
    /**
     * Remove a specific track's cached stream URL
     */
    fun remove(trackId: String) {
        cache.remove(trackId)
    }
}
