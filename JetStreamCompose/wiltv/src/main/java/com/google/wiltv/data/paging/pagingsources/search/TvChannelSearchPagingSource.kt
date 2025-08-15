package com.google.wiltv.data.paging.pagingsources.search

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.network.TvChannelsResponse
import com.google.wiltv.data.repositories.SearchRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class TvChannelSearchPagingSource(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository,
    private val query: String
) : PagingSource<Int, TvChannel>() {
    override fun getRefreshKey(state: PagingState<Int, TvChannel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvChannel> {
        return try {

            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                return LoadResult.Error(Exception("No token"))
            }
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            
            val channelsResult = searchRepository.searchTvChannelsByQuery(
                token = token,
                query = query,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            val channels = when (channelsResult) {
                is ApiResult.Success -> {
                    channelsResult.data
                }
                is ApiResult.Error -> {
                    val errorMsg = "Failed to search TV channels: ${channelsResult.message ?: channelsResult.error}"
                    return LoadResult.Error(Exception(errorMsg))
                }
            }

            val result = LoadResult.Page(
                data = channels.member, // List<TvChannel>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (channels.member.isEmpty()) null else currentPage + 1
            )

            result
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}