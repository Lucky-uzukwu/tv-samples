package com.google.jetstream.data.paging.pagingsources.tvshow

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.TvShowsResponse
import com.google.jetstream.data.repositories.TvShowsRepository
import com.google.jetstream.data.repositories.UserRepository
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
        return try {
            val token = userRepository.userToken.firstOrNull()
                ?: return LoadResult.Error(Exception("No token"))
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            // Fetch all catalogs
            val tvShows: TvShowsResponse = tvShowsRepository.getTvShowsToShowInCatalogSection(
                token = token,
                catalogId = catalogId,
                page = currentPage,
                itemsPerPage = pageSize
            ).firstOrNull() ?: TvShowsResponse(member = emptyList())

            LoadResult.Page(
                data = tvShows.member, // List<TvShow>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (tvShows.member.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}