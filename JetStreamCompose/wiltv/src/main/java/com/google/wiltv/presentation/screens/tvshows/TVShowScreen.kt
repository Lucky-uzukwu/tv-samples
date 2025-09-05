package com.google.wiltv.presentation.screens.tvshows

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.common.FocusManagementConfig
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.TvCatalogLayout
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.backgroundImageState
import com.google.wiltv.presentation.screens.home.carouselSaver
import com.google.wiltv.presentation.utils.getErrorState
import com.google.wiltv.presentation.utils.hasError

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun TVShowScreen(
    onTVShowClick: (tvShow: TvShow) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    tvShowScreenViewModel: TvShowScreenViewModel = hiltViewModel(),
) {
    val uiState by tvShowScreenViewModel.uiState.collectAsStateWithLifecycle()
    val heroSectionTvShows = tvShowScreenViewModel.heroSectionTvShows.collectAsLazyPagingItems()
    val watchlistItemIds by tvShowScreenViewModel.watchlistItemIds.collectAsStateWithLifecycle()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    // Monitor paging errors and propagate to ViewModel
    LaunchedEffect(heroSectionTvShows.hasError()) {
        if (heroSectionTvShows.hasError()) {
            heroSectionTvShows.getErrorState()?.let { errorText ->
                tvShowScreenViewModel.handlePagingError(errorText)
            }
        }
    }

    when (val currentState = uiState) {
        is TvShowScreenUiState.Ready -> {
            val backgroundState = backgroundImageState()
            TvCatalogLayout(
                featuredTvShows = heroSectionTvShows,
                catalogToTvShows = currentState.catalogToTvShows,
                genreToTvShows = currentState.genreToTvShows,
                onTvShowClick = onTVShowClick,
                carouselState = carouselState,
                backgroundState = backgroundState,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "TV Shows Screen",
                streamingProviders = currentState.streamingProviders,
                onStreamingProviderClick = onStreamingProviderClick,
                focusManagementConfig = FocusManagementConfig(
                    enableFocusRestoration = true,
                    enableFocusLogging = true
                ),
                watchlistItemIds = watchlistItemIds
            )
        }

        is TvShowScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is TvShowScreenUiState.Error -> ErrorScreen(
            uiText = currentState.message,
            onRetry = {
                tvShowScreenViewModel
                    .retryOperation()
            },
            modifier = Modifier.fillMaxSize()
        )

    }
}

