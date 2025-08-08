package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.MovieSearchResponse
import com.google.wiltv.data.network.ShowSearchResponse
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