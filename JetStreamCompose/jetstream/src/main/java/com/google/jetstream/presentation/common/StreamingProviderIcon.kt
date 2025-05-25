package com.google.jetstream.presentation.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun StreamingProviderIcon(
    logoPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = {
        // Default placeholder: a simple spinning indicator
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp / 3), // Adjust size relative to the icon
                strokeWidth = 2.dp
            )
        }
    },
) {
    val imageUrl = "https://stage.nortv.xyz/storage/$logoPath"
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White)
            .then(
                if (borderColor != null) {
                    Modifier.border(BorderStroke(borderWidth, borderColor), CircleShape)
                } else {
                    Modifier
                }
            ).then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true) // Optional: for smooth transition when image loads
                .build(),
            contentDescription = contentDescription, // Fit ensures the whole logo is visible
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, // Crop ensures the image fills the circle
//            loading = {
//                placeholder?.invoke()
//            },
        )
    }
}