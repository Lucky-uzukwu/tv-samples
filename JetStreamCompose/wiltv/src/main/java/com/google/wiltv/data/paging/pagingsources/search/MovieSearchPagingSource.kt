package com.google.wiltv.data.paging.pagingsources.search

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.network.MovieSearchResponse
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class MovieSearchPagingSource(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
    private val query: String
) : PagingSource<Int, MovieNew>() {
    override fun getRefreshKey(state: PagingState<Int, MovieNew>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieNew> {
        return try {
            Log.d("MovieSearchPaging", "=== Movie Search Load Start ===")
            Log.d("MovieSearchPaging", "Query: '$query'")
            Log.d("MovieSearchPaging", "Load params - key: ${params.key}, loadSize: ${params.loadSize}")
            
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Log.e("MovieSearchPaging", "No authentication token available")
                return LoadResult.Error(Exception("No token"))
            }
            Log.d("MovieSearchPaging", "Token available: ${token.take(20)}...")
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            
            Log.d("MovieSearchPaging", "Calling API - page: $currentPage, pageSize: $pageSize")
            val moviesResult = searchRepository.searchMoviesByQuery(
                token = token,
                query = query,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            val movies = when (moviesResult) {
                is ApiResult.Success -> {
                    Log.d("MovieSearchPaging", "API Success - Got ${moviesResult.data.member.size} movies")
                    moviesResult.data
                }
                is ApiResult.Error -> {
                    val errorMsg = "Failed to search movies: ${moviesResult.message ?: moviesResult.error}"
                    Log.e("MovieSearchPaging", errorMsg)
                    return LoadResult.Error(Exception(errorMsg))
                }
            }

            val result = LoadResult.Page(
                data = movies.member, // List<MovieNew>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (movies.member.isEmpty()) null else currentPage + 1
            )
            
            Log.d("MovieSearchPaging", "Returning Page with ${movies.member.size} items")
            Log.d("MovieSearchPaging", "PrevKey: ${result.prevKey}, NextKey: ${result.nextKey}")
            result
        } catch (e: Exception) {
            Log.e("MovieSearchPaging", "Exception in load(): ${e.message}", e)
            LoadResult.Error(e)
        }
    }
}