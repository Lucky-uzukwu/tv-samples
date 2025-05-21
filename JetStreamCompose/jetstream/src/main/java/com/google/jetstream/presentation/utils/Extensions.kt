package com.google.jetstream.presentation.utils

import android.annotation.SuppressLint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer


@SuppressLint("DefaultLocale")
fun Int.formatDuration(): String {
    val hours = this / 60
    val remainingMinutes = this % 60
    return String.format("%02dh %02dm", hours, remainingMinutes)
}

@SuppressLint("DefaultLocale")
fun String.formatVotes(): String {
    // Check if the string is a valid number
    val number = this.toLongOrNull() ?: return this

    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
        else -> "$number"
    }.replace(".0", "")  // Remove trailing .0 if it's a whole number
}

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }