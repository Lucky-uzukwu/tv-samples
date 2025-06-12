package com.google.jetstream.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.StreamingProvidersRepository
import com.google.jetstream.data.repositories.UserRepository
import com.google.jetstream.data.pagingsources.movie.MoviesPagingSources
import com.google.jetstream.data.pagingsources.movie.MoviesHeroSectionPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeScreeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
    private val genreRepository: GenreRepository,
    private val streamingProvidersRepository: StreamingProvidersRepository
) : ViewModel() {

    private val _heroMovies = MutableStateFlow<PagingData<MovieNew>>(
        PagingData.empty()
    )
    val heroMovies: StateFlow<PagingData<MovieNew>> get() = _heroMovies.asStateFlow()

    init {
        fetchHeroMovies()
    }

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


    private fun fetchHeroMovies() {
        viewModelScope.launch {
            Pager(
                PagingConfig(pageSize = 5, initialLoadSize = 5)
            ) {
                MoviesHeroSectionPagingSource(movieRepository, userRepository)
            }.flow
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _heroMovies.value = pagingData
                }
        }
    }

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
