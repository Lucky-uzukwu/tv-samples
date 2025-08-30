package com.google.wiltv.data.paging.pagingsources.tvshow

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.UserRepository
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
                initialLoadSize = 20,
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