package com.google.jetstream.data.pagingsources.tvshow

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.TvShowsResponse
import com.google.jetstream.data.repositories.TvShowsRepository
import com.google.jetstream.data.repositories.UserRepository
import kotlinx.coroutines.flow.firstOrNull

class TvShowsGenrePagingSource(
    private val tvShowsRepository: TvShowsRepository,
    private val userRepository: UserRepository,
    private val genreId: Int
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
            val tvshows: TvShowsResponse = tvShowsRepository.getTvShowsToShowInGenreSection(
                token = token,
                genreId = genreId,
                page = currentPage,
                itemsPerPage = pageSize
            ).firstOrNull() ?: TvShowsResponse(member = emptyList())

            LoadResult.Page(
                data = tvshows.member, // List<TvShow>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = when {
                    tvshows.member.size == 1 -> null // Stop fetching if exactly one item
                    tvshows.member.isEmpty() -> null // Stop fetching if empty
                    else -> currentPage + 1 // Continue fetching for more than one item
                }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}