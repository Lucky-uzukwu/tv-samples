package com.google.wiltv.data.paging.pagingsources.search

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
            val token = userRepository.userToken.firstOrNull()
                ?: return LoadResult.Error(Exception("No token"))
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            val moviesResult = searchRepository.searchMoviesByQuery(
                token = token,
                query = query,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            val movies = when (moviesResult) {
                is ApiResult.Success -> moviesResult.data
                is ApiResult.Error -> return LoadResult.Error(
                    Exception("Failed to search movies: ${moviesResult.message ?: moviesResult.error}")
                )
            }

            LoadResult.Page(
                data = movies.member, // List<MovieNew>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (movies.member.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}