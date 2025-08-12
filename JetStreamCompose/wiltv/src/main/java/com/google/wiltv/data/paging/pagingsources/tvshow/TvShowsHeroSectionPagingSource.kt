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

class TvShowsHeroSectionPagingSource(
    private val tvShowRepository: TvShowsRepository,
    private val userRepository: UserRepository,
) : PagingSource<Int, TvShow>() {
    override fun getRefreshKey(state: PagingState<Int, TvShow>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvShow> {
        Logger.d { "🎬 TvShowsHeroSectionPagingSource.load() called with params: ${params.key}, loadSize: ${params.loadSize}" }
        return try {
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Logger.e { "❌ TvShowsHeroSectionPagingSource: No token available" }
                return LoadResult.Error<Int, TvShow>(Exception("No token"))
            }
            
            val currentPage = params.key ?: 1
            Logger.d { "🔍 TvShowsHeroSectionPagingSource: Making API call with page=$currentPage, pageSize=${params.loadSize}" }

            val tvShowsResult = tvShowRepository.getTvShowsToShowInHeroSection(
                token = token,
                page = currentPage,
                itemsPerPage = params.loadSize
            )
            
            Logger.d { "📡 TvShowsHeroSectionPagingSource: API call completed, result type: ${tvShowsResult::class.simpleName}" }

            val tvShows = when (tvShowsResult) {
                is ApiResult.Success -> {
                    Logger.d { "✅ TvShowsHeroSectionPagingSource: Success - got ${tvShowsResult.data.member.size} tv shows" }
                    tvShowsResult.data
                }
                is ApiResult.Error -> {
                    val errorMessage = "Failed to fetch hero tv shows: ${tvShowsResult.message ?: tvShowsResult.error}"
                    Logger.e { "❌ TvShowsHeroSectionPagingSource: Error - $errorMessage" }
                    val errorResult = LoadResult.Error<Int, TvShow>(Exception(errorMessage))
                    Logger.e { "🔥 TvShowsHeroSectionPagingSource: Returning LoadResult.Error: $errorResult" }
                    return errorResult
                }
            }

            val result = LoadResult.Page(
                data = tvShows.member,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (tvShows.member.isEmpty()) null else currentPage + 1
            )
            Logger.d { "📄 TvShowsHeroSectionPagingSource: Returning LoadResult.Page with ${tvShows.member.size} tv shows" }
            result
        } catch (e: Exception) {
            Logger.e(e) { "💥 TvShowsHeroSectionPagingSource: Exception caught - ${e.message}" }
            val errorResult = LoadResult.Error<Int, TvShow>(e)
            Logger.e { "🔥 TvShowsHeroSectionPagingSource: Returning LoadResult.Error from exception: $errorResult" }
            errorResult
        }
    }
}