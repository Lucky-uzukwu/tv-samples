package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.MovieSearchResponse
import com.google.jetstream.data.network.ShowSearchResponse
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun searchMoviesByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieSearchResponse>

    fun searchTvShowsByQuery(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<ShowSearchResponse>
}