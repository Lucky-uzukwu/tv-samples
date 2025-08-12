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
}