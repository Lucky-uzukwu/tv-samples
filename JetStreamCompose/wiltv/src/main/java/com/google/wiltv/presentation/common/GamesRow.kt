// ABOUTME: Row component for displaying competition games in horizontal list
// ABOUTME: Shows games for a specific sport type with title and custom game cards

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

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding

@Composable
fun GamesRow(
    games: LazyPagingItems<CompetitionGame>,
    modifier: Modifier = Modifier,
    startPadding: Dp = rememberChildPadding().start,
    endPadding: Dp = rememberChildPadding().end,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp
    ),
    onGameSelected: (game: CompetitionGame) -> Unit = {},
    lazyRowState: TvLazyListState? = null,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    onItemFocused: (game: CompetitionGame, index: Int) -> Unit = { _, _ -> },
) {

    Column(
        modifier = modifier.focusGroup()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .alpha(1f)
                    .padding(start = startPadding, top = 16.dp, bottom = 16.dp)
            )
        }
        TvLazyRow(
            state = lazyRowState ?: rememberTvLazyListState(),
            contentPadding = PaddingValues(
                start = startPadding,
                end = endPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                count = games.itemCount,
                key = { index -> games.peek(index)?.id ?: "game_$index" },
                contentType = { "GameItem" }
            ) { index ->
                val game = games[index]
                if (game != null) {
                    val focusRequester = focusRequesters[index]
                    GameCard(
                        game = game,
                        onClick = { 
                            onGameSelected(game) 
                            onItemFocused(game, index)
                        },
                        modifier = Modifier
                            .width(168.dp)
                            .height(100.dp)
                            .then(
                                if (focusRequester != null) Modifier.focusRequester(focusRequester)
                                else Modifier
                            )
                    )
                }
            }
        }
    }
}