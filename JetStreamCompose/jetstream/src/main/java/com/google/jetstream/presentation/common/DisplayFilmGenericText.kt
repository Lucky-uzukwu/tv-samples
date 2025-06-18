package com.google.jetstream.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import md_theme_light_onPrimary

@Composable
fun DisplayFilmGenericText(
    text: String,
    maxLines: Int = 3,
) {

    Text(
        text = text,
        color = md_theme_light_onPrimary,
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
        maxLines = maxLines,
        fontWeight = FontWeight.ExtraBold,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 5.dp)
    )
}
