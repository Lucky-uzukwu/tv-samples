// ABOUTME: Enhanced image components with built-in error handling and retry functionality
// ABOUTME: Uses existing AuthenticatedAsyncImage but adds comprehensive error recovery UI

package com.google.wiltv.presentation.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter

@Composable
fun EnhancedPosterImage(
    title: String,
    posterUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    var retryKey by remember { mutableStateOf(0) }
    var isError by remember { mutableStateOf(false) }
    
    if (isError) {
        MoviePosterPlaceholder(
            title = title,
            onRetry = {
                isError = false
                retryKey++
            },
            modifier = modifier
        )
    } else {
        AuthenticatedAsyncImage(
            model = "$posterUrl?retry=$retryKey",
            contentDescription = "Poster for $title",
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            onError = { isError = true },
            onSuccess = { isError = false }
        )
    }
}

@Composable
fun EnhancedBackdropImage(
    title: String,
    backdropUrl: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    var retryKey by remember { mutableStateOf(0) }
    var isError by remember { mutableStateOf(false) }
    
    if (isError) {
        BackdropPlaceholder(
            title = title,
            subtitle = subtitle,
            onRetry = {
                isError = false
                retryKey++
            },
            modifier = modifier
        )
    } else {
        AuthenticatedAsyncImage(
            model = "$backdropUrl?retry=$retryKey",
            contentDescription = "Backdrop for $title",
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            onError = { isError = true },
            onSuccess = { isError = false }
        )
    }
}

@Composable
fun EnhancedProfileImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    var retryKey by remember { mutableStateOf(0) }
    var isError by remember { mutableStateOf(false) }
    
    if (isError) {
        GenericImagePlaceholder(
            onRetry = {
                isError = false
                retryKey++
            },
            modifier = modifier,
            contentDescription = contentDescription
        )
    } else {
        AuthenticatedAsyncImage(
            model = "$imageUrl?retry=$retryKey",
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            onError = { isError = true },
            onSuccess = { isError = false }
        )
    }
}

@Composable
fun EnhancedAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
    showRetryOnError: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    var retryKey by remember { mutableStateOf(0) }
    var isError by remember { mutableStateOf(false) }
    
    if (isError && showRetryOnError && onRetry != null) {
        GenericImagePlaceholder(
            onRetry = {
                isError = false
                retryKey++
                onRetry()
            },
            modifier = modifier,
            contentDescription = contentDescription ?: "Image placeholder"
        )
    } else {
        AuthenticatedAsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier,
            placeholder = placeholder,
            error = error,
            fallback = fallback,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            onError = { isError = showRetryOnError },
            onSuccess = { isError = false }
        )
    }
}