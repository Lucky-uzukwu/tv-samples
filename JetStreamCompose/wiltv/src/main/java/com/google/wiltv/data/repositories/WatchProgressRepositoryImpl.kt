// ABOUTME: Implementation of WatchProgressRepository using Room database operations
// ABOUTME: Handles all watch progress CRUD operations with error handling and logging
package com.google.wiltv.data.repositories

import android.util.Log
import com.google.wiltv.data.dao.WatchProgressDao
import com.google.wiltv.data.entities.WatchProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchProgressRepositoryImpl @Inject constructor(
    private val watchProgressDao: WatchProgressDao
) : WatchProgressRepository {

    companion object {
        private const val TAG = "WatchProgressRepository"
    }

    override suspend fun saveWatchProgress(
        userId: String,
        contentId: Int,
        contentType: String,
        progressMs: Long,
        durationMs: Long,
        completed: Boolean
    ) {
        try {
            val watchProgress = WatchProgress(
                userId = userId,
                contentId = contentId,
                contentType = contentType,
                progressMs = progressMs,
                durationMs = durationMs,
                lastWatched = System.currentTimeMillis(),
                completed = completed
            )
            watchProgressDao.saveWatchProgress(watchProgress)
            Log.d(TAG, "Saved watch progress: userId=$userId, contentId=$contentId, progress=${progressMs}ms/${durationMs}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving watch progress: userId=$userId, contentId=$contentId", e)
            throw e
        }
    }

    override fun getWatchProgress(userId: String, contentId: Int): Flow<WatchProgress?> {
        return watchProgressDao.getWatchProgress(userId, contentId)
    }

    override fun getRecentWatchProgress(userId: String, limit: Int): Flow<List<WatchProgress>> {
        return watchProgressDao.getRecentWatchProgress(userId, limit)
    }

    override fun hasWatchProgress(userId: String, contentId: Int): Flow<Boolean> {
        return watchProgressDao.hasWatchProgress(userId, contentId)
    }

    override suspend fun deleteWatchProgress(userId: String, contentId: Int) {
        try {
            watchProgressDao.deleteWatchProgress(userId, contentId)
            Log.d(TAG, "Deleted watch progress: userId=$userId, contentId=$contentId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting watch progress: userId=$userId, contentId=$contentId", e)
            throw e
        }
    }

    override suspend fun markAsCompleted(userId: String, contentId: Int) {
        try {
            watchProgressDao.markAsCompleted(userId, contentId)
            Log.d(TAG, "Marked as completed: userId=$userId, contentId=$contentId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as completed: userId=$userId, contentId=$contentId", e)
            throw e
        }
    }
}