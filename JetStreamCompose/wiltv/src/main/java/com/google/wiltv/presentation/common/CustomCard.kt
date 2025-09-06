@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.google.wiltv.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.google.wiltv.presentation.theme.WilTvCardShape

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    enhancedImageModifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageUrl: String?,
    text: String? = null,
    cardAspectRatio: Float = 16f / 9f,
    customShape: Shape? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 3.dp)
            .height(100.dp)
            .width(100.dp)
    ) {

        Card(
            onClick = onClick,
            modifier = modifier
                .background(Color.Transparent, customShape ?: RoundedCornerShape(16.dp)),
        ) {
            if (!imageUrl.isNullOrBlank()) {
                EnhancedAsyncImage(
                    model = imageUrl,
                    contentDescription = text ?: "Image",
                    modifier = enhancedImageModifier
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    alignment = Alignment.Center,
                    showRetryOnError = true,
                    onRetry = { /* Image will be retried automatically */ }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text ?: "No Image",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB6BAB5),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
