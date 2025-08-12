package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.GenreResponse
import com.google.wiltv.data.network.GenreService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreRepositoryImpl @Inject constructor(
    private val genreService: GenreService,
    private val userRepository: UserRepository
) : GenreRepository {
    override suspend fun getMovieGenre(): ApiResult<GenreResponse, DataError.Network> {
        Logger.i { "Attempting to fetch movie genres" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isMovieGenre = 1
        )
        return mapToResult(response)
    }

    override suspend fun getTvShowsGenre(): ApiResult<GenreResponse, DataError.Network> {
        Logger.i { "Attempting to fetch TV show genres" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isTvShowGenre = 1
        )
        return mapToResult(response)
    }
}