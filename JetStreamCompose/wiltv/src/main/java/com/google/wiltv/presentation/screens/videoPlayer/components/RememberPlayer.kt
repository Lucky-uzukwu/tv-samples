/*
 * Copyright 2025 Google LLC
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

package com.google.wiltv.presentation.screens.videoPlayer.components

import android.content.ContentValues.TAG
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberPlayer(context: Context): ExoPlayer {

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(10)
            .setSeekBackIncrementMs(10)
//            .setMediaSourceFactory(
//                ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
//            )
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }
    // 2. Handle the lifecycle of the ExoPlayer
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release() // Release the player when the composable is disposed
        }
    }
    return exoPlayer
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberPlayerWithProgressTracking(
    context: Context,
    contentId: Int?,
    contentType: String?,
    watchProgressManager: WatchProgressManager
): ExoPlayer {
    val scope = rememberCoroutineScope()
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setSeekForwardIncrementMs(10)
            .setSeekBackIncrementMs(10)
            .setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
            .build()
            .apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }
    
    // Start progress tracking when content info is available
    DisposableEffect(contentId, contentType) {
        if (contentId != null && contentType != null) {
            watchProgressManager.startTracking(exoPlayer, contentId, contentType, scope)
        }
        
        onDispose {
            // Stop tracking and save final progress
            if (contentId != null && contentType != null) {
                scope.launch {
                    watchProgressManager.stopTracking(exoPlayer)
                }
            }
        }
    }
    
    // Handle the lifecycle of the ExoPlayer
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release() // Release the player when the composable is disposed
        }
    }
    
    return exoPlayer
}
