package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.network.GenreResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

class MockGenreRepositoryImpl : GenreRepository {
    override suspend fun getMovieGenre(): ApiResult<GenreResponse, DataError.Network> {
        return ApiResult.Success(
            GenreResponse(
                member = listOf(
                    Genre(
                        id = 1,
                        name = "Action",
                        isMovieGenre = true,
                        isTvShowGenre = false,
                        isAdultGenre = false,
                        isTvChannelGenre = false,
                        active = true
                    )
                ),
                totalItems = 1
            )
        )
    }

    override suspend fun getTvShowsGenre(): ApiResult<GenreResponse, DataError.Network> {
        return ApiResult.Success(
            GenreResponse(
                member = listOf(
                    Genre(
                        id = 1,
                        name = "Action",
                        isMovieGenre = false,
                        isTvShowGenre = true,
                        isAdultGenre = false,
                        isTvChannelGenre = false,
                        active = true
                    )
                ),
                totalItems = 1
            )
        )
    }

    override suspend fun getTvChannelGenre(): ApiResult<GenreResponse, DataError.Network> {
        return ApiResult.Success(
            GenreResponse(
                member = listOf(
                    Genre(
                        id = 1,
                        name = "News",
                        isMovieGenre = false,
                        isTvShowGenre = false,
                        isAdultGenre = false,
                        isTvChannelGenre = true,
                        active = true
                    ),
                    Genre(
                        id = 2,
                        name = "Sports",
                        isMovieGenre = false,
                        isTvShowGenre = false,
                        isAdultGenre = false,
                        isTvChannelGenre = true,
                        active = true
                    )
                ),
                totalItems = 2
            )
        )
    }

    override suspend fun getAllGenres(): ApiResult<GenreResponse, DataError.Network> {
        return ApiResult.Success(
            GenreResponse(
                member = listOf(
                    Genre(
                        id = 1,
                        name = "Action",
                        isMovieGenre = true,
                        isTvShowGenre = true,
                        isAdultGenre = false,
                        isTvChannelGenre = false,
                        active = true
                    ),
                    Genre(
                        id = 2,
                        name = "Comedy",
                        isMovieGenre = true,
                        isTvShowGenre = true,
                        isAdultGenre = false,
                        isTvChannelGenre = false,
                        active = true
                    ),
                    Genre(
                        id = 3,
                        name = "Drama",
                        isMovieGenre = true,
                        isTvShowGenre = true,
                        isAdultGenre = false,
                        isTvChannelGenre = false,
                        active = true
                    ),
                    Genre(
                        id = 4,
                        name = "News",
                        isMovieGenre = false,
                        isTvShowGenre = false,
                        isAdultGenre = false,
                        isTvChannelGenre = true,
                        active = true
                    ),
                    Genre(
                        id = 5,
                        name = "Sports",
                        isMovieGenre = false,
                        isTvShowGenre = false,
                        isAdultGenre = false,
                        isTvChannelGenre = true,
                        active = true
                    )
                ),
                totalItems = 5
            )
        )
    }
}