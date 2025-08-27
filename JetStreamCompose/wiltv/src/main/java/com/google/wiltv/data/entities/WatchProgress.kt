// ABOUTME: WatchProgress entity for persistent storage of user video playback progress
// ABOUTME: Stores user ID, content ID, progress timestamp, and last watched time for resume functionality
package com.google.wiltv.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "watch_progress",
    primaryKeys = ["user_id", "content_id"],
    indices = [Index(value = ["user_id"]), Index(value = ["last_watched"])]
)
data class WatchProgress(
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "content_id")
    val contentId: Int,
    @ColumnInfo(name = "content_type")
    val contentType: String, // "movie" or "tvshow"
    @ColumnInfo(name = "progress_ms")
    val progressMs: Long, // Progress in milliseconds
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long, // Total duration in milliseconds
    @ColumnInfo(name = "last_watched")
    val lastWatched: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed")
    val completed: Boolean = false // Mark as completed if watched to the end
)