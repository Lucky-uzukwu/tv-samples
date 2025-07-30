package com.google.jetstream.presentation.common

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.util.StringConstants

@Composable
fun CarouselItemImage(
    movie: MovieNew,
    modifier: Modifier = Modifier
) {
    val imageUrl = movie.backdropImageUrl
    var sizeCard by remember { mutableStateOf(Size.Zero) }
    AsyncImage(
        model = imageUrl,
        contentDescription = StringConstants
            .Composable
            .ContentDescription
            .moviePoster(movie.title),
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(21F / 9F)
            .onGloballyPositioned { coordinates ->
                sizeCard = coordinates.size.toSize()
            }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = size.width * 0.8f // Stretch the gradient to 80% of the width
                    )
                )
            },
        contentScale = ContentScale.Crop
    )
}

@Composable
fun CarouselItemImage(
    tvShow: TvShow,
    modifier: Modifier = Modifier
) {
    val imageUrl = tvShow.backdropImageUrl
    var sizeCard by remember { mutableStateOf(Size.Zero) }
    AsyncImage(
        model = imageUrl,
        contentDescription = tvShow.title?.let {
            StringConstants
                .Composable
                .ContentDescription
                .moviePoster(it)
        },
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(21F / 9F)
            .onGloballyPositioned { coordinates ->
                sizeCard = coordinates.size.toSize()
            }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = size.width * 0.8f // Stretch the gradient to 80% of the width
                    )
                )
            },
        contentScale = ContentScale.Crop
    )
}

