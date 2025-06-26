package com.google.jetstream.presentation.screens.tvshows

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.TvShowHeroSectionCarousel
import com.google.jetstream.presentation.screens.backgroundImageState
import com.google.jetstream.presentation.screens.home.carouselSaver
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TVShowScreen(
    onTVShowClick: (tvShow: TvShow) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    tvShowScreenViewModel: TvShowScreenViewModel = hiltViewModel(),
) {
    val uiState by tvShowScreenViewModel.uiState.collectAsStateWithLifecycle()
    val heroSectionTvShows = tvShowScreenViewModel.heroSectionTvShows.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    when (val currentState = uiState) {
        is TvShowScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is TvShowScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())

        is TvShowScreenUiState.Ready -> {
            Catalog(
                heroSectionTvShows = heroSectionTvShows,
                catalogToTvShows = currentState.catalogToTvShows,
                genreToTvShows = currentState.genreToTvShows,
                onTVShowClick = onTVShowClick,
                setSelectedTvShow = setSelectedTvShow,
                goToVideoPlayer = goToVideoPlayer,
                streamingProviders = currentState.streamingProviders,
                carouselState = carouselState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Catalog(
    heroSectionTvShows: LazyPagingItems<TvShow>,
    catalogToTvShows: Map<Catalog, StateFlow<PagingData<TvShow>>>,
    genreToTvShows: Map<Genre, StateFlow<PagingData<TvShow>>>,
    onTVShowClick: (tvShow: TvShow) -> Unit,
    goToVideoPlayer: (tvShow: TvShow) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    streamingProviders: List<StreamingProvider>,
    carouselState: CarouselState,
) {
    val backgroundState = backgroundImageState()
    var isCarouselFocused by remember { mutableStateOf(true) }

    val catalogToLazyPagingItems = catalogToTvShows.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }
    val genreToLazyPagingItems = genreToTvShows.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }

    Box(modifier = modifier) {
        val targetBitmap by remember(backgroundState) { backgroundState.drawable }

        val overlayColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

        Crossfade(targetState = targetBitmap) {
            it?.let {
                Image(
                    modifier = modifier
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                Brush.horizontalGradient(
                                    listOf(
                                        overlayColor,
                                        overlayColor.copy(alpha = 0.8f),
                                        Color.Transparent
                                    )
                                )
                            )
                            drawRect(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent, overlayColor.copy(alpha = 0.5f)
                                    )
                                )
                            )
                        },
                    bitmap = it,
                    contentDescription = "Hero item background",
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }


    TvLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Shows Screen" },
        verticalArrangement = Arrangement.spacedBy(40.dp),
        contentPadding = PaddingValues(vertical = 40.dp)
    ) {

        item(contentType = "TvShowHeroSectionCarousel") {
            TvShowHeroSectionCarousel(
                tvShows = heroSectionTvShows,
                goToMoreInfo = onTVShowClick,
                setSelectedTvShow = { tvShow ->
                    val imageUrl =
                        "https://stage.nortv.xyz/" + "storage/" + tvShow.backdropImagePath
                    backgroundState.load(
                        url = imageUrl
                    )
                    setSelectedTvShow(tvShow)
                },
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                carouselState = carouselState,
            )
        }

//        items(
//            count = genreToLazyPagingItems.size,
//            key = { genre -> genre },
//            contentType = { "GenreRow" }
//        ) { genre ->
//            val genreKey = genreToLazyPagingItems.keys.elementAt(genre)
//            val tvShows: LazyPagingItems<TvShow>? = genreToLazyPagingItems[genreKey]
//
//            if (tvShows != null) {
//                ImmersiveShowsList(
//                    tvShows = tvShows,
//                    sectionTitle = genreKey.name,
//                    onTvShowClick = onTVShowClick,
//                    setSelectedTvShow = { tvShow ->
//                        val imageUrl =
//                            "https://stage.nortv.xyz/" + "storage/" + tvShow.backdropImagePath
//                        setSelectedTvShow(tvShow)
//                        backgroundState.load(
//                            url = imageUrl
//                        )
//                    },
//                    modifier = Modifier,
//                )
//            }
//        }

//        items(
//            count = catalogToLazyPagingItems.size,
//            key = { catalog -> catalog.hashCode() }, //e catalog ID as unique key
//            contentType = { "GenreRow" }
//        ) { catalog ->
//            val catalogKey = catalogToLazyPagingItems.keys.elementAt(catalog)
//            val tvShows: LazyPagingItems<TvShow>? = catalogToLazyPagingItems[catalogKey]
//
//            ImmersiveShowsList(
//                tvShows = tvShows!!,
//                sectionTitle = catalogKey.name,
//                onTvShowClick = onTVShowClick,
//                setSelectedTvShow = { tvShow ->
//                    val imageUrl =
//                        "https://stage.nortv.xyz/" + "storage/" + tvShow.backdropImagePath
//                    setSelectedTvShow(tvShow)
//                    backgroundState.load(
//                        url = imageUrl
//                    )
//                },
//                modifier = Modifier,
//            )
//        }
    }
}