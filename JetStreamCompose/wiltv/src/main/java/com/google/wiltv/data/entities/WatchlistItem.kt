// ABOUTME: WatchlistItem entity for persistent storage of user watchlist data
// ABOUTME: Stores user ID, content ID, content type, and timestamp for watchlist management
package com.google.wiltv.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watchlist",
    primaryKeys = ["user_id", "content_id"],
    indices = [Index(value = ["user_id"])]
)
data class WatchlistItem(
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "content_id")
    val contentId: Int,
    @ColumnInfo(name = "content_type")
    val contentType: String, // "movie" or "tvshow"
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)