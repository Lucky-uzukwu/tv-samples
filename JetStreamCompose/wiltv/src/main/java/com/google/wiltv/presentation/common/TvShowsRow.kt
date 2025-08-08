package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.TvShow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvShowsRow(
    tvShows: StateFlow<PagingData<TvShow>>,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    startPadding: Dp = 8.dp,
    endPadding: Dp = 8.dp,
    title: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp
    ),
    showIndexOverImage: Boolean = false,
    onMovieSelected: (movie: TvShow) -> Unit = {}
) {
    val (lazyRow, firstItem) = remember { FocusRequester.createRefs() }

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
        AnimatedContent(
            targetState = tvShows,
            label = "",
        ) { movieState ->
            val similarMoviesAsLazyItems = movieState.collectAsLazyPagingItems()
            val similarMovies = similarMoviesAsLazyItems.itemSnapshotList.items
            LazyRow(
                contentPadding = PaddingValues(
                    start = startPadding,
                    end = endPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .focusRequester(lazyRow)
                    .focusRequester(firstItem)
            ) {
                itemsIndexed(similarMovies, key = { _, movie -> movie.id }) { index, tvShow ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    TvShowRowItem(
                        modifier = itemModifier.weight(1f),
                        index = index,
                        itemDirection = itemDirection,
                        onTvShowSelected = {
                            lazyRow.saveFocusedChild()
                            onMovieSelected(it)
                        },
                        tvShow = tvShow,
                        showIndexOverImage = showIndexOverImage
                    )
                }
            }
        }
    }
}
