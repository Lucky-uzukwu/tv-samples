// ABOUTME: Genre-specific TV channels list screen showing channels filtered by selected genre
// ABOUTME: Displays TV channels in a grid layout with genre name header and back navigation support

package com.google.wiltv.presentation.screens.genre.tvchannels

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.common.AuthenticatedAsyncImage
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.MovieCard
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
            modifier = Modifier.padding(
                vertical = childPadding.top.times(3.5f)
            )
        )
        AnimatedContent(
            targetState = channels,
            label = "",
        ) { state ->
            val channelList = state.collectAsLazyPagingItems().itemSnapshotList.items
            if (channelList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No channels available for this genre.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    contentPadding = PaddingValues(bottom = WilTvBottomListPadding)
                ) {
                    itemsIndexed(channelList, key = { _, channel -> channel.id }) { index, channel ->
                        MovieCard(
                            onClick = { onChannelSelected(channel) },
                            modifier = Modifier
                                .aspectRatio(16f / 9f)
                                .padding(8.dp)
                                .then(
                                    if (index == 0)
                                        Modifier.focusOnInitialVisibility(isFirstItemVisible)
                                    else Modifier
                                ),
                        ) {
                            val imageUrl = channel.logoUrl
                            imageUrl?.let {
                                AuthenticatedAsyncImage(
                                    model = it,
                                    contentDescription = channel.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}