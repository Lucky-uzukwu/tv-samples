// ABOUTME: Composable component for displaying a horizontal row of TV channels
// ABOUTME: Handles focus management, paging data display, and channel selection events

package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.util.StringConstants
import com.google.wiltv.presentation.theme.WilTvBorderWidth
import com.google.wiltv.presentation.theme.WilTvCardShape
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvChannelsRow(
    tvChannels: StateFlow<PagingData<TvChannel>>,
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
    onChannelSelected: (channel: TvChannel) -> Unit = {}
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
            targetState = tvChannels,
            label = "",
        ) { channelState ->
            val channelsAsLazyItems = channelState.collectAsLazyPagingItems()
            val channels = channelsAsLazyItems.itemSnapshotList.items
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
                itemsIndexed(channels, key = { _, channel -> channel.id }) { index, tvChannel ->
                    val itemModifier = if (index == 0) {
                        Modifier.focusRequester(firstItem)
                    } else {
                        Modifier
                    }
                    TvChannelRowItem(
                        modifier = itemModifier.weight(1f),
                        index = index,
                        itemDirection = itemDirection,
                        onChannelSelected = {
                            lazyRow.saveFocusedChild()
                            onChannelSelected(it)
                        },
                        tvChannel = tvChannel,
                        showIndexOverImage = showIndexOverImage
                    )
                }
            }
        }
    }
}

@Composable
fun TvChannelRowItem(
    index: Int,
    tvChannel: TvChannel,
    onChannelSelected: (TvChannel) -> Unit,
    showIndexOverImage: Boolean,
    modifier: Modifier = Modifier,
    itemDirection: ItemDirection = ItemDirection.Vertical,
    onChannelFocused: (TvChannel) -> Unit = {},
    downFocusRequester: FocusRequester? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val imageUrl = tvChannel.logoUrl

    MovieCard(
        onClick = { onChannelSelected(tvChannel) },
        modifier = Modifier
            .border(
                width = WilTvBorderWidth,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = WilTvCardShape
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onChannelFocused(tvChannel)
                }
            }
            .focusProperties {
                left = if (index == 0) {
                    FocusRequester.Default
                } else {
                    FocusRequester.Default
                }
                right = FocusRequester.Default
                down = downFocusRequester ?: FocusRequester.Default
            }
            .then(modifier)
    ) {
        AuthenticatedAsyncImage(
            model = imageUrl,
            contentDescription = StringConstants
                .Composable
                .ContentDescription
                .moviePoster(tvChannel.name),
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(16F / 9F)
        )
    }
}