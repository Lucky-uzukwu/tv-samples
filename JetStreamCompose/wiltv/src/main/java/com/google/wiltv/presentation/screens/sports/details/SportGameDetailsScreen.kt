// ABOUTME: Sports game details screen showing game info and streaming link selection
// ABOUTME: Displays team information, competition details, and clickable streaming links

package com.google.wiltv.presentation.screens.sports.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.google.wiltv.data.entities.CompetitionGame
import com.google.wiltv.presentation.common.EnhancedBackdropImage
import com.google.wiltv.presentation.common.LiveBadge
import com.google.wiltv.presentation.common.Loading
import com.google.wiltv.presentation.screens.ErrorScreen
import com.google.wiltv.presentation.theme.WilTvButtonShape
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object SportGameDetailsScreen {
    const val GameIdBundleKey = "gameId"
}

@Composable
fun SportGameDetailsScreen(
    onBackPressed: () -> Unit,
    openVideoPlayer: (streamingLink: String, title: String?) -> Unit,
    sportGameDetailsViewModel: SportGameDetailsViewModel = hiltViewModel(),
) {
    val uiState by sportGameDetailsViewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        onBackPressed()
    }

    when (val currentState = uiState) {
        is SportGameDetailsUiState.Loading -> {
            Loading(modifier = Modifier.fillMaxSize())
        }
        is SportGameDetailsUiState.Error -> {
            ErrorScreen(
                uiText = currentState.message,
                onRetry = {
                    sportGameDetailsViewModel.retryOperation()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        is SportGameDetailsUiState.Done -> {
            GameDetails(
                game = currentState.game,
                onBackPressed = onBackPressed,
                openVideoPlayer = openVideoPlayer,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun GameDetails(
    game: CompetitionGame,
    onBackPressed: () -> Unit,
    openVideoPlayer: (streamingLink: String, title: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Create focus requesters for streaming links
    val streamingLinkFocusRequesters = remember(game.streamingLinks.size) {
        List(game.streamingLinks.size) { FocusRequester() }
    }

    LaunchedEffect(game.id) {
        lazyListState.scrollToItem(0)
        // Focus on first streaming link if available
        if (streamingLinkFocusRequesters.isNotEmpty()) {
            streamingLinkFocusRequesters[0].requestFocus()
        }
    }

    Box(modifier = modifier) {
        EnhancedBackdropImage(
            title = game.description,
            backdropUrl = game.featuredImageUrl ?: game.coverImageUrl ?: "",
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 48.dp,
                end = 48.dp,
                top = 200.dp,
                bottom = 48.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                GameHeader(
                    game = game
                )
            }

            if (game.streamingLinks.isNotEmpty()) {
                item {
                    Text(
                        text = "Watch Options",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Add streaming links as individual items for proper focus management
                game.streamingLinks.forEachIndexed { index, link ->
                    item {
                        StreamingLinkCard(
                            streamingOption = StreamingLinkOption(index + 1, link),
                            focusRequester = streamingLinkFocusRequesters[index],
                            onStreamingLinkSelected = { selectedLink ->
                                openVideoPlayer(selectedLink, game.description)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameHeader(
    game: CompetitionGame,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = game.competition.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (game.isLive) {
                Spacer(modifier = Modifier.width(16.dp))
                LiveBadge()
            }
        }

        Text(
            text = game.description,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        val gameTime = try {
            val zonedDateTime = ZonedDateTime.parse(game.gameDate)
            zonedDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' h:mm a"))
        } catch (e: Exception) {
            game.gameDate
        }

        Text(
            text = gameTime,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )

        TeamsInfo(
            teamA = game.teamAData,
            teamB = game.teamBData,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun TeamsInfo(
    teamA: com.google.wiltv.data.entities.Team,
    teamB: com.google.wiltv.data.entities.Team,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamDisplay(team = teamA, modifier = Modifier.weight(1f))
        
        Text(
            text = "VS",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold
        )
        
        TeamDisplay(team = teamB, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TeamDisplay(
    team: com.google.wiltv.data.entities.Team,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = team.logoUrl,
            contentDescription = "${team.name} logo",
            modifier = Modifier.size(60.dp),
            contentScale = ContentScale.Fit
        )
        
        Text(
            text = team.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

data class StreamingLinkOption(
    val optionNumber: Int,
    val link: String
)

@Composable
private fun StreamingLinkCard(
    streamingOption: StreamingLinkOption,
    focusRequester: FocusRequester,
    onStreamingLinkSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onStreamingLinkSelected(streamingOption.link) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.6f),
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Streaming Option ${streamingOption.optionNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "Watch",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}