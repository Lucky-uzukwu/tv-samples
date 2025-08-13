package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.data.network.TvShowsService
import com.google.wiltv.data.utils.ProfileContentHelper
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvShowsRepositoryImpl @Inject constructor(
    private val tvShowService: TvShowsService,
    private val profileRepository: ProfileRepository
) : TvShowsRepository {
    override suspend fun getTvShowsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        
        return mapToResult(tvShowService.getTvShows(
            authToken = "Bearer $token",
            showInHeroSection = 1,
            page = page,
            itemsPerPage = itemsPerPage,
            isAdultContent = contentParams.isAdultContent,
            isKidsContent = contentParams.isKidsContent
        ))
    }

    override suspend fun getTvShowsToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        
        return mapToResult(tvShowService.getTvShows(
            authToken = "Bearer $token",
            catalogId = catalogId,
            itemsPerPage = itemsPerPage,
            page = page,
            isAdultContent = contentParams.isAdultContent,
            isKidsContent = contentParams.isKidsContent
        ))
    }

    override suspend fun getTvShowsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)
        
        return mapToResult(tvShowService.getTvShows(
            authToken = "Bearer $token",
            genreId = genreId,
            itemsPerPage = itemsPerPage,
            page = page,
            isAdultContent = contentParams.isAdultContent,
            isKidsContent = contentParams.isKidsContent
        ))
    }

    override suspend fun getTvShowsDetails(
        token: String,
        tvShowId: String
    ): ApiResult<TvShow, DataError.Network> {
        return mapToResult(tvShowService.getTvShowById(
            authToken = "Bearer $token",
            tvShowId = tvShowId
        ))
    }
}