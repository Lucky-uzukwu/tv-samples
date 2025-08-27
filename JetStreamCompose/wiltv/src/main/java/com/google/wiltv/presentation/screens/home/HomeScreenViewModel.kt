package com.google.wiltv.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.AppDatabase
import com.google.wiltv.data.models.ContinueWatchingItem
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
import com.google.wiltv.data.repositories.WatchProgressRepository
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreeViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
    private val genreRepository: GenreRepository,
    private val appDatabase: AppDatabase,
    private val streamingProvidersRepository: StreamingProvidersRepository,
    private val watchlistRepository: WatchlistRepository,
    private val watchProgressRepository: WatchProgressRepository
) : ViewModel() {


//    @OptIn(ExperimentalPagingApi::class)
//    fun fetchHeroMovies(): Flow<PagingData<MovieEntity>> =
//        Pager(
//            config = PagingConfig(
//                pageSize = PAGE_SIZE,
//                prefetchDistance = 10,
//                initialLoadSize = PAGE_SIZE,
//            ),
//            pagingSourceFactory = {
//                appDatabase.getMoviesDao().getMovies()
//            },
//            remoteMediator = MoviesRemoteMediator(
//                movieRepository = movieRepository,
//                appDatabase = appDatabase,
//                userRepository = userRepository
//            )
//        ).flow

    init {
        fetchHomeScreenData()
    }

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

    val continueWatchingItems: StateFlow<List<ContinueWatchingItem>> = userRepository.userId.flatMapLatest { userId ->
        if (userId != null) {
            watchProgressRepository.getRecentWatchProgress(userId, limit = 10).map { progressList ->
                // For now, just transform progress to items without fetching full movie details
                // This will be optimized later with a proper mapping solution
                progressList.filter { !it.completed && it.progressMs > 0 }.take(5).map { progress ->
                    ContinueWatchingItem(
                        movie = MovieNew(
                            id = progress.contentId,
                            title = "Loading...", // Placeholder until we implement proper movie details fetching
                            tagLine = null,
                            plot = null,
                            releaseDate = null,
                            duration = null,
                            imdbRating = null,
                            imdbVotes = null,
                            backdropImagePath = null,
                            posterImagePath = null,
                            youtubeTrailerUrl = null,
                            contentRating = null,
                            isAdultContent = false,
                            isKidsContent = false,
                            views = null,
                            active = true,
                            showInHeroSection = false,
                            tvShowSeasonPriority = null,
                            moviePeopleCount = null,
                            people = emptyList(),
                            theMovieDbId = null,
                            backdropImageUrl = null,
                            posterImageUrl = null,
                            genres = emptyList(),
                            countries = emptyList(),
                            languages = emptyList(),
                            catalogs = emptyList(),
                            video = null,
                            tvShowSeasonId = if (progress.contentType == "tvshow") progress.contentId else null,
                            peopleCount = null,
                            streamingProviders = emptyList()
                        ),
                        watchProgress = progress
                    )
                }
            }
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _uiState = MutableStateFlow<HomeScreenUiState>(HomeScreenUiState.Loading)
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    fun fetchHomeScreenData() {
        viewModelScope.launch {
            try {
                combine(
                    userRepository.userToken,
                ) { token ->
                    val token = token.firstOrNull() ?: ""
                    when {
                        token.isEmpty() -> HomeScreenUiState.Error(UiText.DynamicString("Unauthorized"))
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
                                return@combine HomeScreenUiState.Error(UiText.DynamicString("No data found"))
                            } else {
                                return@combine HomeScreenUiState.Ready(
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
                _uiState.value = HomeScreenUiState.Error(
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
                HomeScreenUiState.Error(catalogsResponse.error.asUiText(catalogsResponse.message))
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
                val genreToMovies = genresResponse.data.member.associateWith { genre ->
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

            is ApiResult.Error -> {
                _uiState.value = HomeScreenUiState.Error(genresResponse.error.asUiText(genresResponse.message))
            }
        }
        return emptyMap()
    }

    fun retryOperation() {
        fetchHomeScreenData()
    }
    
    fun handlePagingError(errorMessage: UiText) {
        _uiState.value = HomeScreenUiState.Error(errorMessage)
    }
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data class Error(val message: UiText) : HomeScreenUiState
    data class Ready(
        val catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
        val genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
        val streamingProviders: List<StreamingProvider>,
        val continueWatchingItems: List<ContinueWatchingItem> = emptyList()
    ) : HomeScreenUiState
}
