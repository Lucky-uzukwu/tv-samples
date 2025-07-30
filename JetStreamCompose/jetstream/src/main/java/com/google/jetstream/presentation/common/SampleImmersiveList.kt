package com.google.jetstream.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.google.jetstream.data.models.MovieNew

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SampleImmersiveList(
    modifier: Modifier = Modifier,
    movie: MovieNew
) {
    val items = remember { listOf(Color.Red, Color.Green, Color.Yellow) }
    val selectedItem = remember { mutableStateOf<Color?>(null) }

    // Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        val bgColor = selectedItem.value

        // Background
        if (bgColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(20f / 7)
                    .background(bgColor)
            ) {

                Background(
                    movie = movie,
                    visible = true,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Rows
        LazyRow(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(20.dp),
        ) {
            items(items) { color ->
                Surface(
                    onClick = { },
                    modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(16f / 9)
                        .onFocusChanged {
                            if (it.hasFocus) {
                                selectedItem.value = color
                            }
                        },
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = color,
                        focusedContainerColor = color,
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(
                            border = BorderStroke(2.dp, Color.White),
                            inset = 4.dp,
                        )
                    )
                ) {}
            }
        }
    }
}

@Composable
private fun Background(
    movie: MovieNew,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val imageUrl = movie.backdropImageUrl
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Crossfade(
            targetState = movie,
            label = "posterUriCrossfade",

            ) {
            imageUrl?.let { posterUrl ->
                PosterImage(
                    title = it.title,
                    posterUrl = posterUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
