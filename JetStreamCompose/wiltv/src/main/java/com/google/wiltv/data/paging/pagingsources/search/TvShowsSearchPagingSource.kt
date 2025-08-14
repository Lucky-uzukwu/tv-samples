package com.google.wiltv.data.paging.pagingsources.search

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.ShowSearchResponse
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class TvShowsSearchPagingSource(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
    private val query: String
) : PagingSource<Int, TvShow>() {
    override fun getRefreshKey(state: PagingState<Int, TvShow>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvShow> {
        return try {
            Log.d("TvShowSearchPaging", "=== TV Show Search Load Start ===")
            Log.d("TvShowSearchPaging", "Query: '$query'")
            Log.d("TvShowSearchPaging", "Load params - key: ${params.key}, loadSize: ${params.loadSize}")
            
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Log.e("TvShowSearchPaging", "No authentication token available")
                return LoadResult.Error(Exception("No token"))
            }
            Log.d("TvShowSearchPaging", "Token available: ${token.take(20)}...")
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            
            Log.d("TvShowSearchPaging", "Calling API - page: $currentPage, pageSize: $pageSize")
            val searchResult = searchRepository.searchTvShowsByQuery(
                token = token,
                query = query,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            val searchResults = when (searchResult) {
                is ApiResult.Success -> {
                    Log.d("TvShowSearchPaging", "API Success - Got ${searchResult.data.member.size} TV shows")
                    searchResult.data
                }
                is ApiResult.Error -> {
                    val errorMsg = "Failed to search TV shows: ${searchResult.message ?: searchResult.error}"
                    Log.e("TvShowSearchPaging", errorMsg)
                    return LoadResult.Error(Exception(errorMsg))
                }
            }

            val result = LoadResult.Page(
                data = searchResults.member, // List<TvShow>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (searchResults.member.isEmpty()) null else currentPage + 1
            )
            
            Log.d("TvShowSearchPaging", "Returning Page with ${searchResults.member.size} items")
            Log.d("TvShowSearchPaging", "PrevKey: ${result.prevKey}, NextKey: ${result.nextKey}")
            result
        } catch (e: Exception) {
            Log.e("TvShowSearchPaging", "Exception in load(): ${e.message}", e)
            LoadResult.Error(e)
        }
    }
}