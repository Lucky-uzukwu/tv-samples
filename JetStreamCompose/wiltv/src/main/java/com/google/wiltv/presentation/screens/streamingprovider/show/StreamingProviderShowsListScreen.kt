package com.google.wiltv.presentation.screens.streamingprovider.show

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.common.Error
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.common.MovieCard
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.theme.WilTvBottomListPadding
import com.google.wiltv.presentation.utils.focusOnInitialVisibility
import kotlinx.coroutines.flow.StateFlow


object StreamingProviderShowsListScreen {
    const val StreamingProviderIdBundleKey = "streamingProviderId"
}

@Composable
fun StreamingProviderShowsListScreen(
    onBackPressed: () -> Unit,
    onShowSelected: (TvShow) -> Unit,
    streamingProviderShowsListScreenViewModel: StreamingProviderShowsListScreenViewModel = hiltViewModel()
) {
    val uiState by streamingProviderShowsListScreenViewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        StreamingProviderShowsListScreenUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }

        StreamingProviderShowsListScreenUiState.Error -> {
            Error(modifier = Modifier.fillMaxSize())
        }

        is StreamingProviderShowsListScreenUiState.Done -> {
            val showsPagingData = s.shows
            ShowsGrid(
                streamingProviderName = s.streamingProviderName,
                tvShows = showsPagingData,
                onBackPressed = onBackPressed,
                onShowSelected = onShowSelected
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ShowsGrid(
    streamingProviderName: String,
    tvShows: StateFlow<PagingData<TvShow>>,
    onBackPressed: () -> Unit,
    onShowSelected: (TvShow) -> Unit,
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
            text = streamingProviderName,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(
                vertical = childPadding.top.times(3.5f)
            )
        )
        AnimatedContent(
            targetState = tvShows,
            label = "",
        ) { state ->
            val tvShows = state.collectAsLazyPagingItems().itemSnapshotList.items
            if (tvShows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No show available for this provider.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    contentPadding = PaddingValues(bottom = WilTvBottomListPadding)
                ) {
                    itemsIndexed(tvShows, key = { _, tvShow -> tvShow.id }) { index, item ->
                        MovieCard(
                            onClick = { onShowSelected(item) },
                            modifier = Modifier
                                .aspectRatio(1 / 1.5f)
                                .padding(8.dp)
                                .then(
                                    if (index == 0)
                                        Modifier.focusOnInitialVisibility(isFirstItemVisible)
                                    else Modifier
                                ),
                        ) {
                            val imageUrl = item.posterImageUrl
                            item.title?.let {
                                imageUrl?.let { posterUrl ->
                                    PosterImage(
                                        title = it,
                                        posterUrl = posterUrl,
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
}
