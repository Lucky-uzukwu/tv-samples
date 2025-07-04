package com.google.jetstream.data.paging.pagingsources.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.SearchResponse
import com.google.jetstream.data.repositories.SearchRepository
import com.google.jetstream.data.repositories.UserRepository
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
            // Fetch all catalogs
            val movies: SearchResponse = searchRepository.searchWithQueryAndType(
                token = token,
                query = query,
                type = "App\\Models\\Movie",
                page = currentPage,
                itemsPerPage = pageSize
            ).firstOrNull() ?: SearchResponse(member = emptyList())

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