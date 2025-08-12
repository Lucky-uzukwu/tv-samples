package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.data.network.TvShowsService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvShowsRepositoryImpl @Inject constructor(
    private val tvShowService: TvShowsService,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : TvShowsRepository {
    override suspend fun getTvShowsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        val user = userRepository.getUser() ?: return ApiResult.Error(
            error = DataError.Network.UNAUTHORIZED,
            message = "User not found"
        )
        
        return mapToResult(tvShowService.getTvShows(
            authToken = "Bearer $token",
            showInHeroSection = 1,
            page = page,
            itemsPerPage = itemsPerPage
        ))
    }

    override suspend fun getTvShowsToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        val user = userRepository.getUser() ?: return ApiResult.Error(
            error = DataError.Network.UNAUTHORIZED,
            message = "User not found"
        )
        
        return mapToResult(tvShowService.getTvShows(
            authToken = "Bearer $token",
            catalogId = catalogId,
            itemsPerPage = itemsPerPage,
            page = page
        ))
    }

    override suspend fun getTvShowsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        val user = userRepository.getUser() ?: return ApiResult.Error(
            error = DataError.Network.UNAUTHORIZED,
            message = "User not found"
        )
        
        return mapToResult(tvShowService.getTvShows(
            authToken = "Bearer $token",
            genreId = genreId,
            itemsPerPage = itemsPerPage,
            page = page
        ))
    }

    override suspend fun getTvShowsDetails(
        token: String,
        tvShowId: String
    ): ApiResult<TvShow, DataError.Network> {
        val user = userRepository.getUser() ?: return ApiResult.Error(
            error = DataError.Network.UNAUTHORIZED,
            message = "User not found"
        )
        
        return mapToResult(tvShowService.getTvShowById(
            authToken = "Bearer $token",
            tvShowId = tvShowId
        ))
    }
}