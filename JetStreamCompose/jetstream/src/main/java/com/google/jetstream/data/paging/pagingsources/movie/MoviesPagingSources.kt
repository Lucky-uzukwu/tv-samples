package com.google.jetstream.data.paging.pagingsources.movie

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.UserRepository
import kotlinx.coroutines.flow.Flow

private const val NETWORK_PAGE_SIZE = 30


class MoviesPagingSources {
    fun getMoviesCatalogPagingSource(
        catalog: Catalog,
        movieRepository: MovieRepository,
        userRepository: UserRepository
    ): Flow<PagingData<MovieNew>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            MoviesCatalogPagingSource(
                movieRepository,
                userRepository,
                catalog.id
            )
        }.flow
    }

    fun getMoviesGenrePagingSource(
        genreId: Int,
        movieRepository: MovieRepository,
        userRepository: UserRepository
    ): Flow<PagingData<MovieNew>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            MoviesGenrePagingSource(
                movieRepository,
                userRepository,
                genreId
            )
        }.flow
    }
}