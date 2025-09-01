package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.GenreResponse
import com.google.wiltv.data.network.GenreService
import com.google.wiltv.data.utils.ProfileContentHelper
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreRepositoryImpl @Inject constructor(
    private val genreService: GenreService,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository
) : GenreRepository {
    override suspend fun getMovieGenre(): ApiResult<GenreResponse, DataError.Network> {
        Logger.i { "Attempting to fetch movie genres" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isMovieGenre = 1,
            isAdultGenre = contentParams.isAdultContent,
            isKidsGenre = contentParams.isKidsContent
        )
        return mapToResult(response)
    }

    override suspend fun getTvShowsGenre(): ApiResult<GenreResponse, DataError.Network> {
        Logger.i { "Attempting to fetch TV show genres" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isTvShowGenre = 1,
            isAdultGenre = contentParams.isAdultContent,
            isKidsGenre = contentParams.isKidsContent
        )
        return mapToResult(response)
    }

    override suspend fun getTvChannelGenre(): ApiResult<GenreResponse, DataError.Network> {
        Logger.i { "Attempting to fetch TV channel genres" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isTvChannelGenre = 1,
            isAdultGenre = contentParams.isAdultContent,
            isKidsGenre = contentParams.isKidsContent
        )
        return mapToResult(response)
    }

    override suspend fun getAllGenres(): ApiResult<GenreResponse, DataError.Network> {
        Logger.i { "Attempting to fetch all genres" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        val response = genreService.getGenres(
            authToken = "Bearer ${user.token}",
            isAdultGenre = contentParams.isAdultContent,
            isKidsGenre = contentParams.isKidsContent
        )
        return mapToResult(response)
    }
}