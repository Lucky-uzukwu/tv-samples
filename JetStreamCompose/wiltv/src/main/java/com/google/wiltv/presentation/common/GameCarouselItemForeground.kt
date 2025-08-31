// ABOUTME: Foreground overlay component for game carousel items
// ABOUTME: Shows game details and watch button over the hero carousel background

package com.google.wiltv.presentation.common

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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
    onWatchNowClick: () -> Unit,
    isCarouselFocused: Boolean = false
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .padding(start = 34.dp, bottom = 32.dp)
            .width(360.dp)
            .scale(0.7f)
            .bringIntoViewRequester(bringIntoViewRequester),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = game.competition.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (game.isLive) {
                Spacer(modifier = Modifier.width(12.dp))
                LiveBadge()
            }
        }

        Text(
            text = game.description,
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 2
        )

        val gameTime = try {
            val zonedDateTime = ZonedDateTime.parse(game.gameDate)
            zonedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a"))
        } catch (e: Exception) {
            game.gameDate
        }

        Text(
            text = gameTime,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        if (game.streamingLinks.isNotEmpty()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                    onWatchNowClick()
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                shape = ButtonDefaults.shape(shape = WilTvButtonShape),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (game.isLive) "Watch Live" else "Watch",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}