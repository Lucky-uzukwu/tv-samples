package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.MovieSearchResponse
import com.google.wiltv.data.network.ShowSearchResponse
import com.google.wiltv.data.network.TvChannelsResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    suspend fun searchMoviesByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieSearchResponse, DataError.Network>

    suspend fun searchTvShowsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<ShowSearchResponse, DataError.Network>

    suspend fun searchTvChannelsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvChannelsResponse, DataError.Network>
}