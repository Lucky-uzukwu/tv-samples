package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.Genre
import kotlinx.coroutines.flow.Flow

interface GenreRepository {
    fun getMovieGenre(): Flow<List<Genre>>
    fun getTvShowsGenre(): Flow<List<Genre>>
}
