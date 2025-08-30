// ABOUTME: Unified paging source for search results that handles mixed content types
// ABOUTME: Replaces separate movie, TV show, and channel paging sources with single implementation

package com.google.wiltv.data.paging.pagingsources.search

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.network.ContentType
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class UnifiedSearchPagingSource(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
    private val query: String,
    private val contentTypes: List<ContentType>? = null
) : PagingSource<Int, SearchContent>() {
    
    override fun getRefreshKey(state: PagingState<Int, SearchContent>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchContent> {
        return try {
            Log.d("UnifiedSearchPaging", "=== Unified Search Load Start ===")
            Log.d("UnifiedSearchPaging", "Query: '$query'")
            Log.d("UnifiedSearchPaging", "Content types: ${contentTypes?.map { it.apiValue }}")
            Log.d("UnifiedSearchPaging", "Load params - key: ${params.key}, loadSize: ${params.loadSize}")
            
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Log.e("UnifiedSearchPaging", "No authentication token available")
                return LoadResult.Error(Exception("No token"))
            }
            Log.d("UnifiedSearchPaging", "Token available: ${token.take(20)}...")
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            
            Log.d("UnifiedSearchPaging", "Calling API - page: $currentPage, pageSize: $pageSize")
            val searchResult = searchRepository.searchContent(
                token = token,
                query = query,
                page = currentPage,
                itemsPerPage = pageSize,
                contentTypes = contentTypes
            )
            
            val searchResponse = when (searchResult) {
                is ApiResult.Success -> {
                    Log.d("UnifiedSearchPaging", "API Success - Got ${searchResult.data.member.size} items")
                    searchResult.data
                }
                is ApiResult.Error -> {
                    val errorMsg = "Failed to search content: ${searchResult.message ?: searchResult.error}"
                    Log.e("UnifiedSearchPaging", errorMsg)
                    return LoadResult.Error(Exception(errorMsg))
                }
            }

            val result = LoadResult.Page(
                data = searchResponse.member, // List<SearchContent>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (searchResponse.member.isEmpty()) null else currentPage + 1
            )
            
            Log.d("UnifiedSearchPaging", "Returning Page with ${searchResponse.member.size} items")
            Log.d("UnifiedSearchPaging", "PrevKey: ${result.prevKey}, NextKey: ${result.nextKey}")
            result
        } catch (e: Exception) {
            Log.e("UnifiedSearchPaging", "Exception in load(): ${e.message}", e)
            LoadResult.Error(e)
        }
    }
}