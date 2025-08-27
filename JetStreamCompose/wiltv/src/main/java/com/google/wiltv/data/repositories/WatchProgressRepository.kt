// ABOUTME: Repository interface for watch progress operations and data management
// ABOUTME: Defines contract for saving, retrieving, and managing video playback progress
package com.google.wiltv.data.repositories

import com.google.wiltv.data.entities.WatchProgress
import kotlinx.coroutines.flow.Flow

interface WatchProgressRepository {
    
    suspend fun saveWatchProgress(
        userId: String, 
        contentId: Int, 
        contentType: String,
        progressMs: Long, 
        durationMs: Long,
        completed: Boolean = false
    )
    
    fun getWatchProgress(userId: String, contentId: Int): Flow<WatchProgress?>
    
    fun getRecentWatchProgress(userId: String, limit: Int = 20): Flow<List<WatchProgress>>
    
    fun hasWatchProgress(userId: String, contentId: Int): Flow<Boolean>
    
    suspend fun deleteWatchProgress(userId: String, contentId: Int)
    
    suspend fun markAsCompleted(userId: String, contentId: Int)
}