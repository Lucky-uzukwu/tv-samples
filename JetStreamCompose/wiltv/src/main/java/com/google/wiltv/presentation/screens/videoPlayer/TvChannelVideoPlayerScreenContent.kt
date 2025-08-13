package com.google.wiltv.presentation.screens.videoPlayer

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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

    val videoPlayerState = rememberVideoPlayerState(
        hideSeconds = 15,
    )

    // Fallback URLs for testing when the primary URL fails
    val fallbackUrls = listOf(
        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
        "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
    )
    
    LaunchedEffect(directUrl) {
        Logger.i("Playing TV Channel with URL: $directUrl")
        val mediaItem = MediaItem.fromUri(directUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // Handle playback errors with fallback
    LaunchedEffect(exoPlayer) {
        var fallbackIndex = 0
        
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Logger.e("Playback error for URL: ${exoPlayer.currentMediaItem?.mediaId ?: directUrl}", error)
                when (error.errorCode) {
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                        Logger.e("HTTP error (likely 404) for URL: ${exoPlayer.currentMediaItem?.mediaId ?: directUrl}")
                        
                        // Try fallback URLs
                        if (fallbackIndex < fallbackUrls.size) {
                            val fallbackUrl = fallbackUrls[fallbackIndex]
                            Logger.i("Trying fallback URL $fallbackIndex: $fallbackUrl")
                            fallbackIndex++
                            val fallbackMediaItem = MediaItem.fromUri(fallbackUrl)
                            exoPlayer.setMediaItem(fallbackMediaItem)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        } else {
                            Logger.e("All fallback URLs exhausted")
                        }
                    }
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                        Logger.e("Network connection failed for URL: ${exoPlayer.currentMediaItem?.mediaId ?: directUrl}")
                    }
                    else -> {
                        Logger.e("Other playback error: ${error.errorCode} for URL: ${exoPlayer.currentMediaItem?.mediaId ?: directUrl}")
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
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