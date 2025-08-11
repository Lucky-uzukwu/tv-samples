package com.google.wiltv.presentation.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.util.DebugLogger
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient

@Stable
@Immutable
class BackgroundState(
    private val coilImageLoader: ImageLoader,
    private val coilBuilder: ImageRequest.Builder,
) {
    val drawable by lazy { mutableStateOf<ImageBitmap?>(null) }
    private var job: Deferred<ImageResult>? = null
    private var lastLoadedUrl: String? = null

    fun load(url: String, onSuccess: () -> Unit = {}, onError: () -> Unit = {}) {
        // Skip loading if same URL is already loaded
        if (lastLoadedUrl == url && drawable.value != null) {
            onSuccess()
            return
        }

        job?.cancel()

        val request = coilBuilder
            .data(url)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .crossfade(300)
            .target(
                onSuccess = { result ->
                    drawable.value = (result as? BitmapDrawable)?.bitmap?.asImageBitmap()
                    lastLoadedUrl = url
                    onSuccess()
                }, 
                onError = { _ ->
                    onError()
                }
            )
            .build()

        job = coilImageLoader.enqueue(request).job
    }
}


@Composable
fun backgroundImageState(): BackgroundState {
    val context = LocalContext.current
    
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("background_images"))
                    .maxSizePercent(0.02) // Use 2% of disk space
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }

    val builder = remember { ImageRequest.Builder(context) }

    return remember(imageLoader, builder) {
        BackgroundState(imageLoader, builder)
    }
}