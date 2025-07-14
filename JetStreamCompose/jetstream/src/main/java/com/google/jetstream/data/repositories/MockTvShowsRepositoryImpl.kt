package com.google.jetstream.data.repositories

import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.TvShowsResponse
import com.google.jetstream.data.repositories.mock.MockData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockTvShowsRepositoryImpl : TvShowsRepository {
    override fun getTvShowsToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): Flow<TvShowsResponse> = flow {
        emit(
            TvShowsResponse(
                member = listOf(MockData.getTvShow())
            )
        )
    }

    override fun getTvShowsToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<TvShowsResponse> = flow {
        emit(
            TvShowsResponse(
                member = listOf(MockData.getTvShow())
            )
        )
    }

    override fun getTvShowsToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): Flow<TvShowsResponse> = flow {
        emit(
            TvShowsResponse(
                member = listOf(MockData.getTvShow())
            )
        )
    }

    override fun getTvShowsDetails(
        token: String,
        tvShowId: String
    ): Flow<TvShow> = flow {
        emit(
            MockData.getTvShow()
        )
    }
}