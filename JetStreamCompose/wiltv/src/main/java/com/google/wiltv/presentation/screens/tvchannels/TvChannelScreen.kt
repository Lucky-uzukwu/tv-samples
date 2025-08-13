// ABOUTME: Main screen composable for displaying TV channels in a catalog layout
// ABOUTME: Manages channel selection, video playback navigation, and streaming provider integration

package com.google.wiltv.presentation.screens.tvchannels

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
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.common.FocusManagementConfig
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.TvChannelCatalogLayout
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.backgroundImageState
import com.google.wiltv.presentation.screens.home.carouselSaver
import com.google.wiltv.presentation.utils.getErrorState
import com.google.wiltv.presentation.utils.hasError

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun TvChannelScreen(
    goToVideoPlayer: (channel: TvChannel) -> Unit,
    onGenreClick: (genre: Genre) -> Unit,
    tvChannelScreenViewModel: TvChannelScreenViewModel = hiltViewModel(),
) {
    val uiState by tvChannelScreenViewModel.uiState.collectAsStateWithLifecycle()
    val heroSectionTvChannels = tvChannelScreenViewModel.heroSectionTvChannels.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    // Monitor paging errors and propagate to ViewModel
    LaunchedEffect(heroSectionTvChannels.hasError()) {
        if (heroSectionTvChannels.hasError()) {
            heroSectionTvChannels.getErrorState()?.let { errorText ->
                tvChannelScreenViewModel.handlePagingError(errorText)
            }
        }
    }

    when (val currentState = uiState) {
        is TvChannelScreenUiState.Ready -> {
            val backgroundState = backgroundImageState()
            TvChannelCatalogLayout(
                featuredTvChannels = heroSectionTvChannels,
                genreToTvChannels = currentState.genreToTvChannels,
                onChannelClick = goToVideoPlayer,
                carouselState = carouselState,
                backgroundState = backgroundState,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "TV Channels Screen",
                genres = currentState.genres,
                onGenreClick = onGenreClick,
                focusManagementConfig = FocusManagementConfig(
                    enableFocusRestoration = true,
                    enableFocusLogging = true
                )
            )
        }

        is TvChannelScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is TvChannelScreenUiState.Error -> ErrorScreen(
            uiText = currentState.message,
            onRetry = {
                tvChannelScreenViewModel
                    .retryOperation()
            },
            modifier = Modifier.fillMaxSize()
        )

    }
}