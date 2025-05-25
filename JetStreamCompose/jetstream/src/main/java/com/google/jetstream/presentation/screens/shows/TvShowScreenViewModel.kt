package com.google.jetstream.presentation.screens.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.data.pagingsources.tvshow.TvShowPagingSources
import com.google.jetstream.data.pagingsources.tvshow.TvShowsHeroSectionPagingSource
import com.google.jetstream.data.repositories.CatalogRepository
import com.google.jetstream.data.repositories.GenreRepository
import com.google.jetstream.data.repositories.StreamingProvidersRepository
import com.google.jetstream.data.repositories.TvShowsRepository
import com.google.jetstream.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TvShowScreenViewModel @Inject constructor(
    private val tvShowRepository: TvShowsRepository,
    userRepository: UserRepository,
    catalogRepository: CatalogRepository,
    genreRepository: GenreRepository,
    streamingProvidersRepository: StreamingProvidersRepository
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

    // UI State combining all data
    val uiState: StateFlow<TvShowScreenUiState> = combine(
        userRepository.userToken,
    ) { token ->
        val token = token.firstOrNull() ?: ""
        when {
            token.isEmpty() -> TvShowScreenUiState.Error
            else -> {
                // Create paginated flows for each catalog
                val catalogAndTvShows = fetchCatalogsAndTvShows(
                    catalogRepository,
                    token,
                    userRepository
                )
                val genreAndTvShows = fetchTvShowsByGenre(
                    genreRepository = genreRepository,
                    token,
                    userRepository
                )

                val streamingProviders =
                    streamingProvidersRepository.getStreamingProviders(token).firstOrNull()

                TvShowScreenUiState.Ready(
                    catalogToTvShows = catalogAndTvShows,
                    genreToTvShows = genreAndTvShows,
                    streamingProviders = streamingProviders ?: emptyList()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TvShowScreenUiState.Loading
    )

    private suspend fun fetchCatalogsAndTvShows(
        catalogRepository: CatalogRepository,
        token: String,
        userRepository: UserRepository
    ): Map<Catalog, StateFlow<PagingData<TvShow>>> {
        val catalogs = catalogRepository.getTvShowCatalog(token).firstOrNull() ?: emptyList()
        val catalogToTvShowPagingSource = catalogs.associateWith { catalog ->
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

    private suspend fun fetchTvShowsByGenre(
        genreRepository: GenreRepository,
        token: String,
        userRepository: UserRepository
    ): Map<Genre, StateFlow<PagingData<TvShow>>> {
        val genres = genreRepository.getTvShowsGenre(token).firstOrNull() ?: emptyList()
        val genreToTvShowPagingData = genres.associateWith { genre ->
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

}

sealed interface TvShowScreenUiState {
    data object Loading : TvShowScreenUiState
    data object Error : TvShowScreenUiState
    data class Ready(
        val catalogToTvShows: Map<Catalog, StateFlow<PagingData<TvShow>>>,
        val genreToTvShows: Map<Genre, StateFlow<PagingData<TvShow>>>,
        val streamingProviders: List<StreamingProvider>
    ) : TvShowScreenUiState
}
