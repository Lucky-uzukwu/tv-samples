// ABOUTME: Horizontal scrollable row of genre name buttons for TV channels navigation
// ABOUTME: Displays clickable text-based genre buttons with TV-focused styling and focus management

package com.google.wiltv.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.google.wiltv.data.models.Genre
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun GenreNamesRow(
    modifier: Modifier = Modifier,
    onClick: (genre: Genre, index: Int) -> Unit,
    genres: List<Genre>,
    aboveFocusRequester: FocusRequester,
    lazyRowState: TvLazyListState,
    focusRequesters: Map<Int, FocusRequester> = emptyMap(),
    downFocusRequester: FocusRequester? = null,
    onItemFocused: (Int) -> Unit = {},
    onAllClick: (() -> Unit)? = null
) {
    var hasFocus by remember { mutableStateOf(false) }

    Column {
        TvLazyRow(
            state = lazyRowState,
            pivotOffsets = PivotOffsets(0.1f, 0f),
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    hasFocus = focusState.hasFocus
                },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            // Calculate total count: +1 for "All" item if onAllClick is provided
            val totalItemCount = if (onAllClick != null) genres.size + 1 else genres.size

            items(
                count = totalItemCount,
                key = { index -> 
                    if (onAllClick != null && index == 0) "all" 
                    else if (onAllClick != null) genres[index - 1].id 
                    else genres[index].id
                }
            ) { index ->
                val focusRequester = focusRequesters[index]
                var isFocused by remember { mutableStateOf(false) }

                // Handle "All" item vs regular genre
                if (onAllClick != null && index == 0) {
                    // "All" item
                    Surface(
                        onClick = { onAllClick() },
                        modifier = Modifier
                            .then(
                                if (focusRequester != null) Modifier.focusRequester(focusRequester)
                                else Modifier
                            )
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                if (focusState.hasFocus) {
                                    onItemFocused(index)
                                }
                            }
                            .focusProperties {
                                up = aboveFocusRequester
                                downFocusRequester?.let { down = it }
                            },
                        shape = ClickableSurfaceDefaults.shape(shape = WilTvCardShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isFocused)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                            contentColor = Color.White,
                            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedContentColor = Color.White
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(
                                    width = WilTvBorderWidth,
                                    color = Color(0xFFA855F7)
                                ),
                                shape = WilTvCardShape
                            )
                        ),
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    // Regular genre item
                    val genreIndex = if (onAllClick != null) index - 1 else index
                    val genre = genres[genreIndex]

                    Surface(
                        onClick = { onClick(genre, genreIndex) },
                        modifier = Modifier
                            .then(
                                if (focusRequester != null) Modifier.focusRequester(focusRequester)
                                else Modifier
                            )
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                if (focusState.hasFocus) {
                                    onItemFocused(index)
                                }
                            }
                            .focusProperties {
                                up = aboveFocusRequester
                                downFocusRequester?.let { down = it }
                            },
                        shape = ClickableSurfaceDefaults.shape(shape = WilTvCardShape),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isFocused)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                            contentColor = Color.White,
                            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            focusedContentColor = Color.White
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(
                                    width = WilTvBorderWidth,
                                    color = Color(0xFFA855F7)
                                ),
                                shape = WilTvCardShape
                            )
                        ),
                    ) {
                        Text(
                            text = genre.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}