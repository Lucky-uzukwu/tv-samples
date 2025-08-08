package com.google.wiltv.data.paging.pagingsources.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.ShowSearchResponse
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
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
            val token = userRepository.userToken.firstOrNull()
                ?: return LoadResult.Error(Exception("No token"))
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            // Fetch all catalogs
            val searchResults: ShowSearchResponse = searchRepository.searchTvShowsByQuery(
                token = token,
                query = query,
                page = currentPage,
                itemsPerPage = pageSize
            ).firstOrNull() ?: ShowSearchResponse(member = emptyList())

            LoadResult.Page(
                data = searchResults.member, // List<TvShow>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (searchResults.member.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}