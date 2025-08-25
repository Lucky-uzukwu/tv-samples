// ABOUTME: DAO interface for watchlist operations with Room database
// ABOUTME: Provides CRUD operations for WatchlistItem entities with reactive Flow support
package com.google.wiltv.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.wiltv.data.entities.WatchlistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(watchlistItem: WatchlistItem)
    
    @Delete
    suspend fun removeFromWatchlist(watchlistItem: WatchlistItem)
    
    @Query("SELECT * FROM watchlist WHERE user_id = :userId ORDER BY created_at DESC")
    fun getUserWatchlist(userId: String): Flow<List<WatchlistItem>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE user_id = :userId AND content_id = :contentId)")
    fun isInWatchlist(userId: String, contentId: Int): Flow<Boolean>
    
    @Query("DELETE FROM watchlist WHERE user_id = :userId AND content_id = :contentId")
    suspend fun removeFromWatchlistByIds(userId: String, contentId: Int)
}