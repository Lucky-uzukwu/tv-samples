package com.google.jetstream.data.repositories

import com.google.jetstream.data.models.Genre
import kotlinx.coroutines.flow.Flow

interface GenreRepository {
    fun getMovieGenre(token: String): Flow<List<Genre>>
}
