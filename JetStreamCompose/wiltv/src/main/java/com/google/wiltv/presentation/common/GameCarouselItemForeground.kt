// ABOUTME: Foreground overlay component for game carousel items
// ABOUTME: Shows game details and watch button over the hero carousel background

package com.google.wiltv.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.R
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.presentation.theme.WilTvButtonShape
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameCarouselItemForeground(
    game: CompetitionGame,
    modifier: Modifier = Modifier,
    onMoreInfoClick: () -> Unit = {},
    isCarouselFocused: Boolean = false
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(start = 34.dp, bottom = 32.dp)
            .bringIntoViewRequester(bringIntoViewRequester),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TeamVersusImage(
            game = game,
            modifier = Modifier
                .width(250.dp)
                .height(200.dp)
                .scale(0.9f),
            showLiveBadge = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(visible = isCarouselFocused) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomFillButton(
                    onClick = {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                        onMoreInfoClick()
                    },
                    text = stringResource(R.string.more_info),
                    icon = R.drawable.ic_info,
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
}