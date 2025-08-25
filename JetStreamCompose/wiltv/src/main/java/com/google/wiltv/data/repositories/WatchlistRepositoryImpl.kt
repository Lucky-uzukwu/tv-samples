// ABOUTME: Implementation of WatchlistRepository using Room database operations
// ABOUTME: Handles all watchlist CRUD operations with error handling and logging
package com.google.wiltv.data.repositories

import android.util.Log
import com.google.wiltv.data.dao.WatchlistDao
import com.google.wiltv.data.entities.WatchlistItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {

    companion object {
        private const val TAG = "WatchlistRepository"
    }

    override suspend fun addToWatchlist(userId: String, contentId: Int, contentType: String) {
        try {
            val watchlistItem = WatchlistItem(
                userId = userId,
                contentId = contentId,
                contentType = contentType,
                createdAt = System.currentTimeMillis()
            )
            watchlistDao.addToWatchlist(watchlistItem)
            Log.d(TAG, "Added to watchlist: userId=$userId, contentId=$contentId, type=$contentType")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to watchlist: userId=$userId, contentId=$contentId", e)
            throw e
        }
    }

    override suspend fun removeFromWatchlist(userId: String, contentId: Int) {
        try {
            watchlistDao.removeFromWatchlistByIds(userId, contentId)
            Log.d(TAG, "Removed from watchlist: userId=$userId, contentId=$contentId")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from watchlist: userId=$userId, contentId=$contentId", e)
            throw e
        }
    }

    override fun getUserWatchlist(userId: String): Flow<List<WatchlistItem>> {
        return watchlistDao.getUserWatchlist(userId)
    }

    override fun isInWatchlist(userId: String, contentId: Int): Flow<Boolean> {
        return watchlistDao.isInWatchlist(userId, contentId)
    }
}