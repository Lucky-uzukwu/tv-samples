// ABOUTME: Manager for tracking video playback progress with ExoPlayer integration
// ABOUTME: Handles automatic progress saving, restoration, and completion detection
package com.google.wiltv.presentation.screens.videoPlayer.components

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.wiltv.data.repositories.WatchProgressRepository
import com.google.wiltv.data.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchProgressManager @Inject constructor(
    private val watchProgressRepository: WatchProgressRepository,
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "WatchProgressManager"
        private const val PROGRESS_SAVE_INTERVAL_MS = 10_000L // Save every 10 seconds
        private const val COMPLETION_THRESHOLD = 0.9f // 90% watched = completed
        private const val MIN_WATCH_TIME_MS = 30_000L // Minimum 30 seconds to save progress
    }

    private var progressSaveJob: Job? = null
    private var currentContentId: Int? = null
    private var currentContentType: String? = null
    private var isTracking = false

    fun startTracking(
        player: ExoPlayer,
        contentId: Int,
        contentType: String,
        scope: CoroutineScope
    ) {
        Log.d(TAG, "Starting progress tracking: contentId=$contentId, type=$contentType")
        
        currentContentId = contentId
        currentContentType = contentType
        isTracking = true

        // Restore existing progress
        scope.launch {
            restoreProgress(player, contentId)
        }

        // Start periodic progress saving
        startProgressSaving(player, scope)

        // Add player listener for events
        val playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Playback ended - marking as completed")
                        scope.launch {
                            markAsCompleted(contentId, contentType)
                        }
                    }
                    Player.STATE_READY -> {
                        Log.d(TAG, "Player ready - duration: ${player.duration}ms")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying && isTracking) {
                    // Save progress when playback is paused/stopped
                    scope.launch {
                        saveCurrentProgress(player, contentId, contentType)
                    }
                }
            }
        }

        player.addListener(playerListener)
    }

    fun stopTracking(player: ExoPlayer) {
        Log.d(TAG, "Stopping progress tracking")
        isTracking = false
        progressSaveJob?.cancel()
        
        // Save final progress before stopping
        currentContentId?.let { contentId ->
            currentContentType?.let { contentType ->
                // Note: This needs to be called from a coroutine scope
                // The calling code should handle the final save
            }
        }
        
        currentContentId = null
        currentContentType = null
    }

    private fun startProgressSaving(player: ExoPlayer, scope: CoroutineScope) {
        progressSaveJob?.cancel()
        progressSaveJob = scope.launch {
            while (isTracking && player.isPlaying) {
                delay(PROGRESS_SAVE_INTERVAL_MS)
                if (isTracking && currentContentId != null && currentContentType != null) {
                    saveCurrentProgress(player, currentContentId!!, currentContentType!!)
                }
            }
        }
    }

    private suspend fun saveCurrentProgress(player: ExoPlayer, contentId: Int, contentType: String) {
        try {
            val currentPosition = player.currentPosition
            val duration = player.duration
            
            // Only save if we have valid duration and minimum watch time
            if (duration > 0 && currentPosition > MIN_WATCH_TIME_MS) {
                val userId = userRepository.userId.firstOrNull()
                if (userId != null) {
                    val progressPercentage = currentPosition.toFloat() / duration.toFloat()
                    val isCompleted = progressPercentage >= COMPLETION_THRESHOLD
                    
                    watchProgressRepository.saveWatchProgress(
                        userId = userId,
                        contentId = contentId,
                        contentType = contentType,
                        progressMs = currentPosition,
                        durationMs = duration,
                        completed = isCompleted
                    )
                    
                    Log.d(TAG, "Progress saved: ${(progressPercentage * 100).toInt()}% (${currentPosition}ms/${duration}ms)")
                    
                    if (isCompleted) {
                        Log.d(TAG, "Content marked as completed due to progress threshold")
                    }
                } else {
                    Log.w(TAG, "No user ID available for saving progress")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving progress", e)
        }
    }

    private suspend fun restoreProgress(player: ExoPlayer, contentId: Int) {
        try {
            val userId = userRepository.userId.firstOrNull()
            if (userId != null) {
                val watchProgress = watchProgressRepository.getWatchProgress(userId, contentId).firstOrNull()
                watchProgress?.let { progress ->
                    if (!progress.completed && progress.progressMs > MIN_WATCH_TIME_MS) {
                        Log.d(TAG, "Restoring progress: ${progress.progressMs}ms")
                        player.seekTo(progress.progressMs)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring progress", e)
        }
    }

    private suspend fun markAsCompleted(contentId: Int, contentType: String) {
        try {
            val userId = userRepository.userId.firstOrNull()
            if (userId != null) {
                watchProgressRepository.markAsCompleted(userId, contentId)
                Log.d(TAG, "Marked as completed: contentId=$contentId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as completed", e)
        }
    }
}