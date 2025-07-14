package com.google.jetstream.presentation.screens.movies

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
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
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.CarouselState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.network.Catalog
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.ImmersiveListMoviesRow
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.common.MovieHeroSectionCarousel
import com.google.jetstream.presentation.common.StreamingProvidersRow
import com.google.jetstream.presentation.screens.backgroundImageState
import com.google.jetstream.presentation.screens.home.carouselSaver
import kotlinx.coroutines.flow.StateFlow

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
fun MoviesScreen(
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    moviesScreenViewModel: MoviesScreenViewModel = hiltViewModel(),
) {
    val uiState by moviesScreenViewModel.uiState.collectAsStateWithLifecycle()
    val featuredMovies = moviesScreenViewModel.heroSectionMovies.collectAsLazyPagingItems()
    val carouselState = rememberSaveable(saver = carouselSaver) { CarouselState(0) }

    when (val s = uiState) {
        is MoviesScreenUiState.Ready -> {
            Catalog(
                featuredMovies = featuredMovies,
                catalogToMovies = s.catalogToMovies,
                genreToMovies = s.genreToMovies,
                onMovieClick = onMovieClick,
                setSelectedMovie = setSelectedMovie,
                onStreamingProviderClick = onStreamingProviderClick,
                goToVideoPlayer = goToVideoPlayer,
                streamingProviders = s.streamingProviders,
                carouselState = carouselState,
                modifier = Modifier.fillMaxSize(),
            )
        }

        is MoviesScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is MoviesScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }
}

@Composable
@OptIn(ExperimentalTvMaterial3Api::class)
private fun Catalog(
    featuredMovies: LazyPagingItems<MovieNew>,
    catalogToMovies: Map<Catalog, StateFlow<PagingData<MovieNew>>>,
    genreToMovies: Map<Genre, StateFlow<PagingData<MovieNew>>>,
    onStreamingProviderClick: (streamingProvider: StreamingProvider) -> Unit,
    onMovieClick: (movie: MovieNew) -> Unit,
    goToVideoPlayer: (movie: MovieNew) -> Unit,
    modifier: Modifier = Modifier,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    streamingProviders: List<StreamingProvider>,
    carouselState: CarouselState,
) {
    val backgroundState = backgroundImageState()
    val catalogToLazyPagingItems = catalogToMovies.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }
    val genreToLazyPagingItems = genreToMovies.mapValues { (_, flow) ->
        flow.collectAsLazyPagingItems()
    }

    val (carouselFocusRequester, firstLazyRowItemUnderCarouselRequester) = remember { FocusRequester.createRefs() }
    val lazyRowState = rememberTvLazyListState()

    var carouselScrollEnabled by remember { mutableStateOf(true) }

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
            .padding(start = 28.dp)
            .semantics { contentDescription = "Movie Screen" },
        verticalArrangement = Arrangement.spacedBy(30.dp),
        contentPadding = PaddingValues(vertical = 40.dp)
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
                },
                carouselState = carouselState,
                modifier = Modifier
                    .height(340.dp)
                    .fillMaxWidth(),
                carouselScrollEnabled = carouselScrollEnabled,
                firstLazyRowItemUnderCarouselRequester = firstLazyRowItemUnderCarouselRequester,
                carouselFocusRequester = carouselFocusRequester,
                lazyRowState = lazyRowState
            )
        }
//
//        item(
//            contentType = "StreamingProvidersRow",
//            key = "movieScreenStreamingProvidersRow"
//        ) {
//            StreamingProvidersRow(
//                streamingProviders = streamingProviders,
//                onClick = onStreamingProviderClick,
//                modifier = Modifier,
//                firstItemFocusRequester = firstLazyRowItemUnderCarouselRequester,
//                aboveFocusRequester = carouselFocusRequester,
//                lazyRowState = lazyRowState
//            )
//        }

        items(
            count = catalogToLazyPagingItems.size,
            key = { catalog -> catalogToLazyPagingItems.keys.elementAt(catalog).id }, // Use catalog ID as unique key
            contentType = { "MoviesRow" }
        ) { catalog ->
            val catalogKey = catalogToLazyPagingItems.keys.elementAt(catalog)
            val movies = catalogToLazyPagingItems[catalogKey]

            if (movies != null && movies.itemCount > 0) {
                ImmersiveListMoviesRow(
                    movies = movies,
                    sectionTitle = catalogKey.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie = { movie ->
                        carouselScrollEnabled = false
                        val imageUrl =
                            "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
                        setSelectedMovie(movie)
                        backgroundState.load(
                            url = imageUrl
                        )
                    },
                )
            }
        }

        items(
            count = genreToLazyPagingItems.size,
            key = { genre -> genreToLazyPagingItems.keys.elementAt(genre).id }, // Use catalog ID as unique key
            contentType = { "MoviesRow" }
        ) { genre ->
            val genreKey = genreToLazyPagingItems.keys.elementAt(genre)
            val movies = genreToLazyPagingItems[genreKey]

            if (movies != null && movies.itemCount > 0) {
                ImmersiveListMoviesRow(
                    movies = movies,
                    sectionTitle = genreKey.name,
                    onMovieClick = onMovieClick,
                    setSelectedMovie = { movie ->
                        carouselScrollEnabled = false
                        val imageUrl =
                            "https://stage.nortv.xyz/" + "storage/" + movie.backdropImagePath
                        setSelectedMovie(movie)
                        backgroundState.load(
                            url = imageUrl
                        )
                    },
                )
            }
        }
    }
}