package com.google.jetstream.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.presentation.utils.formatDuration

@Composable
fun DisplayFilmExtraInfo(
    modifier: Modifier = Modifier,
    getYear: String?,
    combinedGenre: String?,
    duration: Int?,
    style: TextStyle? = null,
) {
    val year = getYear ?: return
    val genre = combinedGenre ?: return

    val text = buildString {
        append(year)
        append(" - ")
        append(genre)
        duration?.let {
            append(" - ")
            append(it.formatDuration())
        }
    }

    if (style != null) {
        Text(
            modifier = modifier,
            text = text,
            style = style,
            color = Color.White,
            maxLines = 1,
        )
    } else {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.65f
                ),
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(x = 2f, y = 4f),
                    blurRadius = 2f
                )
            ),
            fontWeight = FontWeight.Light,
            maxLines = 1,
            modifier = modifier,
        )
    }
}

@Composable
fun DisplayFilmExtraInfoWithoutDuration(
    getYear: String?,
    combinedGenre: String?,
    style: TextStyle? = null,
) {
    if (style != null) {
        Text(
            text = "$getYear - $combinedGenre",
            style = style,
            color = Color.White,
            maxLines = 1,
        )
    } else {
        Text(
            text = "$getYear - $combinedGenre",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.65f
                ),
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(x = 2f, y = 4f),
                    blurRadius = 2f
                )
            ),
            maxLines = 1,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}