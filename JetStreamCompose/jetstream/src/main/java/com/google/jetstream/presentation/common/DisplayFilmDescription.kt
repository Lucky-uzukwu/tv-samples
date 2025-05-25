package com.google.jetstream.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.models.Film

@Composable
fun DisplayFilmDescription(
    film: Film,
    style: TextStyle? = null
) {
    if (style != null) {
        film.tagLine?.let {
            Text(
                text = it,
                style = style,
                maxLines = 1,
                color = Color.White,
//                modifier = modifier.padding(top = 8.dp)
            )
        }
    } else {
        film.tagLine?.let {
            Text(
                text = it,
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
}