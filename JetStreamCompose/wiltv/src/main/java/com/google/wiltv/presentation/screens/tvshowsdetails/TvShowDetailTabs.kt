package com.google.wiltv.presentation.screens.tvshowsdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import com.google.wiltv.R
import com.google.wiltv.data.models.Season
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.common.PosterImage
import com.google.wiltv.presentation.common.TvShowsRow
import com.google.wiltv.presentation.screens.dashboard.rememberChildPadding
import com.google.wiltv.presentation.screens.movies.TitleValueText
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TvShowDetailTabs(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    selectedTvShow: TvShow,
    similarTvShows: StateFlow<PagingData<TvShow>>,
    refreshScreenWithNewTvShow: (TvShow) -> Unit,
    episodesTabFocusRequester: FocusRequester,
    suggestedTabFocusRequester: FocusRequester,
    detailsTabFocusRequester: FocusRequester,
    playButtonFocusRequester: FocusRequester,
    onTabsFocusChanged: (Boolean) -> Unit
) {
    val tabs = listOf("Episodes", "Suggested", "Details")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val focusRequesters = listOf(episodesTabFocusRequester, suggestedTabFocusRequester, detailsTabFocusRequester)

    Column(modifier = modifier) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = Color.White,
            containerColor = Color.Transparent
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier
                        .focusRequester(focusRequesters[index])
                        .focusProperties {
                            up = playButtonFocusRequester
                            left = if (index > 0) focusRequesters[index - 1] else FocusRequester.Default
                            right = if (index < focusRequesters.size - 1) focusRequesters[index + 1] else FocusRequester.Default
                        }
                        .onFocusChanged { focusState ->
                            try {
                                if (focusState.hasFocus) {
                                    selectedTabIndex = index
                                    onTabsFocusChanged(true)
                                }
                            } catch (e: Exception) {
                                // Handle any focus-related exceptions gracefully
                            }
                        }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> EpisodesTab(
                seasons = selectedTvShow.seasons,
                isFullScreen = isFullScreen
            )
            1 -> SuggestedTab(
                similarTvShows = similarTvShows,
                selectedTvShow = selectedTvShow,
                refreshScreenWithNewTvShow = refreshScreenWithNewTvShow,
                isFullScreen = isFullScreen
            )
            2 -> DetailsTab(
                tvShow = selectedTvShow,
                isFullScreen = isFullScreen
            )
        }
    }
}

@Composable
fun EpisodesTab(
    seasons: List<Season>?,
    isFullScreen: Boolean = false
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = if (isFullScreen)
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        else
            Modifier
                .height(400.dp)
                .padding(16.dp)
                .verticalScroll(scrollState)
    ) {
        if (seasons.isNullOrEmpty()) {
            Text(
                text = stringResource(R.string.no_episodes_available),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            seasons.forEachIndexed { seasonIndex, season ->
                SeasonContent(
                    season = season,
                    seasonIndex = seasonIndex
                )
            }
        }
    }
}

@Composable
private fun SeasonContent(
    season: Season,
    seasonIndex: Int
) {
    Text(
        text = "Season ${season.number ?: (seasonIndex + 1)}",
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = Color.White,
        modifier = Modifier.padding(
            vertical = if (seasonIndex == 0) 16.dp else 24.dp
        )
    )
    
    if (season.episodes.isNullOrEmpty()) {
        Text(
            text = "No episodes available for this season",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    } else {
        LazyRow {
            season.episodes?.forEachIndexed { episodeIndex, episode ->
                item {
                    Column(
                        modifier = Modifier
                            .width(160.dp)
                            .padding(end = 12.dp)
                    ) {
                        if (episode.posterImagePath != null) {
                            val imageUrl = "https://api.nortv.xyz/storage/${episode.posterImagePath}"
                            PosterImage(
                                title = episode.title,
                                posterUrl = imageUrl,
                                modifier = Modifier
                                    .height(120.dp)
                                    .fillMaxWidth()
                            )
                        } else {
                            // Placeholder for episode without poster
                            Box(
                                modifier = Modifier
                                    .height(120.dp)
                                    .fillMaxWidth()
                                    .background(Color.Gray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ep ${episodeIndex + 1}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Episode ${episodeIndex + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        if (episode.plot != null) {
                            Text(
                                text = episode.plot,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestedTab(
    selectedTvShow: TvShow,
    similarTvShows: StateFlow<PagingData<TvShow>>,
    refreshScreenWithNewTvShow: (TvShow) -> Unit,
    isFullScreen: Boolean = false
) {
    Box(
        modifier = Modifier
            .height(400.dp)
            .padding(16.dp)
    ) {
        TvShowsRow(
            title = StringConstants
                .Composable
                .movieDetailsScreenSimilarTo(selectedTvShow.title.toString()),
            titleStyle = MaterialTheme.typography.titleMedium,
            tvShows = similarTvShows,
            onMovieSelected = refreshScreenWithNewTvShow
        )
    }
}

@Composable
fun DetailsTab(
    tvShow: TvShow,
    isFullScreen: Boolean = false
) {
    val childPadding = rememberChildPadding()
    
    Column(
        modifier = if (isFullScreen)
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        else
            Modifier.padding(16.dp)
    ) {
        if (isFullScreen) {
            Text(
                text = "TV Show Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            val itemModifier = Modifier.width(192.dp)
            
            TitleValueText(
                modifier = itemModifier,
                title = stringResource(R.string.status),
                value = "Released",
                valueColor = Color.White
            )
            
            tvShow.languages?.first()?.englishName?.let {
                TitleValueText(
                    modifier = itemModifier,
                    title = stringResource(R.string.original_language),
                    value = it,
                    valueColor = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (tvShow.seasonsCount != null && tvShow.seasonsCount > 0) {
            TitleValueText(
                title = "Seasons",
                value = tvShow.seasonsCount.toString(),
                valueColor = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (isFullScreen) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Additional TV show information would appear here in full-screen mode",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}