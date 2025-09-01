/*
 * Copyright 2024 Google LLC
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.data.models.Episode
import com.google.wiltv.data.models.Season
import com.google.wiltv.presentation.common.ItemDirection
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.MoviesRowItemImage
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
import android.util.Log

@Composable
fun SeasonsAndEpisodes(
    seasons: List<Season>,
    playButtonFocusRequester: FocusRequester,
    episodesTabFocusRequester: FocusRequester,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()

    if (seasons.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(childPadding.start),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = stringResource(R.string.no_episodes_available),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        return
    }

    Column(
        modifier = modifier
            .focusGroup()
            .focusRequester(episodesTabFocusRequester)
    ) {
        seasons.forEachIndexed { seasonIndex, season ->
            SeasonWithEpisodes(
                season = season,
                seasonIndex = seasonIndex,
                playButtonFocusRequester = playButtonFocusRequester,
                onEpisodeClick = onEpisodeClick,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun SeasonWithEpisodes(
    season: Season,
    seasonIndex: Int,
    playButtonFocusRequester: FocusRequester,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()

    Column(modifier = modifier) {
        Text(
            text = "Season ${season.number ?: (seasonIndex + 1)}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            modifier = Modifier.padding(
                start = childPadding.start,
                bottom = 16.dp
            )
        )

        val episodes = season.episodes ?: emptyList()

        if (episodes.isEmpty()) {
            Text(
                text = "No episodes available for this season",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(
                    horizontal = childPadding.start,
                    vertical = 8.dp
                )
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(
                    start = childPadding.start,
                    end = childPadding.end
                )
            ) {
                itemsIndexed(episodes) { episodeIndex, episode ->
                    EpisodeCard(
                        episode = episode,
                        episodeIndex = episodeIndex,
                        isFirstSeason = seasonIndex == 0,
                        isFirstEpisode = episodeIndex == 0,
                        playButtonFocusRequester = playButtonFocusRequester,
                        onEpisodeClick = onEpisodeClick,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    episodeIndex: Int,
    isFirstSeason: Boolean,
    isFirstEpisode: Boolean,
    playButtonFocusRequester: FocusRequester,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val firstEpisodeFocusRequester = remember { FocusRequester() }

    MovieCard(
        onClick = {
            Log.d("EpisodeCard", "MovieCard clicked for episode: ${episode.title}")
            onEpisodeClick(episode)
        },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.8f),
            focusedContainerColor = Color.Black.copy(alpha = 0.9f)
        ),
        modifier = modifier
            .width(280.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .let { cardModifier ->
                if (isFirstSeason && isFirstEpisode) {
                    cardModifier.focusRequester(firstEpisodeFocusRequester)
                } else {
                    cardModifier
                }
            }
    ) {
        Column {
            if (episode.posterImageUrl != null) {
                MoviesRowItemImage(
                    modifier = Modifier.aspectRatio(ItemDirection.Horizontal.aspectRatio),
                    showIndexOverImage = false,
                    movieTitle = episode.title,
                    movieUri = episode.posterImageUrl,
                    index = episodeIndex
                )
            } else {
                Box(
                    modifier = Modifier
                        .aspectRatio(ItemDirection.Horizontal.aspectRatio)
                        .background(Color.Gray.copy(alpha = 0.3f))
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Episode ${episodeIndex + 1}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Episode ${episodeIndex + 1}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                episode.tagLine?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (episode.video == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (episode.plot != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = episode.plot,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}