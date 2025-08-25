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
import com.google.wiltv.data.entities.MovieEntity
import com.google.wiltv.data.entities.WatchlistItem
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
    entities = [MovieEntity::class, MovieRemoteKey::class, WatchlistItem::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao
    abstract fun getMovieRemoteKeyDao(): MovieRemoteKeyDao
    abstract fun getWatchlistDao(): WatchlistDao
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