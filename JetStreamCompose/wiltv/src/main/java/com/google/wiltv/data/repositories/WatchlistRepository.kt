// ABOUTME: Repository interface for watchlist operations and data management
// ABOUTME: Defines contract for adding, removing, and querying watchlist data
package com.google.wiltv.data.repositories

import com.google.wiltv.data.entities.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    
    suspend fun addToWatchlist(userId: String, contentId: Int, contentType: String)
    
    suspend fun removeFromWatchlist(userId: String, contentId: Int)
    
    fun getUserWatchlist(userId: String): Flow<List<WatchlistItem>>
    
    fun isInWatchlist(userId: String, contentId: Int): Flow<Boolean>
}