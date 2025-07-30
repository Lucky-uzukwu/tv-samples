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
import md_theme_light_onPrimary

@Composable
fun DisplayFilmGenericText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle? = null,
    maxLines: Int = 3,
    fontWeight: FontWeight = FontWeight.Normal
) {

    Text(
        modifier = modifier,
        text = text,
        color = md_theme_light_onPrimary,
        style = style
            ?: MaterialTheme.typography.bodySmall.copy(
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
        fontWeight = fontWeight,
        overflow = TextOverflow.Ellipsis,
    )
}
