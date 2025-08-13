// ABOUTME: Genre-specific TV channels list screen showing channels filtered by selected genre
// ABOUTME: Displays TV channels in a grid layout with genre name header and back navigation support

package com.google.wiltv.presentation.screens.genre.tvchannels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.common.AuthenticatedAsyncImage
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.TvChannelCard
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBottomListPadding
import com.google.wiltv.presentation.utils.focusOnInitialVisibility
import kotlinx.coroutines.flow.StateFlow

object GenreTvChannelsListScreen {
    const val GenreIdBundleKey = "genreId"
}

@Composable
fun GenreTvChannelsListScreen(
    onBackPressed: () -> Unit,
    onChannelSelected: (TvChannel) -> Unit,
    genreTvChannelsListScreenViewModel: GenreTvChannelsListScreenViewModel = hiltViewModel()
) {
    val uiState by genreTvChannelsListScreenViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        GenreTvChannelsListScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        GenreTvChannelsListScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is GenreTvChannelsListScreenUiState.Done -> {
            val channelsPagingData = s.channels
            ChannelsGrid(
                genreName = s.genreName,
                channels = channelsPagingData,
                onBackPressed = onBackPressed,
                onChannelSelected = onChannelSelected
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChannelsGrid(
    genreName: String,
    channels: StateFlow<PagingData<TvChannel>>,
    onBackPressed: () -> Unit,
    onChannelSelected: (TvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    val childPadding = rememberChildPadding()
    val isFirstItemVisible = remember { mutableStateOf(false) }
    
    // Collect paging items at the top level - this fixes the flashing empty state
    val channelsPagingItems = channels.collectAsLazyPagingItems()

    BackHandler(onBack = onBackPressed)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Text(
            text = genreName,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White,
            modifier = Modifier.padding(
                vertical = childPadding.top.times(2f)
            )
        )
        
        // Handle different loading states properly
        when (channelsPagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Loading(modifier = Modifier.fillMaxSize())
                }
            }
            
            is LoadState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Error(modifier = Modifier.fillMaxSize())
                }
            }
            
            is LoadState.NotLoading -> {
                if (channelsPagingItems.itemCount == 0) {
                    // Only show empty state when loading is complete and no items exist
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "No channels available for this genre",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Try selecting a different genre or check back later.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Show the channels grid with improved layout for TV
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5), // Reduced from 6 to 5 for better spacing
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = WilTvBottomListPadding
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            count = channelsPagingItems.itemCount,
                            key = { index -> channelsPagingItems[index]?.id ?: "loading_$index" }
                        ) { index ->
                            val channel = channelsPagingItems[index]
                            if (channel != null) {
                                TvChannelCard(
                                    onClick = { onChannelSelected(channel) },
                                    modifier = Modifier
                                        .aspectRatio(1f) // Square aspect ratio for TV channel logos
                                        .padding(6.dp) // Extra padding for scaled border visibility
                                        .then(
                                            if (index == 0)
                                                Modifier.focusOnInitialVisibility(isFirstItemVisible)
                                            else Modifier
                                        ),
                                ) {
                                    val imageUrl = channel.logoUrl
                                    // Using runtime null safety even though model shows non-null
                                    // to prevent crashes from malformed network responses
                                    if (!imageUrl.isNullOrEmpty()) {
                                        AuthenticatedAsyncImage(
                                            model = imageUrl,
                                            contentDescription = channel.name,
                                            modifier = Modifier.fillMaxSize()
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
                                                color = Color.White.copy(alpha = 0.95f),
                                                textAlign = TextAlign.Center,
                                                maxLines = 3
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}