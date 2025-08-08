package com.google.wiltv.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun DisplayFilmTitle(
    modifier: Modifier = Modifier,
    title: String,
    style: TextStyle? = null,
    maxLines: Int = 1
) {
    if (style != null) {
        Text(
            modifier = modifier,
            text = title,
            maxLines = maxLines,
            color = Color.White,
            style = style,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.W700,
        )
    } else {
        Text(
            modifier = modifier,
            text = title,
            color = Color.White,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.W900,
            style = MaterialTheme.typography.displaySmall.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(x = 2f, y = 4f),
                    blurRadius = 2f
                )
            ),
            maxLines = maxLines
        )
    }
}
