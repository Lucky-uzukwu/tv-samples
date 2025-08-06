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
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val heroSectionMovies: StateFlow<PagingData<MovieNew>> = Pager(
        PagingConfig(pageSize = 5, initialLoadSize = 5)
    ) {
        MoviesHeroSectionPagingSource(movieRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )
    
    // Cache catalog and genre movies to prevent refetching on navigation
    private val cachedCatalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>> by lazy {
        kotlinx.coroutines.runBlocking {
            fetchCatalogsAndMovies(catalogRepository, userRepository)
        }
    }
    
    private val cachedGenreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>> by lazy {
        kotlinx.coroutines.runBlocking {
            fetchGenresAndMovies(genreRepository, userRepository)
        }
    }
    
    private val cachedStreamingProviders: List<StreamingProvider> by lazy {
        kotlinx.coroutines.runBlocking {
            streamingProvidersRepository.getStreamingProviders(
                type = "App\\Models\\Movie"
            ).firstOrNull() ?: emptyList()
        }
    }

    // UI State - only check token validity, use cached data
    val uiState: StateFlow<HomeScreenUiState> = combine(
        userRepository.userToken,
    ) { token ->
        val token = token.firstOrNull() ?: ""
        when {
            token.isEmpty() -> HomeScreenUiState.Error
            else -> {
                // Use cached paging sources - no refetch on recomposition
                HomeScreenUiState.Ready(
                    catalogToMovies = cachedCatalogToMovies,
                    genreToMovies = cachedGenreToMovies,
                    streamingProviders = cachedStreamingProviders
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
