package com.google.wiltv.presentation.screens.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.Catalog
import com.google.wiltv.data.paging.pagingsources.tvshow.TvShowPagingSources
import com.google.wiltv.data.paging.pagingsources.tvshow.TvShowsHeroSectionPagingSource
import com.google.wiltv.data.repositories.CatalogRepository
import com.google.wiltv.data.repositories.GenreRepository
import com.google.wiltv.data.repositories.StreamingProvidersRepository
import com.google.wiltv.data.repositories.TvShowsRepository
import com.google.wiltv.data.repositories.UserRepository
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowScreenViewModel @Inject constructor(
    private val tvShowRepository: TvShowsRepository,
    private val userRepository: UserRepository,
    private val catalogRepository: CatalogRepository,
    private val genreRepository: GenreRepository,
    private val streamingProvidersRepository: StreamingProvidersRepository
) : ViewModel() {

    val heroSectionTvShows: StateFlow<PagingData<TvShow>> = Pager(
        PagingConfig(pageSize = 5, initialLoadSize = 5)
    ) {
        TvShowsHeroSectionPagingSource(tvShowRepository, userRepository)
    }.flow.cachedIn(viewModelScope).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingData.empty()
    )

    private val _uiState = MutableStateFlow<TvShowScreenUiState>(TvShowScreenUiState.Loading)
    val uiState: StateFlow<TvShowScreenUiState> = _uiState.asStateFlow()


    init {
        loadTvShowData()
    }

    fun loadTvShowData() {
        viewModelScope.launch {
            try {
                combine(
                    userRepository.userToken,
                ) { token ->
                    val token = token.firstOrNull() ?: ""
                    when {
                        token.isEmpty() -> TvShowScreenUiState.Error(UiText.DynamicString("Unauthorized"))
                        else -> {
                            val catalogToMovies = fetchCatalogsAndTvShows(
                                catalogRepository,
                                userRepository
                            )
                            val genreToMovies = fetchTvShowsByGenre(
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
                                return@combine TvShowScreenUiState.Error(UiText.DynamicString("No data found"))
                            } else {
                                return@combine TvShowScreenUiState.Ready(
                                    catalogToTvShows = catalogToMovies,
                                    genreToTvShows = genreToMovies,
                                    streamingProviders = streamingProviders
                                )
                            }
                        }
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = TvShowScreenUiState.Error(
                    message = UiText.DynamicString(
                        value = "Error fetching tv shows",
                    )
                )
            }
        }
    }

    private suspend fun fetchCatalogsAndTvShows(
        catalogRepository: CatalogRepository,
        userRepository: UserRepository
    ): Map<Catalog, StateFlow<PagingData<TvShow>>> {
        val catalogsResponse = catalogRepository.getTvShowCatalog()
        when (catalogsResponse) {
            is ApiResult.Success -> {
                val catalogToTvShowPagingSource =
                    catalogsResponse.data.member.associateWith { catalog ->
                        TvShowPagingSources().getTvShowsCatalogPagingSource(
                            catalog = catalog,
                            tvShowRepository = tvShowRepository,
                            userRepository = userRepository
                        ).cachedIn(viewModelScope).stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5_000),
                            PagingData.empty()
                        )
                    }
                return catalogToTvShowPagingSource
            }

            is ApiResult.Error -> {
                TvShowScreenUiState.Error(catalogsResponse.error.asUiText(catalogsResponse.message))
            }
        }
        return emptyMap()
    }

    private suspend fun fetchTvShowsByGenre(
        genreRepository: GenreRepository,
        userRepository: UserRepository
    ): Map<Genre, StateFlow<PagingData<TvShow>>> {
        val genresResponse = genreRepository.getTvShowsGenre()
        when (genresResponse) {
            is ApiResult.Success -> {
                val genreToTvShowPagingData = genresResponse.data.member.associateWith { genre ->
                    TvShowPagingSources().getTvShowsGenrePagingSource(
                        genreId = genre.id,
                        tvShowRepository = tvShowRepository,
                        userRepository = userRepository
                    ).cachedIn(viewModelScope).stateIn(
                        viewModelScope,
                        SharingStarted.WhileSubscribed(5_000),
                        PagingData.empty()
                    )
                }
                return genreToTvShowPagingData
            }

            is ApiResult.Error -> {
                _uiState.value = TvShowScreenUiState.Error(genresResponse.error.asUiText(genresResponse.message))
            }
        }
        return emptyMap()
    }

    fun retryOperation() {
        loadTvShowData()
    }
    
    fun handlePagingError(errorMessage: UiText) {
        _uiState.value = TvShowScreenUiState.Error(errorMessage)
    }

}

sealed interface TvShowScreenUiState {
    data object Loading : TvShowScreenUiState
    data class Error(val message: UiText) : TvShowScreenUiState
    data class Ready(
        val catalogToTvShows: Map<Catalog, StateFlow<PagingData<TvShow>>>,
        val genreToTvShows: Map<Genre, StateFlow<PagingData<TvShow>>>,
        val streamingProviders: List<StreamingProvider>
    ) : TvShowScreenUiState
}
