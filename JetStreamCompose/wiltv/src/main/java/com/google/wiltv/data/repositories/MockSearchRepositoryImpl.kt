package com.google.wiltv.data.repositories

import com.google.wiltv.data.models.Country
import com.google.wiltv.data.models.Episode
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.Language
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.MoviePerson
import com.google.wiltv.data.models.Person
import com.google.wiltv.data.models.PersonType
import com.google.wiltv.data.models.Season
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.models.Video
import com.google.wiltv.data.network.MovieSearchResponse
import com.google.wiltv.data.network.ShowSearchResponse
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.network.TvChannelsResponse
import com.google.wiltv.data.repositories.mock.MockData
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockSearchRepositoryImpl : SearchRepository {
    override suspend fun searchMoviesByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieSearchResponse, DataError.Network> {
        return ApiResult.Success(
            MockData.getMovieSearchResponse()
        )
    }

    override suspend fun searchTvShowsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<ShowSearchResponse, DataError.Network> {
        return ApiResult.Success(
            MockData.getTvShowSearchResponse()
        )
    }

    override suspend fun searchTvChannelsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<TvChannelsResponse, DataError.Network> {
        // Create a mock TV channels response for search
        val mockChannels = listOf(
            TvChannel(
                id = 1,
                name = "CNN",
                showInHeroSection = false,
                isAdultContent = false,
                isKidsContent = false,
                priority = 1,
                logoPath = "/cnn-logo.png",
                active = true,
                logoUrl = "https://example.com/cnn-logo.png",
                playLink = "https://example.com/cnn-stream",
                language = "English",
                genres = listOf(),
                countries = listOf()
            ),
            TvChannel(
                id = 2,
                name = "BBC",
                showInHeroSection = false,
                isAdultContent = false,
                isKidsContent = false,
                priority = 2,
                logoPath = "/bbc-logo.png",
                active = true,
                logoUrl = "https://example.com/bbc-logo.png",
                playLink = "https://example.com/bbc-stream",
                language = "English",
                genres = listOf(),
                countries = listOf()
            )
        )
        
        return ApiResult.Success(
            TvChannelsResponse(
                member = mockChannels,
                totalItems = mockChannels.size
            )
        )
    }
}