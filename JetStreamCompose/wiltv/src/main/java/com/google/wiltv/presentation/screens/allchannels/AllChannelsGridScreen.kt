// ABOUTME: Full screen grid display for all TV channels without genre filtering
// ABOUTME: Provides comprehensive channel browsing with proper LoadState handling and focus management

package com.google.wiltv.presentation.screens.allchannels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.common.AuthenticatedAsyncImage
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.TvChannelCard
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.utils.createInitialFocusRestorerModifiers
import com.google.wiltv.presentation.utils.focusOnInitialVisibility

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AllChannelsGridScreen(
    allChannels: LazyPagingItems<TvChannel>,
    onChannelClick: (TvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val focusRestorerModifiers = createInitialFocusRestorerModifiers()
    val isFirstItemVisible = remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Title
        Text(
            text = "All TV Channels",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            modifier = Modifier.padding(
                start = 48.dp,
                top = 32.dp,
                bottom = 24.dp
            )
        )

        // Handle different load states
        when (allChannels.loadState.refresh) {
            is LoadState.Loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }
            is LoadState.Error -> {
                val error = allChannels.loadState.refresh as LoadState.Error
                ErrorScreen(
                    uiText = UiText.DynamicString(error.error.message ?: "Unknown error"),
                    onRetry = { allChannels.retry() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                if (allChannels.itemCount == 0) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No channels available",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Grid content
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        state = gridState,
                        contentPadding = PaddingValues(
                            start = 48.dp,
                            end = 48.dp,
                            bottom = 48.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .then(focusRestorerModifiers.parentModifier)
                    ) {
                        items(
                            count = allChannels.itemCount,
                            key = { index -> "all_channel_${allChannels[index]?.id ?: "index_$index"}" }
                        ) { index ->
                            val channel = allChannels[index]
                            if (channel != null) {
                                TvChannelCard(
                                    onClick = { onChannelClick(channel) },
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .then(
                                            if (index == 0)
                                                focusRestorerModifiers.childModifier.focusOnInitialVisibility(isFirstItemVisible)
                                            else Modifier
                                        )
                                ) {
                                    val imageUrl = channel.logoUrl
                                    // Display image if available, otherwise fallback to text
                                    if (!imageUrl.isNullOrEmpty()) {
                                        AuthenticatedAsyncImage(
                                            model = imageUrl,
                                            contentDescription = channel.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        // Fallback for channels without logos
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = channel.name.takeIf { it.isNotBlank() } ?: "Unknown Channel",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                textAlign = TextAlign.Center,
                                                maxLines = 3
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Loading indicator for pagination
                        when (allChannels.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Loading(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }
                            }
                            is LoadState.Error -> {
                                item {
                                    ErrorScreen(
                                        uiText = UiText.DynamicString("Failed to load more channels"),
                                        onRetry = { allChannels.retry() },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}