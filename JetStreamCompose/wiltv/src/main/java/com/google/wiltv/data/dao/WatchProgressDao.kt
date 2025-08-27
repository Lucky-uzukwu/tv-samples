// ABOUTME: DAO interface for watch progress operations with Room database
// ABOUTME: Provides CRUD operations for WatchProgress entities with reactive Flow support
package com.google.wiltv.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.wiltv.data.entities.WatchProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchProgress(watchProgress: WatchProgress)
    
    @Query("SELECT * FROM watch_progress WHERE user_id = :userId AND content_id = :contentId")
    fun getWatchProgress(userId: String, contentId: Int): Flow<WatchProgress?>
    
    @Query("SELECT * FROM watch_progress WHERE user_id = :userId ORDER BY last_watched DESC LIMIT :limit")
    fun getRecentWatchProgress(userId: String, limit: Int = 20): Flow<List<WatchProgress>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM watch_progress WHERE user_id = :userId AND content_id = :contentId AND progress_ms > 0)")
    fun hasWatchProgress(userId: String, contentId: Int): Flow<Boolean>
    
    @Query("DELETE FROM watch_progress WHERE user_id = :userId AND content_id = :contentId")
    suspend fun deleteWatchProgress(userId: String, contentId: Int)
    
    @Query("DELETE FROM watch_progress WHERE user_id = :userId")
    suspend fun deleteAllUserWatchProgress(userId: String)
    
    @Query("UPDATE watch_progress SET completed = 1 WHERE user_id = :userId AND content_id = :contentId")
    suspend fun markAsCompleted(userId: String, contentId: Int)
}