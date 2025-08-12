package com.google.wiltv.data.network.remote_mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.google.wiltv.AppDatabase
import com.google.wiltv.data.entities.MovieEntity
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.MovieRemoteKey
import com.google.wiltv.data.models.toMovieEntity
import com.google.wiltv.data.network.MovieResponse
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class MoviesRemoteMediator(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val appDatabase: AppDatabase
) : RemoteMediator<Int, MovieEntity>() {


    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)

        return if (System.currentTimeMillis() - (appDatabase.getMovieRemoteKeyDao()
                .getCreationTime() ?: 0) < cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
            }
        }

        try {
            val token = userRepository.userToken.firstOrNull() ?: return MediatorResult.Error(
                Exception("No token")
            )

            val moviesResult = movieRepository.getMoviesToShowInHeroSection(
                token,
                page,
                itemsPerPage = 10
            )
            
            val movies = when (moviesResult) {
                is ApiResult.Success -> moviesResult.data
                is ApiResult.Error -> return MediatorResult.Error(
                    Exception("Failed to fetch movies: ${moviesResult.message ?: moviesResult.error}")
                )
            }

            val endOfPaginationReached = movies.member.isEmpty()

            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    appDatabase.getMovieRemoteKeyDao().clearRemoteKeys()
                    appDatabase.getMoviesDao().clearAllMovies()
                }
                val prevKey = if (page > 1) page - 1 else null
                val nextKey = if (endOfPaginationReached) null else page + 1
                val remoteKeys = movies.member.map {
                    MovieRemoteKey(
                        movieId = it.id,
                        prevKey = prevKey,
                        currentPage = page,
                        nextKey = nextKey
                    )
                }

                appDatabase.getMovieRemoteKeyDao().insertAll(remoteKeys)
                appDatabase.getMoviesDao()
                    .insertAll(movies.member.map { it.toMovieEntity() }.onEachIndexed { _, movie ->
                        movie.page = page
                    })
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (error: IOException) {
            return MediatorResult.Error(error)
        } catch (error: HttpException) {
            return MediatorResult.Error(error)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, MovieEntity>): MovieRemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                appDatabase.getMovieRemoteKeyDao().getRemoteKeyByMovieID(id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, MovieEntity>): MovieRemoteKey? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { movie ->
            appDatabase.getMovieRemoteKeyDao().getRemoteKeyByMovieID(movie.id)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MovieEntity>): MovieRemoteKey? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { movie ->
            appDatabase.getMovieRemoteKeyDao().getRemoteKeyByMovieID(movie.id)
        }
    }
}