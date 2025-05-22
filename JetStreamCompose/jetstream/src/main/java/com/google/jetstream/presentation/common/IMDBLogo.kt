package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun IMDbLogo(
    modifier: Modifier = Modifier,
    textColor: Color = Color(0xFF111827), // Tailwind's text-gray-950
    backgroundColor: Color = Color(0xFFFBBF24) // Tailwind's bg-yellow-500
) {

    Text(
        text = "IMDb",
        modifier = modifier
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        color = textColor,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold
        )
    )
}