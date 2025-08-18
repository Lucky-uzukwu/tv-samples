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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MovieDetailTabs() {
    val tabs = listOf("Episodes", "Suggested", "Details")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            contentColor = Color.White,
            containerColor = Color.Black // adjust to your theme
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.bodyMedium) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> EpisodesTab()
            1 -> SuggestedTab()
            2 -> DetailsTab()
        }
    }
}

@Composable
fun EpisodesTab() {
    LazyColumn(
        modifier = Modifier.height(400.dp) // Fixed height to prevent infinite constraints
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
fun SuggestedTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Suggested content here")
    }
}

@Composable
fun DetailsTab() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Year: 2025")
        Text("Duration: 2h 10m")
        Text("Genres: Action, Drama")
    }
}
