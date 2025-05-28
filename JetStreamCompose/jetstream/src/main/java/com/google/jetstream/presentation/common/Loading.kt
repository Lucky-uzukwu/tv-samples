package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R

@Composable
fun Loading(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.message_loading),
            style = style,
            color = Color.White
        )
    }
}
