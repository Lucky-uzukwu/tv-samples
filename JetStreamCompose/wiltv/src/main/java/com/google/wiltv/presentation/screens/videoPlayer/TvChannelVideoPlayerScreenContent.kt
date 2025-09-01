package com.google.wiltv.presentation.screens.videoPlayer

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import co.touchlab.kermit.Logger
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.screens.videoPlayer.components.NextButton
import com.google.wiltv.presentation.screens.videoPlayer.components.PreviousButton
import com.google.wiltv.presentation.screens.videoPlayer.components.RepeatButton
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerControlsIcon
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerMainFrame
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerMediaTitle
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerPulse
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerSeeker
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberPlayer
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberVideoPlayerPulseState
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberVideoPlayerState

@OptIn(UnstableApi::class)
@Composable
fun TvChannelVideoPlayerScreenContent(
    directUrl: String,
    title: String?,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = rememberPlayer(context)
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val videoPlayerState = rememberVideoPlayerState(
        hideSeconds = 15,
    )
    
    LaunchedEffect(directUrl) {
        Logger.i("Playing TV Channel with URL: $directUrl")
        val mediaItem = MediaItem.fromUri(directUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // Handle playback errors by showing error screen
    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Logger.e("Playback error for URL: ${exoPlayer.currentMediaItem?.mediaId ?: directUrl}", error)
                val errorText = when (error.errorCode) {
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                        "This TV channel is currently unavailable. Please try again later."
                    }
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                        "Network connection failed. Please check your internet connection and try again."
                    }
                    else -> {
                        "Unable to play this TV channel. Please try again later."
                    }
                }
                hasError = true
                errorMessage = errorText
            }
        }
        exoPlayer.addListener(listener)
    }

    BackHandler(onBack = onBackPressed)

    if (hasError) {
        ErrorScreen(
            uiText = UiText.DynamicString(errorMessage),
            onGoBack = onBackPressed,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

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
            subtitles = { /* No subtitles for TV channels */ },
            showControls = videoPlayerState::showControls,
            controls = {
                TvChannelVideoPlayerControls(
                    player = exoPlayer,
                    directUrl = directUrl,
                    title = title,
                    focusRequester = focusRequester,
                    onShowControls = { videoPlayerState.showControls(exoPlayer.isPlaying) },
                )
            }
        )
    }
}

@Composable
fun TvChannelVideoPlayerControls(
    player: Player,
    directUrl: String,
    title: String?,
    focusRequester: FocusRequester,
    onShowControls: () -> Unit = {},
) {

    val isPlaying = player.isPlaying

    VideoPlayerMainFrame(
        mediaTitle = {
            VideoPlayerMediaTitle(
                title = title ?: "TV Channel",
                secondaryText = "Live Stream",
                tertiaryText = null,
            )
        },
        mediaActions = {
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreviousButton(
                    player = player,
                    onShowControls = onShowControls
                )
                NextButton(
                    player = player,
                    onShowControls = onShowControls
                )
                RepeatButton(
                    player = player,
                    onShowControls = onShowControls,
                )
                VideoPlayerControlsIcon(
                    icon = Icons.Default.AutoAwesomeMotion,
                    isPlaying = isPlaying,
                    contentDescription =
                        StringConstants.Composable.VideoPlayerControlPlaylistButton,
                    onShowControls = onShowControls
                )
                VideoPlayerControlsIcon(
                    icon = Icons.Default.ClosedCaption,
                    isPlaying = isPlaying,
                    contentDescription =
                        StringConstants.Composable.VideoPlayerControlClosedCaptionsButton,
                    onShowControls = onShowControls
                )
                VideoPlayerControlsIcon(
                    icon = Icons.Default.Settings,
                    isPlaying = isPlaying,
                    contentDescription =
                        StringConstants.Composable.VideoPlayerControlSettingsButton,
                    onShowControls = onShowControls
                )
            }
        },
        seeker = {
            VideoPlayerSeeker(
                player = player,
                focusRequester = focusRequester,
                onShowControls = onShowControls,
            )
        },
        more = null
    )
}