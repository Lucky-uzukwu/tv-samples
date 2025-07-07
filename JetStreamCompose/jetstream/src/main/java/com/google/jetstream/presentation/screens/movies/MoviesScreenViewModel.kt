package com.google.jetstream.presentation.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.paging.pagingsources.movie.MoviesHeroSectionPagingSource
import com.google.jetstream.data.paging.pagingsources.movie.MoviesPagingSources
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.MovieRepository
import com.google.jetstream.data.repositories.StreamingProvidersRepository
import com.google.jetstream.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MoviesScreenViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    userRepository: UserRepository,
    catalogRepository: CatalogRepository,
    genreRepository: GenreRepository,
    streamingProvidersRepository: StreamingProvidersRepository
) : ViewModel() {

    // Paginated flows for movie lists
    val heroSectionMovies: StateFlow<PagingData<MovieNew>> = Pager(
        PagingConfig(pageSize = 5, initialLoadSize = 5)
    ) {
        MoviesHeroSectionPagingSource(movieRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    // UI State combining all data
    val uiState: StateFlow<MoviesScreenUiState> = combine(
        userRepository.userToken,
    ) { token ->
        val token = token.firstOrNull() ?: ""
        when {
            token.isEmpty() -> MoviesScreenUiState.Error
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

                MoviesScreenUiState.Ready(
                    catalogToMovies = catalogToMovies,
                    genreToMovies = genreToMovies,
                    streamingProviders = streamingProviders ?: emptyList()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MoviesScreenUiState.Loading
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

sealed interface MoviesScreenUiState {
    data object Loading : MoviesScreenUiState
    data object Error : MoviesScreenUiState
    data class Ready(
        val catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
        val genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
        val streamingProviders: List<StreamingProvider>
    ) : MoviesScreenUiState
}
