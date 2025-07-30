package com.google.wiltv

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.google.wiltv.data.dao.MoviesDao
import com.google.wiltv.data.dao.MovieRemoteKeyDao
import com.google.wiltv.data.entities.MovieEntity
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
    entities = [MovieEntity::class, MovieRemoteKey::class],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getMoviesDao(): MoviesDao
    abstract fun getMovieRemoteKeyDao(): MovieRemoteKeyDao
}