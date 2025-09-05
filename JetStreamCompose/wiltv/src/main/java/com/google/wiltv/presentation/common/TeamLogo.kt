// ABOUTME: Reusable team logo composable with fallback to team initials
// ABOUTME: Displays team logo image or generates initials placeholder when logo unavailable

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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage

@Composable
fun TeamLogo(
    logoUrl: String?,
    teamName: String,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    fontSize: TextUnit = 18.sp
) {
    if (!logoUrl.isNullOrBlank()) {
        AsyncImage(
            model = logoUrl,
            contentDescription = teamName,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        TeamInitialsPlaceholder(
            teamName = teamName,
            modifier = modifier,
            size = size,
            fontSize = fontSize
        )
    }
}

@Composable
fun TeamInitialsPlaceholder(
    teamName: String,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    fontSize: TextUnit = 18.sp
) {
    val initials = teamName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { teamName.take(2).uppercase() }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFA855F7)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.Black,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}