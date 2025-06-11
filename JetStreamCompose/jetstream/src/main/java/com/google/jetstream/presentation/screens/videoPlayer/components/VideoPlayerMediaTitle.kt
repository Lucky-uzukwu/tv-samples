/*
 * Copyright 2024 Google LLC
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

package com.google.jetstream.presentation.screens.videoPlayer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.jetstream.presentation.theme.ComposeTvTheme

enum class VideoPlayerMediaTitleType { AD, LIVE, DEFAULT }

@Composable
fun VideoPlayerMediaTitle(
    title: String,
    secondaryText: String?,
    tertiaryText: String?,
    modifier: Modifier = Modifier,
) {
    val subTitle = buildString {
        append(secondaryText)
        if (!secondaryText.isNullOrEmpty() && tertiaryText.isNullOrEmpty()) append(" • ")
        append(tertiaryText)
    }
    Column(modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(Modifier.height(4.dp))

        Spacer(Modifier.width(8.dp))

        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier,
            color = Color.White
        )
    }
}

@Preview(name = "TV Series", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewSeries() {
    ComposeTvTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "True Detective",
                secondaryText = "S1E5",
                tertiaryText = "The Secret Fate Of All Life",
            )
        }
    }
}

@Preview(name = "Live", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewLive() {
    ComposeTvTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "MacLaren Reveal Their 2022 Car: The MCL36",
                secondaryText = "Formula 1",
                tertiaryText = "54K watching now",
            )
        }
    }
}

@Preview(name = "Ads", device = "id:tv_4k")
@Composable
private fun VideoPlayerMediaTitlePreviewAd() {
    ComposeTvTheme {
        Surface(shape = RectangleShape) {
            VideoPlayerMediaTitle(
                title = "Samsung Galaxy Note20 | Ultra 5G",
                secondaryText = "Get the most powerful Note yet",
                tertiaryText = "",
            )
        }
    }
}
