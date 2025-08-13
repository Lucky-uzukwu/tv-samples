// ABOUTME: Helper class providing paging data flows for TV channels
// ABOUTME: Creates Pager instances for different TV channel data sources (hero section, genres)

package com.google.wiltv.data.paging.pagingsources.tvchannel

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.repositories.TvChannelsRepository
import com.google.wiltv.data.repositories.UserRepository
import kotlinx.coroutines.flow.Flow

private const val NETWORK_PAGE_SIZE = 30

class TvChannelPagingSources {
    fun getTvChannelsGenrePagingSource(
        genreId: Int,
        tvChannelsRepository: TvChannelsRepository,
        userRepository: UserRepository
    ): Flow<PagingData<TvChannel>> {

        return Pager(
            PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                initialLoadSize = 5,
                enablePlaceholders = false
            )
        ) {
            TvChannelsGenrePagingSource(
                tvChannelsRepository,
                userRepository,
                genreId
            )
        }.flow
    }
}