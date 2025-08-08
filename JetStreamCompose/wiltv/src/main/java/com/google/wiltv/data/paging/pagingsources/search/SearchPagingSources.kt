package com.google.wiltv.data.paging.pagingsources.search

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import kotlinx.coroutines.flow.Flow

private const val NETWORK_PAGE_SIZE = 30

class SearchPagingSources {

    fun searchMovies(
        query: String,
        searchRepository: SearchRepository,
        userRepository: UserRepository
    ): Flow<PagingData<MovieNew>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            MovieSearchPagingSource(
                searchRepository,
                userRepository,
                query
            )
        }.flow
    }

    fun searchTvShows(
        query: String,
        searchRepository: SearchRepository,
        userRepository: UserRepository
    ): Flow<PagingData<TvShow>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            TvShowsSearchPagingSource(
                searchRepository,
                userRepository,
                query
            )
        }.flow
    }
}