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

package com.google.jetstream.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Surface
import com.google.jetstream.presentation.theme.JetStreamBorderWidth
import com.google.jetstream.presentation.theme.JetStreamCardShape

@Composable
fun MovieCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable BoxScope.() -> Unit,
) {
    StandardCardContainer(
        modifier = modifier,
        title = { },
        imageCard = {
            Surface(
                onClick = onClick,
                shape = ClickableSurfaceDefaults.shape(JetStreamCardShape),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(
                            width = JetStreamBorderWidth,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = JetStreamCardShape
                    )
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                content = image
            )
        },
    )
}
