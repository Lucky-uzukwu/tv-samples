// ABOUTME: Utility to prevent Android TV screensaver during video playback
// ABOUTME: Manages FLAG_KEEP_SCREEN_ON window flag based on ExoPlayer state

package com.google.wiltv.presentation.utils

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun KeepScreenOn(player: ExoPlayer) {
    val context = LocalContext.current
    
    DisposableEffect(player, player.isPlaying) {
        val activity = context as? Activity
        
        if (activity != null && player.isPlaying) {
            // Keep screen on while playing
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        onDispose {
            // Clear the flag when leaving or pausing
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}