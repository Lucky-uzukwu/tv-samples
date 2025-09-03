package com.google.wiltv.presentation.screens.tvshows

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.Episode
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.Season
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.presentation.common.StreamingProviderIcon
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.screens.movies.WatchEpisodeButton
import com.google.wiltv.presentation.screens.movies.WatchlistButton
import com.google.wiltv.presentation.screens.tvshowsdetails.SeasonsToEpisodes
import com.google.wiltv.presentation.utils.formatDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvShowDetails(
    id: Int,
    title: String?,
    releaseDate: String?,
    genres: List<Genre>?,
    duration: Int?,
    plot: String?,
    streamingProviders: List<StreamingProvider>?,
    seasonsToEpisodes: SeasonsToEpisodes,
    onEpisodeClick: (Episode) -> Unit,
    playButtonFocusRequester: FocusRequester,
    watchlistButtonFocusRequester: FocusRequester,
    episodesTabFocusRequester: FocusRequester,
    onPlayButtonFocused: (() -> Unit)? = null,
    isInWatchlist: Boolean = false,
    watchlistLoading: Boolean = false,
    onToggleWatchlist: (() -> Unit)
) {
    val childPadding = rememberChildPadding()
    val seasons = seasonsToEpisodes.map { it.first }

    // Find the next episode to watch based on user's progress
    // For now, this defaults to S1E1, but in the future it will check watch progress
    val (targetSeason, targetEpisode, targetEpisodeData) = findNextEpisodeToWatch(seasons)

    val hasTargetEpisodeVideo = targetEpisodeData?.video != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(432.dp)
    ) {
        Column(
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = childPadding.start)
                    .fillMaxWidth(0.55f)
                    .focusGroup()
                    .focusProperties {
                        left = FocusRequester.Cancel
                        right = FocusRequester.Cancel
                        down = playButtonFocusRequester
                        // Remove right = Cancel to allow horizontal navigation between buttons
                    }
            ) {
                title?.let {
                    TvShowLargeTitle(
                        modifier = Modifier
                            .focusable(),
                        tvShowTitle = it
                    )
                }

                TvShowDotSeparatedRow(
                    modifier = Modifier.padding(top = 20.dp),
                    texts = listOf(
                        (releaseDate?.substring(0, 4) ?: "-"),
                        duration?.formatDuration() ?: "0h 0m",
                        genres?.take(3)?.joinToString(", ") { it.name },
                    )
                )
                plot?.let { TvShowDescription(description = it) }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    streamingProviders?.take(5)?.forEach { streamingProvider ->
                        if (streamingProvider.logoUrl != null) {
                            StreamingProviderIcon(
                                modifier = Modifier.padding(top = 16.dp),
                                logoUrl = streamingProvider.logoUrl,
                                contentDescription = streamingProvider.name ?: "Streaming Provider",
                            )
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                }
            }

            // Button Row with Play/Coming Soon and Watchlist buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = childPadding.start)
            ) {
                WatchEpisodeButton(
                    modifier = Modifier
                        .focusProperties {
                            canFocus = true
                            left = FocusRequester.Cancel
                            right = watchlistButtonFocusRequester
                        }
                        .onFocusChanged { focusState ->
                            try {
                                if (focusState.hasFocus) {
                                    onPlayButtonFocused?.invoke()
                                }
                            } catch (e: Exception) {
                                // Handle any focus-related exceptions gracefully
                            }
                        },
                    focusRequester = playButtonFocusRequester,
                    seasonNumber = targetSeason,
                    episodeNumber = targetEpisode,
                    isEnabled = hasTargetEpisodeVideo,
                    onClick = {
                        if (hasTargetEpisodeVideo) {
                            onEpisodeClick(targetEpisodeData)
                        }
                    }
                )

                // Watchlist button - only show if toggle function is available
                WatchlistButton(
                    isInWatchlist = isInWatchlist,
                    isLoading = watchlistLoading,
                    onClick = onToggleWatchlist,
                    focusRequester = watchlistButtonFocusRequester,
                    modifier = Modifier.focusProperties {
                        canFocus = true
                        left = playButtonFocusRequester
                        right = FocusRequester.Cancel
                    }
                )
            }
        }
    }
}

@Composable
private fun TvShowDescription(description: String) {
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
fun TvShowLargeTitle(
    modifier: Modifier = Modifier,
    tvShowTitle: String
) {
    Text(
        modifier = modifier,
        text = tvShowTitle,
        style = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        ),
        maxLines = 1
    )
}

@Composable
private fun TvShowDotSeparatedRow(
    texts: List<String?>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        texts.forEachIndexed { index, text ->
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                )
                if (index < texts.size - 1) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Finds the next episode the user should watch based on their progress.
 * Currently defaults to S1E1, but can be enhanced to track actual watch progress.
 *
 * @param seasons List of seasons with episodes
 * @return Triple of (seasonNumber, episodeNumber, episodeData)
 */
private fun findNextEpisodeToWatch(seasons: List<Season>?): Triple<Int, Int, Episode?> {
    if (seasons.isNullOrEmpty()) {
        return Triple(1, 1, null)
    }

    // TODO: In the future, implement actual watch progress tracking:
    // 1. Check user's watch history from database/preferences
    // 2. Find last watched episode
    // 3. Return next unwatched episode
    // 4. If all episodes watched, return first episode of next season

    // For now, find the first available episode starting from S1E1
    val sortedSeasons = seasons.sortedBy { it.number ?: 0 }.filter { it.number!! > 0 }

    for (season in sortedSeasons) {
        val seasonNumber = season.number ?: continue
        val sortedEpisodes = season.episodes?.sortedBy { it.tvShowSeasonPriority ?: 0 }

        if (!sortedEpisodes.isNullOrEmpty()) {
            val firstEpisode = sortedEpisodes.first()
            val episodeNumber = firstEpisode.tvShowSeasonPriority ?: 1
            return Triple(seasonNumber, episodeNumber, firstEpisode)
        }
    }

    // Fallback if no episodes found
    return Triple(1, 1, null)
}