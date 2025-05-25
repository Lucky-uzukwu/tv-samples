package com.google.jetstream.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.data.models.Film

@Composable
fun DisplayFilmTitle(
    film: Film,
    style: TextStyle? = null
) {
    if (style != null) {
        Text(
            text = film.title,
            maxLines = 2,
            color = Color.White,
            style = style
        )
    } else {
        Text(
            text = film.title,
            color = Color.White,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(x = 2f, y = 4f),
                    blurRadius = 2f
                )
            ),
            maxLines = 2
        )
    }
}
