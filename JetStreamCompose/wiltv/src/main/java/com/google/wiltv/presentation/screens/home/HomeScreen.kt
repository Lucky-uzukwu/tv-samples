package com.google.wiltv.presentation.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
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
import com.google.wiltv.presentation.utils.getErrorState
import com.google.wiltv.presentation.utils.hasError

@OptIn(ExperimentalTvMaterial3Api::class)
val carouselSaver =
    Saver<CarouselState, Int>(save = { it.activeItemIndex }, restore = { CarouselState(it) })


@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun HomeScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = homeScreeViewModel.heroSectionMovies.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    // Monitor paging errors and propagate to ViewModel
    LaunchedEffect(featuredMovies.hasError()) {
        if (featuredMovies.hasError()) {
            featuredMovies.getErrorState()?.let { errorText ->
                homeScreeViewModel.handlePagingError(errorText)
            }
        }
    }

    when (val currentState = uiState) {
        is HomeScreenUiState.Ready -> {
            val backgroundState = backgroundImageState()
            CatalogLayout(
                featuredMovies = featuredMovies,
                catalogToMovies = currentState.catalogToMovies,
                genreToMovies = null,
                onMovieClick = onMovieClick,
                goToVideoPlayer = goToVideoPlayer,
                setSelectedMovie = setSelectedMovie,
                carouselState = carouselState,
                backgroundState = backgroundState,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "Home Screen",
                streamingProviders = currentState.streamingProviders,
                onStreamingProviderClick = onStreamingProviderClick,
                focusManagementConfig = FocusManagementConfig(
                    enableFocusRestoration = true,
                    enableFocusLogging = true
                ),
                onRowError = { errorText ->
                    homeScreeViewModel.handlePagingError(errorText)
                }
            )
        }

        is HomeScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> ErrorScreen(
            uiText = currentState.message,
            onRetry = {
                homeScreeViewModel
                    .retryOperation()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}


