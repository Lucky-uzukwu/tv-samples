package com.google.wiltv.presentation.screens.movies

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
import com.google.wiltv.data.models.Genre
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.Video
import com.google.wiltv.data.entities.WatchProgress
import com.google.wiltv.presentation.common.StreamingProviderIcon
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.utils.formatDuration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieDetails(
    id: Int,
    title: String?,
    releaseDate: String?,
    genres: List<Genre>?,
    duration: Int?,
    plot: String?,
    streamingProviders: List<StreamingProvider>?,
    video: Video?,
    openVideoPlayer: (movieId: String) -> Unit,
    playButtonFocusRequester: FocusRequester,
    watchlistButtonFocusRequester: FocusRequester,
    episodesTabFocusRequester: FocusRequester,
    onPlayButtonFocused: (() -> Unit)? = null,
    isInWatchlist: Boolean = false,
    watchlistLoading: Boolean = false,
    onToggleWatchlist: (() -> Unit),
    watchProgress: WatchProgress? = null
) {
    val childPadding = rememberChildPadding()


    // Request initial focus on Play button when screen loads
//    LaunchedEffect(Unit) {
//        playButtonFocusRequester.requestFocus()
//    }

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
                    MovieLargeTitle(
                        modifier = Modifier
                            .focusable(),
                        movieTitle = it
                    )
                }

                DotSeparatedRow(
                    modifier = Modifier.padding(top = 20.dp),
                    texts = listOf(
                        (releaseDate?.substring(0, 4) ?: "-"),
                        duration?.formatDuration() ?: "0h 0m",
                        genres?.take(3)?.joinToString(", ") { it.name },
                    )
                )
                plot?.let { MovieDescription(description = it) }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    streamingProviders?.take(5)?.forEach { streamingProvider ->
                        if (streamingProvider.logoUrl != null) {
                            StreamingProviderIcon(
                                modifier = Modifier.padding(top = 16.dp),
                                logoUrl = streamingProvider.logoUrl,
                                contentDescription = "Prime Video",
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
                if (video != null) {
                    val hasProgress = watchProgress != null && !watchProgress.completed && watchProgress.progressMs > 0
                    val progressPercentage = if (hasProgress && watchProgress!!.durationMs > 0) {
                        watchProgress.progressMs.toFloat() / watchProgress.durationMs.toFloat()
                    } else 0f
                    
                    ResumePlayButton(
                        modifier = Modifier
                            .focusProperties {
                                canFocus = true
                                left = FocusRequester.Cancel
//                                    right = watchlistButtonFocusRequester
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
                        onClick = {
                            openVideoPlayer(id.toString())
                        },
                        hasProgress = hasProgress,
                        progressPercentage = progressPercentage
                    )
                } else {
                    ComingSoonButton(
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
                        focusRequester = playButtonFocusRequester
                    )
                }

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
fun MovieLargeTitle(
    modifier: Modifier = Modifier,
    movieTitle: String
) {
    Text(
        modifier = modifier,
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