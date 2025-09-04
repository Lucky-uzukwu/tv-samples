package com.google.wiltv.presentation.screens.videoPlayer

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import co.touchlab.kermit.Logger
import androidx.lifecycle.ViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.google.wiltv.presentation.screens.videoPlayer.components.WatchProgressManager
import javax.inject.Inject
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerOverlay
import com.google.wiltv.presentation.screens.videoPlayer.components.VideoPlayerPulse
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberPlayer
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberVideoPlayerPulseState
import com.google.wiltv.presentation.screens.videoPlayer.components.rememberVideoPlayerState
import com.google.wiltv.presentation.utils.KeepScreenOn

@OptIn(UnstableApi::class)
@HiltViewModel
class TvShowEpisodeProgressViewModel @Inject constructor(
    val watchProgressManager: WatchProgressManager
) : ViewModel()

@Composable
fun TvShowEpisodeVideoPlayerScreenContent(
    directUrl: String,
    title: String?,
    token: String?,
    episodeId: Int?,
    onBackPressed: () -> Unit,
    progressViewModel: TvShowEpisodeProgressViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val exoPlayer = rememberPlayer(context)
    val coroutineScope = rememberCoroutineScope()

    val videoPlayerState = rememberVideoPlayerState(
        hideSeconds = 15,
    )

    LaunchedEffect(directUrl, token) {
        Logger.i("TvShowEpisodePlayer - Playing Episode with URL: $directUrl")
        Logger.i("TvShowEpisodePlayer - Token available: ${token != null}")
        token?.let { Logger.i("TvShowEpisodePlayer - Token first 10 chars: ${it.take(10)}...") }
        
        try {
            // Create HTTP data source factory - URLs are pre-authenticated with query params
            val dataSourceFactory = DefaultHttpDataSource.Factory().apply {
                setAllowCrossProtocolRedirects(true)
                setConnectTimeoutMs(30000)
                setReadTimeoutMs(30000)
                
                // Set basic headers but no Authorization - URLs contain auth via query params
                val headers = mapOf(
                    "Accept" to "*/*",
                    "User-Agent" to "WilTV Android App"
                )
                setDefaultRequestProperties(headers)
                Logger.i("TvShowEpisodePlayer - Using pre-signed URL authentication (no Bearer headers)")
                Logger.i("TvShowEpisodePlayer - URL contains auth params: ${directUrl.contains("token=")}")
            }

            // Create HLS media source for episode streaming
            val mediaSource = if (directUrl.contains(".m3u8")) {
                Logger.i("TvShowEpisodePlayer - Creating HLS media source")
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(MediaItem.fromUri(directUrl))
            } else {
                Logger.i("TvShowEpisodePlayer - Creating Progressive media source")
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(directUrl))
            }

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.play()
            Logger.i("TvShowEpisodePlayer - Started playback")
            
            // Start progress tracking if episodeId is available
            episodeId?.let { epId ->
                Logger.i("TvShowEpisodePlayer - Starting progress tracking for episode ID: $epId")
                progressViewModel.watchProgressManager.startTracking(
                    player = exoPlayer,
                    contentId = epId,
                    contentType = "tvshow",
                    scope = coroutineScope
                )
            } ?: run {
                Logger.w("TvShowEpisodePlayer - No episode ID available, skipping progress tracking")
            }
        } catch (e: Exception) {
            Logger.e("TvShowEpisodePlayer - Error setting up authenticated playback", e)
        }
    }

    // Stop progress tracking when composable is disposed
    DisposableEffect(episodeId) {
        onDispose {
            if (episodeId != null) {
                Logger.i("TvShowEpisodePlayer - Stopping progress tracking for episode ID: $episodeId")
                progressViewModel.watchProgressManager.stopTracking(exoPlayer)
            }
        }
    }

    // Handle playback errors
    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Logger.e("TvShowEpisodePlayer - Playback error for URL: $directUrl", error)
                Logger.e("TvShowEpisodePlayer - Error message: ${error.message}")
                Logger.e("TvShowEpisodePlayer - URL has token param: ${directUrl.contains("token=")}")
                
                when (error.errorCode) {
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                        Logger.e("TvShowEpisodePlayer - HTTP error (403/401/404) - Pre-signed URL authentication issue")
                        Logger.e("TvShowEpisodePlayer - URL might be expired or invalid")
                        // Try to extract HTTP status code from the error
                        error.cause?.let { cause ->
                            Logger.e("TvShowEpisodePlayer - Root cause: ${cause.message}")
                        }
                    }
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                        Logger.e("TvShowEpisodePlayer - Network connection failed for episode URL: $directUrl")
                    }
                    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> {
                        Logger.e("TvShowEpisodePlayer - Parsing error - possible authentication response instead of media")
                    }
                    else -> {
                        Logger.e("TvShowEpisodePlayer - Other playback error: ${error.errorCode} for episode URL: $directUrl")
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
    }

    BackHandler(onBack = onBackPressed)
    
    // Keep screen on during playback
    KeepScreenOn(player = exoPlayer)

    val pulseState = rememberVideoPlayerPulseState()

    Box(
        Modifier
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
            modifier = Modifier.resizeWithContentScale(
                contentScale = ContentScale.Fit,
                sourceSizeDp = null
            )
        )

        val focusRequester = remember { FocusRequester() }
        VideoPlayerOverlay(
            modifier = Modifier.align(Alignment.BottomCenter),
            focusRequester = focusRequester,
            isPlaying = exoPlayer.isPlaying,
            isControlsVisible = videoPlayerState.isControlsVisible,
            centerButton = { VideoPlayerPulse(pulseState) },
            subtitles = { /* Subtitles could be added here in the future */ },
            showControls = videoPlayerState::showControls,
            controls = {
                TvShowEpisodeVideoPlayerControls(
                    player = exoPlayer,
                    directUrl = directUrl,
                    title = title ?: "Episode",
                    focusRequester = focusRequester,
                    onShowControls = { videoPlayerState.showControls(exoPlayer.isPlaying) },
                )
            }
        )
    }
}

@Composable
private fun TvShowEpisodeVideoPlayerControls(
    player: ExoPlayer,
    directUrl: String,
    title: String,
    focusRequester: FocusRequester,
    onShowControls: () -> Unit
) {
    // For now, reuse the TV channel controls
    // In the future, this could have episode-specific controls
    TvChannelVideoPlayerControls(
        player = player,
        directUrl = directUrl,
        title = title,
        focusRequester = focusRequester,
        onShowControls = onShowControls
    )
}