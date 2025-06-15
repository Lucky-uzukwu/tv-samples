package com.google.jetstream.presentation.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.ImmersiveListMoviesRow
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieHeroSectionCarousel
import com.google.jetstream.presentation.screens.backgroundImageState
import kotlinx.coroutines.flow.StateFlow

sealed class LastViewedItem {
    data class CarouselItem(val movieId: Int, val index: Int) : LastViewedItem()
    data class ImmersiveListItem(val movieId: Int, val catalogId: String, val index: Int) :
        LastViewedItem()
}


@Composable
fun HomeScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    homeScreeViewModel: HomeScreeViewModel = hiltViewModel(),
) {
    val uiState by homeScreeViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = homeScreeViewModel.heroMovies.collectAsLazyPagingItems()

    when (val s = uiState) {
        is HomeScreenUiState.Ready -> {
            Catalog(
                featuredMovies = featuredMovies,
                catalogToMovies = s.catalogToMovies,
                genreToMovies = s.genreToMovies,
                onMovieClick = onMovieClick,
                setSelectedMovie = setSelectedMovie,
                goToVideoPlayer = goToVideoPlayer,
                streamingProviders = s.streamingProviders,
                modifier = Modifier.fillMaxSize(),
            )
        }

        is HomeScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is HomeScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun Catalog(
    featuredMovies: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    streamingProviders: List<StreamingProvider>,
) {

    // Persist last viewed item state
    var lastMovieId by rememberSaveable { mutableIntStateOf(0) }
    var lastCatalogId by rememberSaveable { mutableStateOf<String?>(null) } // null for carousel
    var isCarouselLast by rememberSaveable { mutableStateOf(true) }

    val lazyListState = rememberLazyListState()
    val backgroundState = backgroundImageState()
    var isCarouselFocused by remember { mutableStateOf(true) }

    val catalogToLazyPagingItems = catalogToMovies.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }
    val genreToLazyPagingItems = genreToMovies.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }

    // Restore scroll position when navigating back
    LaunchedEffect(lastMovieId, lastCatalogId) {
        if (isCarouselLast) {
            lazyListState.scrollToItem(0) // Carousel is at index 0
        } else {
            // Find the catalog index for the immersive list
            val catalogIndex =
                catalogToLazyPagingItems.keys.indexOfFirst { it.id == lastCatalogId }
            if (catalogIndex >= 0) {
                lazyListState.scrollToItem(catalogIndex + 1) // +1 for carousel
            }
        }
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

    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {

        item(contentType = "HeroSectionCarousel") {
            MovieHeroSectionCarousel(
                movies = featuredMovies,
                goToVideoPlayer = goToVideoPlayer,
                goToMoreInfo = onMovieClick,

                setSelectedMovie = { movie ->
                    val imageUrl = "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
                    backgroundState.load(
                        url = imageUrl
                    )
                    setSelectedMovie(movie)
                    // Update last viewed item
                    lastMovieId = movie.id
                    lastCatalogId = null
                    isCarouselLast = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                isCarouselFocused = isCarouselFocused,
                initialMovieId = lastMovieId.takeIf { isCarouselLast }
                    ?: 0 // Pass movie ID if carousel was last
            )
        }
        items(
            items = catalogToLazyPagingItems.keys.toList(),
            key = { catalog -> catalog.id }, // Use catalog ID as unique key
            contentType = { "MoviesRow" }
        ) { catalog ->
            val movies: LazyPagingItems<MovieNew>? = catalogToLazyPagingItems[catalog]

            if (movies != null && movies.itemCount > 0) {
                ImmersiveListMoviesRow(
                    movies = movies,
                    sectionTitle = catalog.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie = { movie ->
                        val imageUrl =
                            "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
                        setSelectedMovie(movie)
                        backgroundState.load(
                            url = imageUrl
                        )
                    },
                    modifier = Modifier
                )
            }
        }
    }
}