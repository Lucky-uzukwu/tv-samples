@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.google.wiltv.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageUrl: String,
    cardAspectRatio: Float = 16f / 9f
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
//                .aspectRatio(cardAspectRatio)
                .background(Color.Transparent, RoundedCornerShape(16.dp)),
        ) {
            EnhancedAsyncImage(
                model = imageUrl, 
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                alignment = Alignment.Center,
                showRetryOnError = true,
                onRetry = { /* Image will be retried automatically */ }
            )
        }
    }
}

@Preview
@Composable
fun PreviewCustomCard() {
    CustomCard(
        imageUrl = "https://live.staticflickr.com/3060/3304130387_1f3c41d5ab.jpg",
        onClick = {},
    )
}