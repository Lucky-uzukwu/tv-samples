// ABOUTME: Paging source for TV channels to be shown in the hero section carousel
// ABOUTME: Fetches TV channels marked for hero section display with pagination support

package com.google.wiltv.data.paging.pagingsources.tvchannel

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.network.TvChannelsResponse
import com.google.wiltv.data.repositories.TvChannelsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.firstOrNull

class TvChannelsHeroSectionPagingSource(
    private val tvChannelsRepository: TvChannelsRepository,
    private val userRepository: UserRepository,
) : PagingSource<Int, TvChannel>() {
    override fun getRefreshKey(state: PagingState<Int, TvChannel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvChannel> {
        Logger.d { "📺 TvChannelsHeroSectionPagingSource.load() called with params: ${params.key}, loadSize: ${params.loadSize}" }
        return try {
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Logger.e { "❌ TvChannelsHeroSectionPagingSource: No token available" }
                return LoadResult.Error<Int, TvChannel>(Exception("No token"))
            }
            
            val currentPage = params.key ?: 1
            Logger.d { "🔍 TvChannelsHeroSectionPagingSource: Making API call with page=$currentPage, pageSize=${params.loadSize}" }

            val tvChannelsResult = tvChannelsRepository.getTvChannelsToShowInHeroSection(
                token = token,
                page = currentPage,
                itemsPerPage = params.loadSize
            )
            
            Logger.d { "📡 TvChannelsHeroSectionPagingSource: API call completed, result type: ${tvChannelsResult::class.simpleName}" }

            val tvChannels = when (tvChannelsResult) {
                is ApiResult.Success -> {
                    Logger.d { "✅ TvChannelsHeroSectionPagingSource: Success - got ${tvChannelsResult.data.member.size} tv channels" }
                    tvChannelsResult.data
                }
                is ApiResult.Error -> {
                    val errorMessage = "Failed to fetch hero tv channels: ${tvChannelsResult.message ?: tvChannelsResult.error}"
                    Logger.e { "❌ TvChannelsHeroSectionPagingSource: Error - $errorMessage" }
                    val errorResult = LoadResult.Error<Int, TvChannel>(Exception(errorMessage))
                    Logger.e { "🔥 TvChannelsHeroSectionPagingSource: Returning LoadResult.Error: $errorResult" }
                    return errorResult
                }
            }

            val result = LoadResult.Page(
                data = tvChannels.member,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (tvChannels.member.isEmpty()) null else currentPage + 1
            )
            Logger.d { "📄 TvChannelsHeroSectionPagingSource: Returning LoadResult.Page with ${tvChannels.member.size} tv channels" }
            result
        } catch (e: Exception) {
            Logger.e(e) { "💥 TvChannelsHeroSectionPagingSource: Exception caught - ${e.message}" }
            val errorResult = LoadResult.Error<Int, TvChannel>(e)
            Logger.e { "🔥 TvChannelsHeroSectionPagingSource: Returning LoadResult.Error from exception: $errorResult" }
            errorResult
        }
    }
}