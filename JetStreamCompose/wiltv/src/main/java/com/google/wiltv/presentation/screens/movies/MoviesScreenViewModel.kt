package com.google.wiltv.presentation.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.data.paging.pagingsources.movie.MoviesHeroSectionPagingSource
import com.google.wiltv.data.paging.pagingsources.movie.MoviesPagingSources
import com.google.wiltv.data.repositories.CatalogRepository
import com.google.wiltv.data.repositories.GenreRepository
import com.google.wiltv.data.repositories.MovieRepository
import com.google.wiltv.data.repositories.StreamingProvidersRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.repositories.WatchlistRepository
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import co.touchlab.kermit.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesScreenViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
    private val genreRepository: GenreRepository,
    private val streamingProvidersRepository: StreamingProvidersRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {

    // Paginated flows for movie lists
    val heroSectionMovies: StateFlow<PagingData<MovieNew>> = Pager(
        PagingConfig(pageSize = 20, initialLoadSize = 20)
    ) {
        MoviesHeroSectionPagingSource(movieRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    val watchlistItemIds: StateFlow<Set<String>> = userRepository.userId.map { userId ->
        if (userId != null) {
            try {
                watchlistRepository.getUserWatchlist(userId).stateIn(viewModelScope).value
                    .map { it.contentId.toString() }
                    .toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptySet()
    )

    private val _uiState = MutableStateFlow<MoviesScreenUiState>(MoviesScreenUiState.Loading)
    val uiState: StateFlow<MoviesScreenUiState> = _uiState.asStateFlow()

    init {
        fetchMovieScreenData()
    }

    fun fetchMovieScreenData() {
        viewModelScope.launch {
            try {
                combine(
                    userRepository.userToken,
                ) { token ->
                    val token = token.firstOrNull() ?: ""
                    when {
                        token.isEmpty() -> MoviesScreenUiState.Error(UiText.DynamicString("Unauthorized"))
                        else -> {
                            val catalogToMovies = fetchCatalogsAndMovies(
                                catalogRepository,
                                userRepository
                            )
                            val genreToMovies = fetchGenresAndMovies(
                                genreRepository = genreRepository,
                                userRepository
                            )
                            val streamingProvidersResult = streamingProvidersRepository.getStreamingProviders(
                                type = "App\\Models\\TvShow"
                            )
                            val streamingProviders = when (streamingProvidersResult) {
                                is ApiResult.Success -> streamingProvidersResult.data
                                is ApiResult.Error -> {
                                    Logger.e { "❌ Failed to fetch streaming providers: ${streamingProvidersResult.message ?: streamingProvidersResult.error}" }
                                    emptyList()
                                }
                            }

                            if (catalogToMovies.isEmpty() && genreToMovies.isEmpty() && streamingProviders.isEmpty()) {
                                return@combine MoviesScreenUiState.Error(UiText.DynamicString("No data found"))
                            } else {
                                return@combine MoviesScreenUiState.Ready(
                                    catalogToMovies = catalogToMovies,
                                    genreToMovies = genreToMovies,
                                    streamingProviders = streamingProviders
                                )
                            }
                        }
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = MoviesScreenUiState.Error(
                    message = UiText.DynamicString(
                        value = "Error Fetching movies",
                    )
                )
            }
        }
    }

    private suspend fun fetchCatalogsAndMovies(
        catalogRepository: CatalogRepository,
        userRepository: UserRepository
    ): Map<Catalog, StateFlow<PagingData<MovieNew>>> {
        val catalogsResponse = catalogRepository.getMovieCatalog()
        when (catalogsResponse) {
            is ApiResult.Success -> {
                val catalogToMovies = catalogsResponse.data.member.associateWith { catalog ->
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

            is ApiResult.Error -> {
                MoviesScreenUiState.Error(catalogsResponse.error.asUiText(catalogsResponse.message))
            }
        }
        return emptyMap()
    }

    private suspend fun fetchGenresAndMovies(
        genreRepository: GenreRepository,
        userRepository: UserRepository
    ): Map<Genre, StateFlow<PagingData<MovieNew>>> {
        val genresResponse = genreRepository.getMovieGenre()
        when (genresResponse) {
            is ApiResult.Success -> {
                Logger.d { "🎬 Creating genre paging sources for ${genresResponse.data.member.size} genres" }
                val genreToMovies = genresResponse.data.member.associateWith { genre ->
                    Logger.d { "🎬 Creating paging source for genre: ${genre.name} (id: ${genre.id})" }
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
                Logger.d { "🎬 Created ${genreToMovies.size} genre paging sources" }
                return genreToMovies
            }

            is ApiResult.Error -> {
                _uiState.value = MoviesScreenUiState.Error(genresResponse.error.asUiText(genresResponse.message))
            }
        }
        return emptyMap()
    }

    fun retryOperation() {
        fetchMovieScreenData()
    }
    
    fun handlePagingError(errorMessage: UiText) {
        Logger.e { "🚨 MoviesScreenViewModel.handlePagingError called with message: $errorMessage" }
        Logger.e { "🚨 Setting UI state to Error" }
        _uiState.value = MoviesScreenUiState.Error(errorMessage)
        Logger.e { "🚨 UI state updated to: ${_uiState.value}" }
    }
}

sealed interface MoviesScreenUiState {
    data object Loading : MoviesScreenUiState
    data class Error(val message: UiText) : MoviesScreenUiState
    data class Ready(
        val catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
        val genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
        val streamingProviders: List<StreamingProvider>
    ) : MoviesScreenUiState
}
