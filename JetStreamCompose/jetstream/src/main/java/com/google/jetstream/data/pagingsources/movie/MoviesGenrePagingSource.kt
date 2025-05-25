package com.google.jetstream.data.pagingsources.movie

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.MovieResponse
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.UserRepository
import kotlinx.coroutines.flow.firstOrNull

class MoviesGenrePagingSource(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val genreId: Int
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
            val movies: MovieResponse = movieRepository.getMoviesToShowInGenreSection(
                token = token,
                genreId = genreId,
                page = currentPage,
                itemsPerPage = pageSize
            ).firstOrNull() ?: MovieResponse(member = emptyList())

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