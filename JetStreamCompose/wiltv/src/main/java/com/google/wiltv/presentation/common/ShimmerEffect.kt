// ABOUTME: Shimmer loading effects for better visual feedback during content loading
// ABOUTME: Provides animated placeholders that match content shapes with smooth shimmer animation

package com.google.wiltv.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.presentation.theme.WilTvBottomListPadding
import com.google.wiltv.presentation.theme.WilTvCardShape
import com.google.wiltv.presentation.utils.Padding

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(Size.Zero) }
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width,
        targetValue = 2 * size.width,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.3f),
                Color.White.copy(alpha = 0.1f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width, size.height)
        )
    )
        .onGloballyPositioned { coordinates -> size = coordinates.size.toSize() }
}

@Composable
fun SearchLoadingShimmer(
    modifier: Modifier = Modifier,
    childPadding: Padding = Padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Loading text with subtle animation
        Text(
            text = "Searching...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Grid of shimmer placeholders
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(
                start = childPadding.start + 28.dp,
                end = 16.dp,
                bottom = WilTvBottomListPadding
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(15) { // Show 15 placeholder items
                Box(
                    modifier = Modifier
                        .aspectRatio(1 / 1.5f)
                        .padding(6.dp)
                        .clip(WilTvCardShape)
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
fun CompactShimmerPlaceholder(
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 200.dp,
    height: androidx.compose.ui.unit.Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(4.dp))
            .shimmerEffect()
    )
}