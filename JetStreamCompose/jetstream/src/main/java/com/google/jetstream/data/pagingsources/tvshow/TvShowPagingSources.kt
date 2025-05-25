package com.google.jetstream.data.pagingsources.tvshow

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.repositories.TvShowsRepository
import com.google.jetstream.data.repositories.UserRepository
import kotlinx.coroutines.flow.Flow

private const val NETWORK_PAGE_SIZE = 30


class TvShowPagingSources {
    fun getTvShowsCatalogPagingSource(
        catalog: Catalog,
        tvShowsRepository: TvShowsRepository,
        userRepository: UserRepository
    ): Flow<PagingData<TvShow>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            TvShowsCatalogPagingSource(
                tvShowsRepository,
                userRepository,
                catalog.id
            )
        }.flow
    }

    fun getTvShowsGenrePagingSource(
        genreId: Int,
        tvShowsRepository: TvShowsRepository,
        userRepository: UserRepository
    ): Flow<PagingData<TvShow>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 40,
                enablePlaceholders = false
            )
        ) {
            TvShowsGenrePagingSource(
                tvShowsRepository,
                userRepository,
                genreId
            )
        }.flow
    }
}