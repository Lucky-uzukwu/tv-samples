package com.google.jetstream

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.jetstream.data.dao.MoviesDao
import com.google.jetstream.data.dao.MovieRemoteKeyDao
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.MovieRemoteKey
import com.google.jetstream.data.models.converters.AnyListConverter
import com.google.jetstream.data.models.converters.CountryConverter
import com.google.jetstream.data.models.converters.GenreConverter
import com.google.jetstream.data.models.converters.LanguageConverter
import com.google.jetstream.data.models.converters.MoviePersonConverter
import com.google.jetstream.data.models.converters.StreamingProviderConverter
import com.google.jetstream.data.models.converters.SubtitleConverter

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
    entities = [MovieNew::class, MovieRemoteKey::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao
    abstract fun getMovieRemoteKeyDao(): MovieRemoteKeyDao
}