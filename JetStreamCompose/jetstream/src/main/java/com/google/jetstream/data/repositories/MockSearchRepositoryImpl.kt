package com.google.jetstream.data.repositories

import com.google.jetstream.data.models.Country
import com.google.jetstream.data.models.Episode
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.Language
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.MoviePerson
import com.google.jetstream.data.models.Person
import com.google.jetstream.data.models.PersonType
import com.google.jetstream.data.models.Season
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.models.Video
import com.google.jetstream.data.network.MovieSearchResponse
import com.google.jetstream.data.network.ShowSearchResponse
import com.google.jetstream.data.repositories.mock.MockData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockSearchRepositoryImpl : SearchRepository {
    override fun searchMoviesByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieSearchResponse> = flow {
        emit(
            MockData.getMovieSearchResponse()
        )
    }

    override fun searchTvShowsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<ShowSearchResponse> = flow {
        emit(
            MockData.getTvShowSearchResponse()
        )
    }
}