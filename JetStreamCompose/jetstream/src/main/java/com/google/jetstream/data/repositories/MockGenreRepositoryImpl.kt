package com.google.jetstream.data.repositories

import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.network.Catalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockGenreRepositoryImpl : GenreRepository {
    override fun getMovieGenre(): Flow<List<Genre>> = flow {
        emit(
            listOf(
                Genre(
                    id = 1,
                    name = "Action",
                    isMovieGenre = true,
                    isTvShowGenre = false,
                    isAdultGenre = false,
                    isTvChannelGenre = false,
                    active = true
                )
            )
        )
    }

    override fun getTvShowsGenre(): Flow<List<Genre>> = flow {
        emit(
            listOf(
                Genre(
                    id = 1,
                    name = "Action",
                    isMovieGenre = false,
                    isTvShowGenre = true,
                    isAdultGenre = false,
                    isTvChannelGenre = false,
                    active = true
                )
            )
        )
    }
}