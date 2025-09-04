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
import android.util.Log
import com.google.wiltv.data.entities.WatchProgress
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
    onToggleWatchlist: (() -> Unit),
    episodeWatchProgress: Map<Int, WatchProgress> = emptyMap()
) {
    val childPadding = rememberChildPadding()
    val seasons = seasonsToEpisodes.map { it.first }

    // Find the next episode to watch based on user's progress
    val (targetSeason, targetEpisode, targetEpisodeData) = findNextEpisodeToWatch(
        seasonsToEpisodes,
        episodeWatchProgress
    )

    val hasTargetEpisodeVideo = targetEpisodeData?.video != null

    // Calculate watch progress for the target episode
    val targetEpisodeProgress = targetEpisodeData?.let { episode ->
        episodeWatchProgress[episode.id]
    }
    val hasProgress =
        targetEpisodeProgress != null && !targetEpisodeProgress.completed && targetEpisodeProgress.progressMs > 0
    val progressPercentage = if (hasProgress && targetEpisodeProgress!!.durationMs > 0) {
        targetEpisodeProgress.progressMs.toFloat() / targetEpisodeProgress.durationMs.toFloat()
    } else 0f

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
                    hasProgress = hasProgress,
                    progressPercentage = progressPercentage,
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
 * Uses watch progress data to determine where the user should resume watching.
 *
 * @param seasonsToEpisodes List of seasons with episodes
 * @param watchProgress Map of episode IDs to their watch progress
 * @return Triple of (seasonNumber, episodeNumber, episodeData)
 */
private fun findNextEpisodeToWatch(
    seasonsToEpisodes: SeasonsToEpisodes, watchProgress: Map<Int, WatchProgress>
): Triple<Int, Int, Episode?> {
    Log.d(
        "TvShowDetails",
        "findNextEpisodeToWatch: Starting with ${seasonsToEpisodes.size} seasons"
    )
    Log.d(
        "TvShowDetails",
        "findNextEpisodeToWatch: Watch progress has ${watchProgress.size} entries"
    )

    val seasons = seasonsToEpisodes.map { it.first }
    if (seasons.isEmpty()) {
        Log.d("TvShowDetails", "findNextEpisodeToWatch: No seasons found, returning default")
        return Triple(1, 1, null)
    }

    // Get all episodes across all seasons, sorted by season then episode number
    val allEpisodes = mutableListOf<Pair<Episode, Pair<Int, Int>>>()

    // Use the explicit episodes from seasonsToEpisodes instead of season.episodes
    for ((season, episodesList) in seasonsToEpisodes) {
        val seasonNumber = season.number ?: continue
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: Processing season $seasonNumber with ${episodesList.size} episodes"
        )

        // Sort by tvShowSeasonPriority (episode number) first, fallback to id
        val sortedEpisodes = episodesList.sortedBy { episode ->
            episode.title.toInt()
        }

        for ((index, episode) in sortedEpisodes.withIndex()) {
            // Use tvShowSeasonPriority as episode number, fallback to 1-based index
            val episodeNumber = episode.title.toInt()
            Log.d(
                "TvShowDetails",
                "findNextEpisodeToWatch: Episode ${episode.title} - S${seasonNumber}E${episodeNumber} (ID: ${episode.id})"
            )
            allEpisodes.add(Pair(episode, Pair(seasonNumber, episodeNumber)))
        }
    }

    if (allEpisodes.isEmpty()) {
        Log.d("TvShowDetails", "findNextEpisodeToWatch: No episodes found, returning default")
        return Triple(1, 1, null)
    }

    Log.d("TvShowDetails", "findNextEpisodeToWatch: Total ${allEpisodes.size} episodes collected")

    // If no watch progress, return first episode with video
    if (watchProgress.isEmpty()) {
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: No watch progress, finding first playable episode"
        )
        val firstPlayableEpisode = allEpisodes.firstOrNull { (episode, _) -> episode.video != null }
        if (firstPlayableEpisode != null) {
            val (episode, seasonEpisodePair) = firstPlayableEpisode
            Log.d(
                "TvShowDetails",
                "findNextEpisodeToWatch: Selected first playable episode: ${episode.title} - S${seasonEpisodePair.first}E${seasonEpisodePair.second}"
            )
            return Triple(seasonEpisodePair.first, seasonEpisodePair.second, episode)
        } else {
            Log.d(
                "TvShowDetails",
                "findNextEpisodeToWatch: No playable episodes found, returning first anyway"
            )
            val (firstEpisode, seasonEpisodePair) = allEpisodes.first()
            return Triple(seasonEpisodePair.first, seasonEpisodePair.second, firstEpisode)
        }
    }

    // Find episodes with watch progress, sorted by last watched time
    Log.d("TvShowDetails", "findNextEpisodeToWatch: Checking for watched episodes...")
    val watchedEpisodes = allEpisodes.filter { (episode, _) ->
        val hasProgress = watchProgress.containsKey(episode.id)
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: Episode ${episode.title} (ID: ${episode.id}) has progress: $hasProgress"
        )
        hasProgress
    }.sortedBy { (episode, _) ->
        watchProgress[episode.id]?.lastWatched ?: 0
    }

    Log.d("TvShowDetails", "findNextEpisodeToWatch: Found ${watchedEpisodes.size} watched episodes")

    if (watchedEpisodes.isEmpty()) {
        // No episodes watched yet, return first episode with video
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: No watched episodes, finding first playable episode"
        )
        val firstPlayableEpisode = allEpisodes.firstOrNull { (episode, _) -> episode.video != null }
        if (firstPlayableEpisode != null) {
            val (episode, seasonEpisodePair) = firstPlayableEpisode
            Log.d(
                "TvShowDetails",
                "findNextEpisodeToWatch: Selected first playable episode: ${episode.title} - S${seasonEpisodePair.first}E${seasonEpisodePair.second}"
            )
            return Triple(seasonEpisodePair.first, seasonEpisodePair.second, episode)
        } else {
            Log.d(
                "TvShowDetails",
                "findNextEpisodeToWatch: No playable episodes found, returning first anyway"
            )
            val (firstEpisode, seasonEpisodePair) = allEpisodes.first()
            return Triple(seasonEpisodePair.first, seasonEpisodePair.second, firstEpisode)
        }
    }

    // Find last watched episode
    val (lastWatchedEpisode, lastWatchedPair) = watchedEpisodes.last()
    val lastProgress = watchProgress[lastWatchedEpisode.id]!!

    Log.d(
        "TvShowDetails",
        "findNextEpisodeToWatch: Last watched episode: ${lastWatchedEpisode.title} - S${lastWatchedPair.first}E${lastWatchedPair.second}"
    )
    Log.d(
        "TvShowDetails",
        "findNextEpisodeToWatch: Last progress - completed: ${lastProgress.completed}, progressMs: ${lastProgress.progressMs}"
    )

    // If last watched episode is not completed, resume it
    if (!lastProgress.completed && lastProgress.progressMs > 0) {
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: Resuming incomplete episode: ${lastWatchedEpisode.title}"
        )
        return Triple(lastWatchedPair.first, lastWatchedPair.second, lastWatchedEpisode)
    }

    // Last episode was completed, find next episode
    val lastWatchedIndex = allEpisodes.indexOfFirst { it.first.id == lastWatchedEpisode.id }
    Log.d(
        "TvShowDetails",
        "findNextEpisodeToWatch: Last watched episode index: $lastWatchedIndex out of ${allEpisodes.size}"
    )

    if (lastWatchedIndex != -1 && lastWatchedIndex + 1 < allEpisodes.size) {
        val (nextEpisode, nextPair) = allEpisodes[lastWatchedIndex + 1]
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: Next episode: ${nextEpisode.title} - S${nextPair.first}E${nextPair.second}"
        )
        return Triple(nextPair.first, nextPair.second, nextEpisode)
    }

    // All episodes watched, return first episode with video
    Log.d(
        "TvShowDetails",
        "findNextEpisodeToWatch: All episodes watched, finding first playable episode"
    )
    val firstPlayableEpisode = allEpisodes.firstOrNull { (episode, _) -> episode.video != null }
    if (firstPlayableEpisode != null) {
        val (episode, firstPair) = firstPlayableEpisode
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: Selected first playable episode: ${episode.title} - S${firstPair.first}E${firstPair.second}"
        )
        return Triple(firstPair.first, firstPair.second, episode)
    } else {
        Log.d(
            "TvShowDetails",
            "findNextEpisodeToWatch: No playable episodes found, returning first episode"
        )
        val (firstEpisode, firstPair) = allEpisodes.first()
        return Triple(firstPair.first, firstPair.second, firstEpisode)
    }
}