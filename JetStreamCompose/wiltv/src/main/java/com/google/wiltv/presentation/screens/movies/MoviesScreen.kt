package com.google.wiltv.presentation.screens.movies

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.presentation.common.CatalogLayout
import com.google.wiltv.presentation.common.FocusManagementConfig
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.backgroundImageState
import com.google.wiltv.presentation.screens.home.carouselSaver
import com.google.wiltv.presentation.utils.getErrorState
import com.google.wiltv.presentation.utils.hasError

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun MoviesScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    navController: NavController,
    moviesScreenViewModel: MoviesScreenViewModel = hiltViewModel(),
) {
    val uiState by moviesScreenViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = moviesScreenViewModel.heroSectionMovies.collectAsLazyPagingItems()
    val watchlistItemIds by moviesScreenViewModel.watchlistItemIds.collectAsStateWithLifecycle()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    // Monitor paging errors and propagate to ViewModel
    LaunchedEffect(featuredMovies.hasError()) {
        if (featuredMovies.hasError()) {
            featuredMovies.getErrorState()?.let { errorText ->
                moviesScreenViewModel.handlePagingError(errorText)
            }
        }
    }

    when (val currentState = uiState) {
        is MoviesScreenUiState.Ready -> {
            val backgroundState = backgroundImageState()
            CatalogLayout(
                featuredMovies = featuredMovies,
                catalogToMovies = currentState.catalogToMovies,
                genreToMovies = currentState.genreToMovies,
                onMovieClick = onMovieClick,
                goToVideoPlayer = goToVideoPlayer,
                carouselState = carouselState,
                backgroundState = backgroundState,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Movie Screen",
                streamingProviders = currentState.streamingProviders,
                onStreamingProviderClick = onStreamingProviderClick,
                focusManagementConfig = FocusManagementConfig(
                    enableFocusRestoration = true,
                    enableFocusLogging = true
                ),
                onRowError = { errorText ->
                    moviesScreenViewModel.handlePagingError(errorText)
                },
                watchlistItemIds = watchlistItemIds
            )
        }

        is MoviesScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is MoviesScreenUiState.Error -> ErrorScreen(
            uiText = currentState.message,
            onRetry = {
                moviesScreenViewModel
                    .retryOperation()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

