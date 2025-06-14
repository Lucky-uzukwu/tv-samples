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

package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.utils.bringIntoViewIfChildrenAreFocused
import com.google.jetstream.presentation.utils.formatPLot
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating

@Composable
fun ImmersiveShowsList(
    tvShows: LazyPagingItems<TvShow>,
    sectionTitle: String? = stringResource(R.string.top_10_movies_title),
    modifier: Modifier = Modifier,
    setSelectedTvShow: (TvShow) -> Unit,
    gradientColor: Color = Color.Black.copy(alpha = 0.7f),
    onTvShowClick: (tvShow: TvShow) -> Unit
) {
    var isListFocused by remember { mutableStateOf(false) }

    var selectedTvShow by remember(tvShows) {
        mutableStateOf(tvShows.itemSnapshotList.firstOrNull())
    }

    LaunchedEffect(Unit) {
        if (tvShows.itemSnapshotList.items.isNotEmpty()) {
            selectedTvShow = tvShows.itemSnapshotList.items.first()
            setSelectedTvShow(selectedTvShow!!)
        }
    }


    ImmersiveList(
        selectedTvShow = selectedTvShow ?: return,
        isListFocused = isListFocused,
        gradientColor = gradientColor,
        tvShows = tvShows,
        sectionTitle = sectionTitle,
        onMovieClick = onTvShowClick,
        onMovieFocused = {
            selectedTvShow = it
            setSelectedTvShow(it)
        },
        onFocusChanged = {
            isListFocused = it.hasFocus
        },
        modifier = modifier.bringIntoViewIfChildrenAreFocused(
            PaddingValues(bottom = 50.dp)
        )
    )

}

@Composable
private fun ImmersiveList(
    selectedTvShow: TvShow,
    isListFocused: Boolean,
    gradientColor: Color,
    tvShows: LazyPagingItems<TvShow>,
    sectionTitle: String?,
    onFocusChanged: (FocusState) -> Unit,
    onMovieFocused: (TvShow) -> Unit,
    onMovieClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = modifier
    ) {
        Background(
            movie = selectedTvShow,
            visible = isListFocused,
            modifier = modifier
                .height(500.dp)
                .gradientOverlay(gradientColor)
        )
        Column {
            // TODO HERE you can add more deails for each row
            if (isListFocused) {
                TvShowDescription(
                    tvShow = selectedTvShow,
                    modifier = Modifier.padding(
                        start = rememberChildPadding().start,
                        bottom = 10.dp
                    )
                )
            }

            ImmersiveListShowsRow(
                tvShows = tvShows,
                itemDirection = ItemDirection.Horizontal,
                title = sectionTitle,
                showIndexOverImage = false,
                onMovieSelected = onMovieClick,
                isListFocused = isListFocused,
                onMovieFocused = onMovieFocused,
                modifier = Modifier.onFocusChanged(onFocusChanged)
            )
        }
    }
}

@Composable
private fun Background(
    movie: TvShow,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Crossfade(
            targetState = movie,
            label = "posterUriCrossfade",

            ) {
            it.title?.let { movieTitle ->
                PosterImage(
                    movieTitle = movieTitle,
                    movieUri = imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun TvShowDescription(
    tvShow: TvShow,
    modifier: Modifier = Modifier,
) {
    val combinedGenre = tvShow.genres?.joinToString(" ") { genre -> genre.name }
    val getYear = tvShow.releaseDate?.substring(0, 4)

    Column(
        modifier = modifier.padding(
            start = 32.dp
        ),
    ) {
        tvShow.title?.let {
            Text(
                modifier = Modifier.padding(top = 64.dp),
                text = it,
                maxLines = 2,
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontSize = 40.sp,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        val formattedPlot = tvShow.plot.formatPLot()
        DisplayFilmGenericText(formattedPlot)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            IMDbLogo()
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${
                    tvShow.imdbRating.getImdbRating()
                }/10 - ${tvShow.imdbVotes.toString().formatVotes()} IMDB Votes",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                color = Color.White,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        DisplayFilmExtraInfo(
            getYear,
            combinedGenre,
            tvShow.duration,
            style = MaterialTheme.typography.bodyLarge,
        )

    }
}

private fun Modifier.gradientOverlay(gradientColor: Color): Modifier =
    drawWithCache {
        val horizontalGradient = Brush.horizontalGradient(
            colors = listOf(
                gradientColor,
                Color.Transparent
            ),
            startX = size.width.times(0.2f),
            endX = size.width.times(0.7f)
        )
        val verticalGradient = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                gradientColor
            ),
            endY = size.width.times(0.3f)
        )
        val linearGradient = Brush.linearGradient(
            colors = listOf(
                gradientColor,
                Color.Transparent
            ),
            start = Offset(
                size.width.times(0.2f),
                size.height.times(0.5f)
            ),
            end = Offset(
                size.width.times(0.9f),
                0f
            )
        )

        onDrawWithContent {
            drawContent()
            drawRect(horizontalGradient)
            drawRect(verticalGradient)
            drawRect(linearGradient)
        }
    }
