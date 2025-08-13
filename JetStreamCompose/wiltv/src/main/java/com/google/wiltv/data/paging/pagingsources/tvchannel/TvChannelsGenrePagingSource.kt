// ABOUTME: Paging source for TV channels filtered by genre
// ABOUTME: Fetches TV channels belonging to a specific genre with pagination support

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

class TvChannelsGenrePagingSource(
    private val tvChannelsRepository: TvChannelsRepository,
    private val userRepository: UserRepository,
    private val genreId: Int
) : PagingSource<Int, TvChannel>() {

    override fun getRefreshKey(state: PagingState<Int, TvChannel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvChannel> {
        Logger.d { "📺 TvChannelsGenrePagingSource.load() called with params: ${params.key}, loadSize: ${params.loadSize}, genreId: $genreId" }
        return try {
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Logger.e { "❌ TvChannelsGenrePagingSource: No token available" }
                return LoadResult.Error<Int, TvChannel>(Exception("No token"))
            }
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            Logger.d { "🔍 TvChannelsGenrePagingSource: Making API call with genreId=$genreId, page=$currentPage, pageSize=$pageSize" }

            val tvChannelsResult = tvChannelsRepository.getTvChannelsToShowInGenreSection(
                token = token,
                genreId = genreId,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            Logger.d { "📡 TvChannelsGenrePagingSource: API call completed, result type: ${tvChannelsResult::class.simpleName}" }

            val tvChannels = when (tvChannelsResult) {
                is ApiResult.Success -> {
                    Logger.d { "✅ TvChannelsGenrePagingSource: Success - got ${tvChannelsResult.data.member.size} tv channels for genre $genreId" }
                    tvChannelsResult.data
                }
                is ApiResult.Error -> {
                    val errorMessage = "Failed to fetch genre tv channels: ${tvChannelsResult.message ?: tvChannelsResult.error}"
                    Logger.e { "❌ TvChannelsGenrePagingSource: Error - $errorMessage" }
                    val errorResult = LoadResult.Error<Int, TvChannel>(Exception(errorMessage))
                    Logger.e { "🔥 TvChannelsGenrePagingSource: Returning LoadResult.Error: $errorResult" }
                    return errorResult
                }
            }

            val result = LoadResult.Page(
                data = tvChannels.member, // List<TvChannel>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (tvChannels.member.isEmpty()) null else currentPage + 1
            )
            Logger.d { "📄 TvChannelsGenrePagingSource: Returning LoadResult.Page with ${tvChannels.member.size} tv channels" }
            result
        } catch (e: Exception) {
            Logger.e(e) { "💥 TvChannelsGenrePagingSource: Exception caught - ${e.message}" }
            val errorResult = LoadResult.Error<Int, TvChannel>(e)
            Logger.e { "🔥 TvChannelsGenrePagingSource: Returning LoadResult.Error from exception: $errorResult" }
            errorResult
        }
    }
}