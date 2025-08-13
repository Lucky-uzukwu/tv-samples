package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.TvChannelsResponse
import com.google.wiltv.data.network.TvChannelService
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.data.utils.ProfileContentHelper
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvChannelsRepositoryImpl @Inject constructor(
    private val tvChannelService: TvChannelService,
    private val profileRepository: ProfileRepository
) : TvChannelsRepository {
    override suspend fun getTvChannelsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<TvChannelsResponse, DataError.Network> {
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)

        return mapToResult(
            tvChannelService.getTvChannels(
                authToken = "Bearer $token",
                showInHeroSection = 1,
                page = page,
                itemsPerPage = itemsPerPage,
                isAdultContent = contentParams.isAdultContent,
                isKidsContent = contentParams.isKidsContent
            )
        )
    }


    override suspend fun getTvChannelsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvChannelsResponse, DataError.Network> {
        val selectedProfile = profileRepository.getSelectedProfile().firstOrNull()
        val contentParams = ProfileContentHelper.getContentFilterParams(selectedProfile)

        return mapToResult(
            tvChannelService.getTvChannels(
                authToken = "Bearer $token",
                genreId = genreId,
                itemsPerPage = itemsPerPage,
                page = page,
                isAdultContent = contentParams.isAdultContent,
                isKidsContent = contentParams.isKidsContent
            )
        )
    }
}