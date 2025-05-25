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

package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.jetstream.R
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.common.DisplayMovieGenericText
import com.google.jetstream.presentation.common.IMDbLogo
import com.google.jetstream.presentation.common.StreamingProviderIcon
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.JetStreamButtonShape
import com.google.jetstream.presentation.utils.formatDuration
import com.google.jetstream.presentation.utils.formatPLot
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieDetails(
    selectedMovie: MovieNew,
    openVideoPlayer: (movieId: String) -> Unit,
) {
    val childPadding = rememberChildPadding()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .height(432.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.55f)) {
            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier.padding(start = childPadding.start)
            ) {
                MovieLargeTitle(movieTitle = selectedMovie.title)
                if (selectedMovie.tagLine != null) {
                    MovieTagLine(movieTagline = selectedMovie.tagLine)
                }

                Column(
                    modifier = Modifier.alpha(0.75f)
                ) {
                    DotSeparatedRow(
                        modifier = Modifier.padding(top = 20.dp),
                        texts = listOf(
                            (selectedMovie.releaseDate?.substring(0, 4)
                                ?: "-") + " (${selectedMovie.countries.first().iso31661})",
                            selectedMovie.genres.joinToString(", ") { it.name },
                            selectedMovie.duration?.formatDuration() ?: "0h 0m"
                        )
                    )
                    MovieDescription(description = selectedMovie.plot.toString())
                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        IMDbLogo()
                        Spacer(modifier = Modifier.width(8.dp))
                        DisplayMovieGenericText(
                            "${
                                selectedMovie.getImdbRating()
                            }/10 - ${selectedMovie.imdbVotes.toString().formatVotes()} IMDB Votes"
                        )
                    }


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        selectedMovie.streamingProviders.take(5).forEach { streamingProvider ->
                            if (streamingProvider.logoPath != null) {
                                StreamingProviderIcon(
                                    modifier = Modifier.padding(top = 16.dp),
                                    logoPath = streamingProvider.logoPath,
                                    contentDescription = "Prime Video",
                                )
                                Spacer(Modifier.width(16.dp))
                            }

                        }
                    }
                }

                if (selectedMovie.video != null) {
                    WatchTrailerButton(
                        modifier = Modifier.onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch { bringIntoViewRequester.bringIntoView() }
                            }
                        },
                        openVideoPlayer = openVideoPlayer,
                        selectedMovie = selectedMovie
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchTrailerButton(
    modifier: Modifier = Modifier,
    selectedMovie: MovieNew,
    openVideoPlayer: (movieId: String) -> Unit,
) {
    Button(
        onClick = { openVideoPlayer(selectedMovie.id.toString()) },
        modifier = modifier.padding(top = 24.dp),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Watch Now",
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun DirectorScreenplayMusicRow(
    director: String,
    screenplay: String,
    music: String
) {
    Row(modifier = Modifier.padding(top = 32.dp)) {
        TitleValueText(
            modifier = Modifier
                .padding(end = 32.dp)
                .weight(1f),
            title = stringResource(R.string.director),
            value = director,
            valueColor = Color.White.copy(alpha = 0.9f)
        )

        TitleValueText(
            modifier = Modifier
                .padding(end = 32.dp)
                .weight(1f),
            title = stringResource(R.string.screenplay),
            value = screenplay,
            valueColor = Color.White.copy(alpha = 0.9f)
        )

        TitleValueText(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.music),
            value = music,
            valueColor = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun MovieDescription(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.titleSmall.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.9f)
        ),
        modifier = Modifier.padding(top = 8.dp),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun MovieLargeTitle(movieTitle: String) {
    Text(
        text = movieTitle,
        style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        ),
        maxLines = 1
    )
}

@Composable
private fun MovieTagLine(movieTagline: String) {
    Text(
        text = movieTagline,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.9f)
        ),
        maxLines = 1
    )
}