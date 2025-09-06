// ABOUTME: Composable wrapper around AsyncImage that automatically uses authenticated ImageLoader
// ABOUTME: Provides same API as regular AsyncImage but includes user authentication headers

package com.google.wiltv.presentation.common

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.google.wiltv.data.network.AuthenticatedImageLoader
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AuthenticatedAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    val context = LocalContext.current

    // Get the authenticated ImageLoader from Hilt
    val imageLoader = EntryPointAccessors
        .fromApplication(context, ImageLoaderEntryPoint::class.java)
        .getAuthenticatedImageLoader()

    val imageRequestBuilder = ImageRequest.Builder(context)
        .data(model)
        .crossfade(300)
        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
        .diskCachePolicy(coil.request.CachePolicy.ENABLED)

    model?.let {
        if (it.endsWith(".svg")) {
            imageRequestBuilder.decoderFactory(SvgDecoder.Factory())
        }
    }

    AsyncImage(
        model = imageRequestBuilder.build(),
        contentDescription = contentDescription,
        imageLoader = imageLoader,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        onLoading = onLoading,
        onSuccess = onSuccess,
        onError = onError,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ImageLoaderEntryPoint {
    @AuthenticatedImageLoader
    fun getAuthenticatedImageLoader(): ImageLoader
}