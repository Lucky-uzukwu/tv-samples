package com.google.wiltv.presentation.screens.movies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.Country
import com.google.wiltv.data.models.Genre
import com.google.wiltv.presentation.common.StreamingProviderIcon
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.utils.formatDuration
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.Video

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
    episodesTabFocusRequester: FocusRequester,
    onPlayButtonFocused: (() -> Unit)? = null
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
                .fillMaxWidth(0.55f)
                .focusGroup()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = childPadding.start)
            ) {
                title?.let {
                    MovieLargeTitle(
                        modifier = Modifier.focusable(),
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

                if (video != null) {
                    PlayButton(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .focusProperties {
                                canFocus = true
                                down = episodesTabFocusRequester
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
                        }
                    )
                }
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