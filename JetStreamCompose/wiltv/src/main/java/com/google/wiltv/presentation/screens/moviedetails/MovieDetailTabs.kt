package com.google.wiltv.presentation.screens.moviedetails

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp

@Composable
fun MovieDetailTabs(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
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
            containerColor = Color.Black // adjust to your theme
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier
                        .focusRequester(focusRequesters[index])
                        .focusProperties {
                            // Always allow UP navigation to PlayButton since it's now always in composition
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
                                // In a real app, you might want to log this
                            }
                        }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> EpisodesTab(isFullScreen = isFullScreen)
            1 -> SuggestedTab(isFullScreen = isFullScreen)
            2 -> DetailsTab(isFullScreen = isFullScreen)
        }
    }
}

@Composable
fun EpisodesTab(isFullScreen: Boolean = false) {
    LazyColumn(
        modifier = if (isFullScreen) 
            Modifier.fillMaxSize().padding(16.dp)
        else 
            Modifier.height(400.dp) // Fixed height to prevent infinite constraints
    ) {
        item {
            Text("Season 1 - 6 Episodes", style = MaterialTheme.typography.bodyLarge)
        }
        items(6) { episodeIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.Gray) // placeholder for episode image
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Episode ${episodeIndex + 1} - Resurrection")
                    Text("Description if necessary")
                }
            }
        }
    }
}

@Composable
fun SuggestedTab(isFullScreen: Boolean = false) {
    Box(
        modifier = if (isFullScreen) 
            Modifier.fillMaxSize()
        else 
            Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFullScreen) "Full-screen suggested movies would appear here" else "Suggested content here",
            style = if (isFullScreen) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
fun DetailsTab(isFullScreen: Boolean = false) {
    Column(
        modifier = if (isFullScreen) 
            Modifier.fillMaxSize().padding(24.dp)
        else 
            Modifier.padding(16.dp)
    ) {
        if (isFullScreen) {
            Text(
                text = "Movie Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Text("Year: 2025", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Duration: 2h 10m", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Genres: Action, Drama", color = Color.White, style = MaterialTheme.typography.bodyMedium)
        
        if (isFullScreen) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Additional movie information would appear here in full-screen mode", 
                 color = Color.White.copy(alpha = 0.7f), 
                 style = MaterialTheme.typography.bodySmall)
        }
    }
}
