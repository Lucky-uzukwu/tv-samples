package com.google.jetstream.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.tv.material3.CarouselDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ShapeDefaults
import com.google.jetstream.presentation.utils.getListBPosition

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BoxScope.CarouselIndicator(
    itemCount: Int,
    activeItemIndex: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(start = 500.dp, bottom = 100.dp)
            .graphicsLayer {
                clip = true
                shape = ShapeDefaults.ExtraSmall
            }
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicator Row
            CarouselDefaults.IndicatorRow(
                itemCount = itemCount,
                activeItemIndex = getListBPosition(activeItemIndex).position,
                indicator =
                    { isActive ->
                        val activeColor = Color.White.copy(alpha = 1f) // Increased whiteness
                        val inactiveColor = activeColor.copy(alpha = 0.3f)
                        Box(
                            modifier =
                                Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isActive) activeColor else inactiveColor,
                                        shape = CircleShape,
                                    ),
                        )
                    }
            )
        }
    }
}
