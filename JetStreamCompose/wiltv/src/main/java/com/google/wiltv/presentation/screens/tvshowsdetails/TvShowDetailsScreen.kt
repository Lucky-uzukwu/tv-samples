/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.wiltv.presentation.screens.tvshowsdetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.wiltv.presentation.common.EnhancedBackdropImage
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.models.Season
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.screens.tvshows.TvShowDetails
import com.google.wiltv.presentation.screens.tvshowsdetails.SeasonsAndEpisodes
import java.net.URLEncoder
import android.util.Log
import com.google.wiltv.presentation.screens.moviedetails.BackdropImageWithGradients

object TvShowDetailsScreen {
    const val TvShowIdBundleKey = "tvShowId"
}

@Composable
fun TvShowDetailsScreen(
    openVideoPlayer: (tvShowId: String) -> Unit,
    onBackPressed: () -> Unit,
    onNewTvShowSelected: (TvShow) -> Unit,
    tvShowDetailsScreenViewModel: TvShowDetailsScreenViewModel = hiltViewModel(),
) {
    val uiState by tvShowDetailsScreenViewModel.uiState.collectAsStateWithLifecycle()
    val isInWatchlist by tvShowDetailsScreenViewModel.isInWatchlist.collectAsStateWithLifecycle()
    val watchlistLoading by tvShowDetailsScreenViewModel.watchlistLoading.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is TvShowDetailsScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        is TvShowDetailsScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is TvShowDetailsScreenUiState.Done -> {
            Details(
                tvShow = s.tvShow,
                seasons = s.seasons,
                openVideoPlayer = openVideoPlayer,
                onBackPressed = onBackPressed,
                isInWatchlist = isInWatchlist,
                watchlistLoading = watchlistLoading,
                onToggleWatchlist = tvShowDetailsScreenViewModel::toggleWatchlist,
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize()
            )
        }
    }
}

@Composable
private fun Details(
    tvShow: TvShow,
    seasons: List<Season>,
    openVideoPlayer: (tvShowId: String) -> Unit,
    onBackPressed: () -> Unit,
    isInWatchlist: Boolean,
    watchlistLoading: Boolean,
    onToggleWatchlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val lazyListState = rememberLazyListState()
    val playButtonFocusRequester = remember { FocusRequester() }
    val watchlistButtonFocusRequester = remember { FocusRequester() }
    val episodesTabFocusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize()) {
        BackdropImageWithGradients(
            title = tvShow.title ?: tvShow.tagLine ?: "",
            backdropUrl = tvShow.backdropImageUrl ?: "",
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
    }


    BackHandler(onBack = onBackPressed)
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp),
    ) {
        item {
            TvShowDetails(
                openVideoPlayer = openVideoPlayer,
                id = tvShow.id,
                title = tvShow.title,
                releaseDate = tvShow.releaseDate,
                genres = tvShow.genres,
                duration = tvShow.duration,
                plot = tvShow.plot,
                streamingProviders = tvShow.streamingProviders,
                seasons = tvShow.seasons,
                playButtonFocusRequester = playButtonFocusRequester,
                watchlistButtonFocusRequester = watchlistButtonFocusRequester,
                episodesTabFocusRequester = episodesTabFocusRequester,
                onPlayButtonFocused = null,
                isInWatchlist = isInWatchlist,
                watchlistLoading = watchlistLoading,
                onToggleWatchlist = onToggleWatchlist
            )
        }

        item {
            SeasonsAndEpisodes(
                seasons = seasons.ifEmpty { tvShow.seasons ?: emptyList() },
                playButtonFocusRequester = playButtonFocusRequester,
                episodesTabFocusRequester = episodesTabFocusRequester,
                onEpisodeClick = { episode ->
                    Log.d("TvShowDetails", "Episode clicked: ${episode.title}")
                    Log.d("TvShowDetails", "Episode video: ${episode.video}")
                    Log.d("TvShowDetails", "HLS URL: ${episode.video?.hlsPlaylistUrl}")
                    
                    episode.video?.hlsPlaylistUrl?.let { url ->
                        Log.d("TvShowDetails", "Navigating to video player with URL: $url")
                        // Encode URL for navigation routing (not for auth - preserves pre-signed params)
                        val encodedUrl = URLEncoder.encode(url, "UTF-8")
                        Log.d("TvShowDetails", "URL-encoded for navigation: $encodedUrl")
                        openVideoPlayer(encodedUrl)
                    } ?: run {
                        Log.d("TvShowDetails", "No video available for episode: ${episode.title}")
                    }
                },
                modifier = Modifier.padding(top = 24.dp)
            )
        }

    }
}

@Composable
private fun MovieImageWithGradients(
    title: String,
    backdropImagePath: String,
    modifier: Modifier = Modifier,
) {
    val imageUrl = "https://api.nortv.xyz/storage/$backdropImagePath"
    EnhancedBackdropImage(
        title = title,
        backdropUrl = imageUrl,
        modifier = modifier
    )
}


