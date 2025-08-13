// ABOUTME: Foreground overlay component for TV channel carousel items
// ABOUTME: Displays channel information and play button with focus-based visibility animation

package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import com.google.wiltv.R
import com.google.wiltv.data.network.TvChannel
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TvChannelCarouselItemForeground(
    tvChannel: TvChannel,
    modifier: Modifier = Modifier,
    onChannelClick: () -> Unit,
    isCarouselFocused: Boolean = false
) {
    val combinedGenre = tvChannel.genres.joinToString(" ") { genre -> genre.name }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(start = 34.dp, bottom = 32.dp)
            .width(360.dp)
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.padding(bottom = 5.dp),
        ) {
            DisplayFilmExtraInfo(
                getYear = null, // Channels don't have release years
                combinedGenre = combinedGenre,
                duration = null // Channels don't have duration
            )
        }
        
        DisplayFilmTitle(
            title = tvChannel.name,
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 2
        )
        
        tvChannel.language?.let { language ->
            DisplayFilmGenericText(
                modifier = Modifier.padding(top = 4.dp),
                text = "Language: $language",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
        }

        AnimatedVisibility(visible = isCarouselFocused) {
            CustomFillButton(
                onClick = {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                    onChannelClick()
                },
                text = stringResource(R.string.play),
                icon = R.drawable.play_icon,
                iconTint = MaterialTheme.colorScheme.inverseOnSurface,
                buttonColor = ButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    focusedContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                ),
            )
        }
    }
}