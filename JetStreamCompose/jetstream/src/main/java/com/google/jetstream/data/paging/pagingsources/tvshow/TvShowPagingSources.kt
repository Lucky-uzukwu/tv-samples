package com.google.jetstream.data.paging.pagingsources.tvshow

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
        tvShowRepository: TvShowsRepository,
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
                tvShowRepository,
                userRepository,
                catalog.id
            )
        }.flow
    }

    fun getTvShowsGenrePagingSource(
        genreId: Int,
        tvShowRepository: TvShowsRepository,
        userRepository: UserRepository
    ): Flow<PagingData<TvShow>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 5,
                enablePlaceholders = false
            )
        ) {
            TvShowsGenrePagingSource(
                tvShowRepository,
                userRepository,
                genreId
            )
        }.flow
    }
}