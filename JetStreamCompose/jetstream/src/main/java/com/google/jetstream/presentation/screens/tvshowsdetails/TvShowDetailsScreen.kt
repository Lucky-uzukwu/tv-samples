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

package com.google.jetstream.presentation.screens.tvshowsdetails

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.tv.material3.MaterialTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.TvShowsRow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.screens.movies.CastAndCrewList
import com.google.jetstream.presentation.screens.movies.MovieDetails
import com.google.jetstream.presentation.screens.movies.TitleValueText
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
    similarTvShows: StateFlow<PagingData<TvShow>>,
    openVideoPlayer: (tvShowId: String) -> Unit,
    onBackPressed: () -> Unit,
    refreshScreenWithNewMovie: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val childPadding = rememberChildPadding()

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
            MovieDetails(
                openVideoPlayer = openVideoPlayer,
                id = tvShow.id,
                title = tvShow.title,
                tagLine = tvShow.tagLine,
                releaseDate = tvShow.releaseDate,
                countries = tvShow.countries,
                genres = tvShow.genres,
                duration = tvShow.duration,
                plot = tvShow.plot,
                imdbRating = tvShow.imdbRating,
                imdbVotes = tvShow.imdbVotes,
                streamingProviders = tvShow.streamingProviders,
                video = null
            )
        }

        if (tvShow.tvShowPeople?.isNotEmpty() == true) {
            item {
                CastAndCrewList(
                    castAndCrew = tvShow.tvShowPeople.map { it.person }
                )
            }
        }

        item {
            TvShowsRow(
                title = StringConstants
                    .Composable
                    .movieDetailsScreenSimilarTo(tvShow.title.toString()),
                titleStyle = MaterialTheme.typography.titleMedium,
                tvShows = similarTvShows,
                onMovieSelected = refreshScreenWithNewMovie
            )
        }

//        item {
//            MovieReviews(
//                modifier = Modifier.padding(top = childPadding.top),
//                reviewsAndRatings = tvShow.getImdbRating()
//            )
//        }

        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = childPadding.start)
                    .padding(BottomDividerPadding)
                    .fillMaxWidth()
                    .height(1.dp)
                    .alpha(0.15f)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = childPadding.start),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val itemModifier = Modifier.width(192.dp)

                TitleValueText(
                    modifier = itemModifier,
                    title = stringResource(R.string.status),
                    value = "Released",
                    valueColor = Color.White
                )
                tvShow.languages?.first()?.englishName?.let {
                    TitleValueText(
                        modifier = itemModifier,
                        title = stringResource(R.string.original_language),
                        value = it,
                        valueColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MovieImageWithGradients(
    title: String,
    backdropImagePath: String,
    modifier: Modifier = Modifier,
) {
    val imageUrl = "https://stage.nortv.xyz/storage/$backdropImagePath"
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(imageUrl)
            .crossfade(true).build(),
        contentDescription = StringConstants
            .Composable
            .ContentDescription
            .moviePoster(title), modifier = modifier
    )
}


private val BottomDividerPadding = PaddingValues(vertical = 48.dp)
