package com.google.jetstream.presentation.utils

import android.annotation.SuppressLint


@SuppressLint("DefaultLocale")
fun Int.formatDuration(): String {
    val hours = this / 60
    val remainingMinutes = this % 60
    return String.format("%02dh %02dm", hours, remainingMinutes)
}