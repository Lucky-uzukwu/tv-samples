package com.google.wiltv.data.paging.pagingsources.movie

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.network.MovieResponse
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull

class MoviesHeroSectionPagingSource(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
) : PagingSource<Int, MovieNew>() {
    override fun getRefreshKey(state: PagingState<Int, MovieNew>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieNew> {
        return try {
            val token = userRepository.userToken.firstOrNull() ?: return LoadResult.Error(
                Exception(
                    "No token"
                )
            )
            val currentPage = params.key ?: 1

            val moviesResult = movieRepository.getMoviesToShowInHeroSection(
                token,
                currentPage,
                itemsPerPage = params.loadSize
            )
            
            val movies = when (moviesResult) {
                is ApiResult.Success -> moviesResult.data
                is ApiResult.Error -> return LoadResult.Error(
                    Exception("Failed to fetch movies: ${moviesResult.message ?: moviesResult.error}")
                )
            }

            LoadResult.Page(
                data = movies.member,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (movies.member.isEmpty()) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}