package com.google.wiltv.data.paging.pagingsources.tvshow

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvShowsResponse
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.firstOrNull

class TvShowsCatalogPagingSource(
    private val tvShowsRepository: TvShowsRepository,
    private val userRepository: UserRepository,
    private val catalogId: String
) : PagingSource<Int, TvShow>() {

    override fun getRefreshKey(state: PagingState<Int, TvShow>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvShow> {
        Logger.d { "📚 TvShowsCatalogPagingSource.load() called with params: ${params.key}, loadSize: ${params.loadSize}, catalogId: $catalogId" }
        return try {
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Logger.e { "❌ TvShowsCatalogPagingSource: No token available" }
                return LoadResult.Error<Int, TvShow>(Exception("No token"))
            }
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            Logger.d { "🔍 TvShowsCatalogPagingSource: Making API call with catalogId=$catalogId, page=$currentPage, pageSize=$pageSize" }

            val tvShowsResult = tvShowsRepository.getTvShowsToShowInCatalogSection(
                token = token,
                catalogId = catalogId,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            Logger.d { "📡 TvShowsCatalogPagingSource: API call completed, result type: ${tvShowsResult::class.simpleName}" }

            val tvShows = when (tvShowsResult) {
                is ApiResult.Success -> {
                    Logger.d { "✅ TvShowsCatalogPagingSource: Success - got ${tvShowsResult.data.member.size} tv shows for catalog $catalogId" }
                    tvShowsResult.data
                }
                is ApiResult.Error -> {
                    val errorMessage = "Failed to fetch catalog tv shows: ${tvShowsResult.message ?: tvShowsResult.error}"
                    Logger.e { "❌ TvShowsCatalogPagingSource: Error - $errorMessage" }
                    val errorResult = LoadResult.Error<Int, TvShow>(Exception(errorMessage))
                    Logger.e { "🔥 TvShowsCatalogPagingSource: Returning LoadResult.Error: $errorResult" }
                    return errorResult
                }
            }

            val result = LoadResult.Page(
                data = tvShows.member, // List<TvShow>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (tvShows.member.isEmpty()) null else currentPage + 1
            )
            Logger.d { "📄 TvShowsCatalogPagingSource: Returning LoadResult.Page with ${tvShows.member.size} tv shows" }
            result
        } catch (e: Exception) {
            Logger.e(e) { "💥 TvShowsCatalogPagingSource: Exception caught - ${e.message}" }
            val errorResult = LoadResult.Error<Int, TvShow>(e)
            Logger.e { "🔥 TvShowsCatalogPagingSource: Returning LoadResult.Error from exception: $errorResult" }
            errorResult
        }
    }
}