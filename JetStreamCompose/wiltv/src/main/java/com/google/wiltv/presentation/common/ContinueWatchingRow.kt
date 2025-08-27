// ABOUTME: Continue Watching row component for displaying recently watched content with progress
// ABOUTME: Shows movies and TV shows that can be resumed with progress indicators
package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.data.models.ContinueWatchingItem
import com.google.wiltv.data.models.MovieNew

@Composable
fun ContinueWatchingRow(
    continueWatchingItems: List<ContinueWatchingItem>,
    onMovieClick: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier
) {
    if (continueWatchingItems.isEmpty()) return
    
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        Text(
            text = "Continue Watching",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(continueWatchingItems) { item ->
                ContinueWatchingCard(
                    item = item,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.width(160.dp).height(240.dp)
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onMovieClick: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        MovieCard(
            onClick = { onMovieClick(item.movie) },
            isInWatchlist = false, // For now, we don't show watchlist status here
            modifier = Modifier.matchParentSize(),
            image = {
                // Simple placeholder for now - we'll need to add proper image loading
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.movie.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
        
        // Progress indicator at bottom
        LinearProgressIndicator(
            progress = { item.progressPercentage },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp),
            color = Color.Red,
            trackColor = Color.Gray
        )
    }
}