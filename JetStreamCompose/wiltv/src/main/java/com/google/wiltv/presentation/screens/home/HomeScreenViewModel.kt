package com.google.wiltv.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.AppDatabase
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
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
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
    private val appDatabase: AppDatabase,
    private val streamingProvidersRepository: StreamingProvidersRepository
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
        PagingConfig(pageSize = 5, initialLoadSize = 5)
    ) {
        MoviesHeroSectionPagingSource(movieRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
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
                            val streamingProviders =
                                streamingProvidersRepository.getStreamingProviders(
                                    type = "App\\Models\\TvShow"
                                ).firstOrNull() ?: emptyList()

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
}

sealed interface HomeScreenUiState {
    data object Loading : HomeScreenUiState
    data class Error(val message: UiText) : HomeScreenUiState
    data class Ready(
        val catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
        val genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
        val streamingProviders: List<StreamingProvider>
    ) : HomeScreenUiState
}
