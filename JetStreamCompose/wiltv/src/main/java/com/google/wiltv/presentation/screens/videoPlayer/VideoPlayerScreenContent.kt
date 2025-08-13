package com.google.wiltv.presentation.screens.videoPlayer

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.paging.PagingData
import co.touchlab.kermit.Logger
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerControls
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerPulse
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberPlayer
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberVideoPlayerPulseState
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberVideoPlayerState
import kotlinx.coroutines.flow.StateFlow

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreenContent(
    selectedMovie: MovieNew,
    similarMovies: StateFlow<PagingData<MovieNew>>,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = rememberPlayer(context)

    val videoPlayerState = rememberVideoPlayerState(
        hideSeconds = 15,
    )

    LaunchedEffect(selectedMovie) {
        Logger.i("Selected Movie: ${selectedMovie.title}, with video: ${selectedMovie.video?.hlsPlaylistUrl}")
        exoPlayer.addMediaItem(MediaItem.fromUri(selectedMovie.video?.hlsPlaylistUrl.toString()))
        exoPlayer.setMediaItem(MediaItem.fromUri(selectedMovie.video?.hlsPlaylistUrl.toString()))
        exoPlayer.prepare()
    }

    BackHandler(onBack = onBackPressed)

    val pulseState = rememberVideoPlayerPulseState()

    Box(
        Modifier.Companion
            .dPadEvents(
                exoPlayer,
                videoPlayerState,
                pulseState
            )
            .focusable()
    ) {
        PlayerSurface(
            player = exoPlayer,
            surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
            modifier = Modifier.Companion.resizeWithContentScale(
                contentScale = ContentScale.Companion.Fit,
                sourceSizeDp = null
            )
        )

        val focusRequester = remember { FocusRequester() }
        VideoPlayerOverlay(
            modifier = Modifier.Companion.align(Alignment.Companion.BottomCenter),
            focusRequester = focusRequester,
            isPlaying = exoPlayer.isPlaying,
            isControlsVisible = videoPlayerState.isControlsVisible,
            centerButton = { VideoPlayerPulse(pulseState) },
            subtitles = { /* TODO Implement subtitles */ },
            showControls = videoPlayerState::showControls,
            controls = {
                VideoPlayerControls(
                    player = exoPlayer,
                    movie = selectedMovie,
                    focusRequester = focusRequester,
                    onShowControls = { videoPlayerState.showControls(exoPlayer.isPlaying) },
                )
            }
        )
    }
}