// ABOUTME: Paging source for loading all TV channels without genre filtering
// ABOUTME: Uses getTvChannelsToShowInHeroSection method to fetch all available channels

package com.google.wiltv.data.paging.pagingsources.tvchannel

import androidx.paging.PagingSource
import androidx.paging.PagingState
import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.repositories.TvChannelsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class TvChannelsAllPagingSource(
    private val tvChannelsRepository: TvChannelsRepository,
    private val userRepository: UserRepository
) : PagingSource<Int, TvChannel>() {

    override fun getRefreshKey(state: PagingState<Int, TvChannel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvChannel> {
        Logger.d { "📺 TvChannelsAllPagingSource.load() called with params: ${params.key}, loadSize: ${params.loadSize}" }
        return try {
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Logger.e { "❌ TvChannelsAllPagingSource: No token available" }
                return LoadResult.Error<Int, TvChannel>(Exception("No token"))
            }
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            Logger.d { "🔍 TvChannelsAllPagingSource: Making API call with page=$currentPage, pageSize=$pageSize" }

            val tvChannelsResult = tvChannelsRepository.getTvChannels(
                token = token,
                page = currentPage,
                itemsPerPage = pageSize
            )

            when (tvChannelsResult) {
                is ApiResult.Success -> {
                    val tvChannels = tvChannelsResult.data.member
                    val totalItems = tvChannelsResult.data.totalItems ?: 0
                    
                    Logger.d { "✅ TvChannelsAllPagingSource: Successfully loaded ${tvChannels.size} channels, total: $totalItems" }

                    val prevKey = if (currentPage == 1) null else currentPage - 1
                    val nextKey = if (tvChannels.isEmpty() || tvChannels.size < pageSize) {
                        null
                    } else {
                        currentPage + 1
                    }

                    LoadResult.Page(
                        data = tvChannels,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }
                is ApiResult.Error -> {
                    Logger.e { "❌ TvChannelsAllPagingSource: API error - ${tvChannelsResult.message}" }
                    LoadResult.Error<Int, TvChannel>(Exception(tvChannelsResult.message))
                }
            }
        } catch (exception: Exception) {
            Logger.e(exception) { "❌ TvChannelsAllPagingSource: Exception during load" }
            LoadResult.Error<Int, TvChannel>(exception)
        }
    }
}