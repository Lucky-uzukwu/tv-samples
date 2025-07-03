package com.google.jetstream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.AppDatabase
import com.google.jetstream.data.entities.MovieEntity
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.network.remote_mediator.MoviesRemoteMediator
import com.google.jetstream.data.paging.pagingsources.movie.MoviesPagingSources
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.StreamingProvidersRepository
import com.google.jetstream.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

const val PAGE_SIZE = 20

@HiltViewModel
class HomeScreeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
    private val genreRepository: GenreRepository,
    private val appDatabase: AppDatabase,
    private val streamingProvidersRepository: StreamingProvidersRepository
) : ViewModel() {


    @OptIn(ExperimentalPagingApi::class)
    fun fetchHeroMovies(): Flow<PagingData<MovieEntity>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 10,
                initialLoadSize = PAGE_SIZE,
            ),
            pagingSourceFactory = {
                appDatabase.getMoviesDao().getMovies()
            },
            remoteMediator = MoviesRemoteMediator(
                movieRepository = movieRepository,
                appDatabase = appDatabase,
                userRepository = userRepository
            )
        ).flow


    // UI State combining all data
    val uiState: StateFlow<HomeScreenUiState> = combine(
        userRepository.userToken,
    ) { token ->
        val token = token.firstOrNull() ?: ""
        when {
            token.isEmpty() -> HomeScreenUiState.Error
            else -> {
                // Create paginated flows for each catalog
                val catalogToMovies = fetchCatalogsAndMovies(
                    catalogRepository,
                    userRepository
                )
                val genreToMovies = fetchGenresAndMovies(
                    genreRepository = genreRepository,
                    userRepository
                )

                val streamingProviders =
                    streamingProvidersRepository.getStreamingProviders().firstOrNull()

                HomeScreenUiState.Ready(
                    catalogToMovies = catalogToMovies,
                    genreToMovies = genreToMovies,
                    streamingProviders = streamingProviders ?: emptyList()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenUiState.Loading
    )

    private suspend fun fetchCatalogsAndMovies(
        catalogRepository: CatalogRepository,
        userRepository: UserRepository
    ): Map<Catalog, StateFlow<PagingData<MovieNew>>> {
        val catalogs = catalogRepository.getMovieCatalog().firstOrNull() ?: emptyList()
        val catalogToMovies = catalogs.associateWith { catalog ->
            MoviesPagingSources().getMoviesCatalogPagingSource(
                catalog = catalog,
                movieRepository = movieRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )
        }
        return catalogToMovies
    }

    private suspend fun fetchGenresAndMovies(
        genreRepository: GenreRepository,
        userRepository: UserRepository
    ): Map<Genre, StateFlow<PagingData<MovieNew>>> {
        val genres = genreRepository.getMovieGenre().firstOrNull() ?: emptyList()
        val genreToMovies = genres.associateWith { genre ->
            MoviesPagingSources().getMoviesGenrePagingSource(
                genreId = genre.id,
                movieRepository = movieRepository,
                userRepository = userRepository
            ).cachedIn(viewModelScope).stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                PagingData.empty()
            )
        }
        return genreToMovies
    }
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data object Error : HomeScreenUiState
    data class Ready(
        val catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
        val genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
        val streamingProviders: List<StreamingProvider>
    ) : HomeScreenUiState
}
