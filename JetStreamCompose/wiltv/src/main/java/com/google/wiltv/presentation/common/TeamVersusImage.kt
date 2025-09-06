// ABOUTME: Custom composable for team versus game visualization
// ABOUTME: Creates team matchup display with logos and "VS" text when cover image unavailable

package com.google.wiltv.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.entities.CompetitionGame
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TeamVersusImage(
    modifier: Modifier = Modifier,
    game: CompetitionGame,
    showLiveBadge: Boolean = true,
    isFocused: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamLogo(
                    logoUrl = game.teamAData.logoUrl,
                    teamName = game.teamAData.name,
                    size = 50.dp,
                    fontSize = 14.sp
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused) Color.Black else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                TeamLogo(
                    logoUrl = game.teamBData.logoUrl,
                    teamName = game.teamBData.name,
                    size = 50.dp,
                    fontSize = 14.sp
                )
            }

            val gameTime = try {
                val zonedDateTime = ZonedDateTime.parse(game.gameDate)
                zonedDateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy - h:mm a"))
            } catch (e: Exception) {
                game.gameDate
            }

            Text(
                text = gameTime,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = game.teamAData.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "VS",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isFocused) Color.Black else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Text(
                    text = game.teamBData.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = game.competition.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isFocused) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showLiveBadge && game.isLive) {
            LiveBadge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun LiveBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "LIVE",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}