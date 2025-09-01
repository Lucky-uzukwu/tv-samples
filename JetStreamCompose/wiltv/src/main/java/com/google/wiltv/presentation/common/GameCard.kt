// ABOUTME: Game card composable for displaying competition games
// ABOUTME: Shows cover image or custom team versus layout with live indicators

/*
 * Copyright 2023 Google LLC
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

package com.google.wiltv.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Surface
import coil.compose.AsyncImage
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape

@Composable
fun GameCard(
    game: CompetitionGame,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: androidx.tv.material3.ClickableSurfaceScale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
    glow: androidx.tv.material3.ClickableSurfaceGlow = ClickableSurfaceDefaults.glow(
        focusedGlow = Glow(
            elevationColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            elevation = 8.dp
        )
    ),
) {
    StandardCardContainer(
        modifier = modifier,
        title = { },
        imageCard = {
            Surface(
                onClick = onClick,
                shape = ClickableSurfaceDefaults.shape(WilTvCardShape),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = WilTvBorderWidth,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = WilTvCardShape
                    )
                ),
                scale = scale,
                glow = glow,
                content = {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TeamVersusImage(
                            game = game,
                            modifier = Modifier.fillMaxSize(),
                            showLiveBadge = false
                        )


                        if (game.isLive) {
                            LiveBadge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            )
        },
    )
}