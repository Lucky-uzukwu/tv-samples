package com.google.wiltv.data.paging.pagingsources.movie

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.network.MovieResponse
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import co.touchlab.kermit.Logger
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
        Logger.d { "🚀 MoviesGenrePagingSource.load() called with params: ${params.key}, loadSize: ${params.loadSize}" }
        return try {
            val token = userRepository.userToken.firstOrNull()
            if (token == null) {
                Logger.e { "❌ MoviesGenrePagingSource: No token available" }
                return LoadResult.Error<Int, MovieNew>(Exception("No token"))
            }
            
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize
            Logger.d { "🔍 MoviesGenrePagingSource: Making API call with genreId=1, page=$currentPage, pageSize=$pageSize" }
            
            val moviesResult = movieRepository.getMoviesToShowInGenreSection(
                token = token,
                genreId = genreId,
                page = currentPage,
                itemsPerPage = pageSize
            )
            
            Logger.d { "📡 MoviesGenrePagingSource: API call completed, result type: ${moviesResult::class.simpleName}" }
            
            val movies = when (moviesResult) {
                is ApiResult.Success -> {
                    Logger.d { "✅ MoviesGenrePagingSource: Success - got ${moviesResult.data.member.size} movies" }
                    moviesResult.data
                }
                is ApiResult.Error -> {
                    val errorMessage = "Failed to fetch genre movies: ${moviesResult.message ?: moviesResult.error}"
                    Logger.e { "❌ MoviesGenrePagingSource: Error - $errorMessage" }
                    val errorResult = LoadResult.Error<Int, MovieNew>(Exception(errorMessage))
                    Logger.e { "🔥 MoviesGenrePagingSource: Returning LoadResult.Error: $errorResult" }
                    return errorResult
                }
            }

            val result = LoadResult.Page(
                data = movies.member, // List<MovieNew>
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (movies.member.isEmpty()) null else currentPage + 1
            )
            Logger.d { "📄 MoviesGenrePagingSource: Returning LoadResult.Page with ${movies.member.size} movies" }
            result
        } catch (e: Exception) {
            Logger.e(e) { "💥 MoviesGenrePagingSource: Exception caught - ${e.message}" }
            val errorResult = LoadResult.Error<Int, MovieNew>(e)
            Logger.e { "🔥 MoviesGenrePagingSource: Returning LoadResult.Error from exception: $errorResult" }
            errorResult
        }
    }
}