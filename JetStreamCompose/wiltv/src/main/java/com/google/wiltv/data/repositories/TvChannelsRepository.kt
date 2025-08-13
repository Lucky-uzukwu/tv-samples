package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.TvChannelsResponse
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

interface TvChannelsRepository {
    suspend fun getTvChannelsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int,
    ): ApiResult<TvChannelsResponse, DataError.Network>

    suspend fun getTvChannelsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvChannelsResponse, DataError.Network>

    suspend fun getTvChannels(
        token: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvChannelsResponse, DataError.Network>
}
