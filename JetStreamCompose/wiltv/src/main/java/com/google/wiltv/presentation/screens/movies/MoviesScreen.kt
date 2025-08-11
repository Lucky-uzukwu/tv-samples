package com.google.wiltv.presentation.screens.movies

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun MoviesScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    navController: NavController,
    moviesScreenViewModel: MoviesScreenViewModel = hiltViewModel(),
) {
    val uiState by moviesScreenViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = moviesScreenViewModel.heroSectionMovies.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    when (val currentState = uiState) {
        is MoviesScreenUiState.Ready -> {
            val backgroundState = backgroundImageState()
            CatalogLayout(
                featuredMovies = featuredMovies,
                catalogToMovies = currentState.catalogToMovies,
                genreToMovies = currentState.genreToMovies,
                onMovieClick = onMovieClick,
                goToVideoPlayer = goToVideoPlayer,
                setSelectedMovie = setSelectedMovie,
                carouselState = carouselState,
                backgroundState = backgroundState,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Movie Screen",
                streamingProviders = currentState.streamingProviders,
                onStreamingProviderClick = onStreamingProviderClick,
                focusManagementConfig = FocusManagementConfig(
                    enableFocusRestoration = true,
                    enableFocusLogging = true
                )
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

