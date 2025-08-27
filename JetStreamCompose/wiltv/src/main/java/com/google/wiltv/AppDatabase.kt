package com.google.wiltv

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.wiltv.data.dao.MoviesDao
import com.google.wiltv.data.dao.MovieRemoteKeyDao
import com.google.wiltv.data.dao.WatchlistDao
import com.google.wiltv.data.dao.WatchProgressDao
import com.google.wiltv.data.entities.MovieEntity
import com.google.wiltv.data.entities.WatchlistItem
import com.google.wiltv.data.entities.WatchProgress
import com.google.wiltv.data.models.MovieRemoteKey
import com.google.wiltv.data.models.converters.AnyListConverter
import com.google.wiltv.data.models.converters.CountryConverter
import com.google.wiltv.data.models.converters.GenreConverter
import com.google.wiltv.data.models.converters.LanguageConverter
import com.google.wiltv.data.models.converters.MoviePersonConverter
import com.google.wiltv.data.models.converters.StreamingProviderConverter
import com.google.wiltv.data.models.converters.SubtitleConverter

@TypeConverters(
    MoviePersonConverter::class,
    GenreConverter::class,
    CountryConverter::class,
    LanguageConverter::class,
    StreamingProviderConverter::class,
    AnyListConverter::class,
    SubtitleConverter::class
)
@Database(
    entities = [MovieEntity::class, MovieRemoteKey::class, WatchlistItem::class, WatchProgress::class],
    version = 4,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao
    abstract fun getMovieRemoteKeyDao(): MovieRemoteKeyDao
    abstract fun getWatchlistDao(): WatchlistDao
    abstract fun getWatchProgressDao(): WatchProgressDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watchlist` (
                `user_id` TEXT NOT NULL, 
                `content_id` INTEGER NOT NULL, 
                `content_type` TEXT NOT NULL, 
                `created_at` INTEGER NOT NULL, 
                PRIMARY KEY(`user_id`, `content_id`)
            )
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_watchlist_user_id` ON `watchlist` (`user_id`)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop the existing watchlist table and recreate to resolve schema mismatch
        database.execSQL("DROP TABLE IF EXISTS watchlist")
        
        // Recreate with the correct structure
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watchlist` (
                `user_id` TEXT NOT NULL, 
                `content_id` INTEGER NOT NULL, 
                `content_type` TEXT NOT NULL, 
                `created_at` INTEGER NOT NULL, 
                PRIMARY KEY(`user_id`, `content_id`)
            )
            """.trimIndent()
        )
        
        // Recreate the index
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_watchlist_user_id` ON `watchlist` (`user_id`)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create watch_progress table for video playback progress tracking
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watch_progress` (
                `user_id` TEXT NOT NULL,
                `content_id` INTEGER NOT NULL,
                `content_type` TEXT NOT NULL,
                `progress_ms` INTEGER NOT NULL,
                `duration_ms` INTEGER NOT NULL,
                `last_watched` INTEGER NOT NULL,
                `completed` INTEGER NOT NULL,
                PRIMARY KEY(`user_id`, `content_id`)
            )
            """.trimIndent()
        )
        
        // Create indexes for efficient querying
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_watch_progress_user_id` ON `watch_progress` (`user_id`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_watch_progress_last_watched` ON `watch_progress` (`last_watched`)")
    }
}