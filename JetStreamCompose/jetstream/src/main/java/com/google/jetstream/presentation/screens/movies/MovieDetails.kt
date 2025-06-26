package com.google.jetstream.presentation.screens.movies

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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.models.Country
import com.google.jetstream.data.models.Genre
import com.google.jetstream.presentation.common.DisplayFilmGenericText
import com.google.jetstream.presentation.common.IMDbLogo
import com.google.jetstream.presentation.common.StreamingProviderIcon
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.utils.formatDuration
import com.google.jetstream.presentation.utils.formatVotes
import com.google.jetstream.presentation.utils.getImdbRating
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.Video
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovieDetails(
    id: Int,
    title: String?,
    tagLine: String?,
    releaseDate: String?,
    countries: List<Country>?,
    genres: List<Genre>?,
    duration: Int?,
    plot: String?,
    imdbRating: String?,
    imdbVotes: Int?,
    streamingProviders: List<StreamingProvider>?,
    video: Video?,
    openVideoPlayer: (movieId: String) -> Unit,
) {
    val childPadding = rememberChildPadding()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val playButtonFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(432.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth(0.55f)
            .focusGroup()) {
            Spacer(modifier = Modifier.height(50.dp))
            Column(
                modifier = Modifier.padding(start = childPadding.start)
            ) {
                title?.let {
                    MovieLargeTitle(
                        modifier = Modifier.focusable(),
                        movieTitle = it
                    )
                }
                if (tagLine != null) {
                    MovieTagLine(movieTagline = tagLine)
                }

                Column(
                    modifier = Modifier.alpha(0.75f)
                ) {
                    DotSeparatedRow(
                        modifier = Modifier.padding(top = 20.dp),
                        texts = listOf(
                            (releaseDate?.substring(0, 4)
                                ?: "-") + " (${countries?.first()?.iso31661})",
                            genres?.joinToString(", ") { it.name },
                            duration?.formatDuration() ?: "0h 0m"
                        )
                    )
                    plot?.let { MovieDescription(description = it) }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row {
                        IMDbLogo()
                        Spacer(modifier = Modifier.width(8.dp))
//                        DisplayFilmGenericText(
//                            "${imdbRating.getImdbRating()}/10 - " +
//                                    "${imdbVotes.toString().formatVotes()} Votes"
//                        )
                    }


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        streamingProviders?.take(5)?.forEach { streamingProvider ->
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

                if (video != null) {
                    PlayButton(
                        modifier = Modifier
                            .padding(top = 16.dp),
                        focusRequester = playButtonFocusRequester,
                        onClick = {
                            coroutineScope.launch {
                                bringIntoViewRequester.bringIntoView()
                            }
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
private fun MovieLargeTitle(
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