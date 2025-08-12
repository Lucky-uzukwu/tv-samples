package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.data.repositories.mock.MockData
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

class MockTvShowsRepositoryImpl : TvShowsRepository {
    override suspend fun getTvShowsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        return ApiResult.Success(
            TvShowsResponse(
                member = listOf(MockData.getTvShow())
            )
        )
    }

    override suspend fun getTvShowsToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        return ApiResult.Success(
            TvShowsResponse(
                member = listOf(MockData.getTvShow())
            )
        )
    }

    override suspend fun getTvShowsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvShowsResponse, DataError.Network> {
        return ApiResult.Success(
            TvShowsResponse(
                member = listOf(MockData.getTvShow())
            )
        )
    }

    override suspend fun getTvShowsDetails(
        token: String,
        tvShowId: String
    ): ApiResult<TvShow, DataError.Network> {
        return ApiResult.Success(
            MockData.getTvShow()
        )
    }
}