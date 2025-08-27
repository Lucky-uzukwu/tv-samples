// ABOUTME: Data model representing an item in the Continue Watching list
// ABOUTME: Combines movie details with watch progress for resume functionality
package com.google.wiltv.data.models

import com.google.wiltv.data.entities.WatchProgress

data class ContinueWatchingItem(
    val movie: MovieNew,
    val watchProgress: WatchProgress
) {
    val progressPercentage: Float
        get() = if (watchProgress.durationMs > 0) {
            watchProgress.progressMs.toFloat() / watchProgress.durationMs.toFloat()
        } else 0f
        
    val canResume: Boolean
        get() = watchProgress.progressMs > 0 && !watchProgress.completed
}