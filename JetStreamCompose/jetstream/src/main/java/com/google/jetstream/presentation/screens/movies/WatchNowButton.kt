package com.google.jetstream.presentation.screens.movies

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.presentation.theme.JetStreamButtonShape

@Composable
fun WatchNowButton(
    modifier: Modifier = Modifier,
    filmId: Int,
    openVideoPlayer: (movieId: String) -> Unit,
) {
    Button(
        onClick = { openVideoPlayer(filmId.toString()) },
        modifier = modifier.padding(top = 24.dp).focusable(),
        colors = ButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        shape = ButtonDefaults.shape(shape = JetStreamButtonShape)
    ) {
        Icon(
            imageVector = Icons.Outlined.PlayArrow,
            contentDescription = null
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "Watch Now",
            style = MaterialTheme.typography.titleSmall,
        )
    }
}