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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import com.google.wiltv.presentation.common.AuthenticatedAsyncImage
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.models.Season
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.screens.tvshows.TvShowDetails
import com.google.wiltv.presentation.screens.tvshowsdetails.TvShowDetailTabs
import kotlinx.coroutines.flow.StateFlow

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
                similarTvShows = s.similarTvShows,
                openVideoPlayer = openVideoPlayer,
                onBackPressed = onBackPressed,
                refreshScreenWithNewMovie = onNewTvShowSelected,
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
    similarTvShows: StateFlow<PagingData<TvShow>>,
    openVideoPlayer: (tvShowId: String) -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()
    val playButtonFocusRequester = remember { FocusRequester() }
    val episodesTabFocusRequester = remember { FocusRequester() }
    val suggestedTabFocusRequester = remember { FocusRequester() }
    val detailsTabFocusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize()) {
        MovieImageWithGradients(
            title = tvShow.title ?: tvShow.tagLine ?: "",
            backdropImagePath = tvShow.backdropImagePath ?: "",
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
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = modifier,
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
                episodesTabFocusRequester = episodesTabFocusRequester,
                onPlayButtonFocused = null
            )
        }

        item {
            TvShowDetailTabs(
                modifier = Modifier.height(500.dp),
                isFullScreen = false,
                selectedTvShow = tvShow,
                seasons = seasons,
                similarTvShows = similarTvShows,
                refreshScreenWithNewTvShow = refreshScreenWithNewMovie,
                episodesTabFocusRequester = episodesTabFocusRequester,
                suggestedTabFocusRequester = suggestedTabFocusRequester,
                detailsTabFocusRequester = detailsTabFocusRequester,
                playButtonFocusRequester = playButtonFocusRequester,
                onTabsFocusChanged = { focused -> }
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
    AuthenticatedAsyncImage(
        model = imageUrl,
        contentDescription = StringConstants
            .Composable
            .ContentDescription
            .moviePoster(title),
        modifier = modifier
    )
}


