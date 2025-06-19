package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.backgroundImageState
import com.google.jetstream.presentation.screens.categories.CategoriesScreen
import com.google.jetstream.presentation.screens.dashboard.navigation.drawer.HomeDrawer
import com.google.jetstream.presentation.screens.home.HomeScreen
import com.google.jetstream.presentation.screens.movies.MoviesScreen
import com.google.jetstream.presentation.screens.profile.ProfileScreen
import com.google.jetstream.presentation.screens.search.SearchScreen
import com.google.jetstream.presentation.screens.tvshows.TVShowScreen
import com.google.jetstream.presentation.utils.Padding

val ParentPadding = PaddingValues(vertical = 8.dp, horizontal = 29.dp)

@Composable
fun rememberChildPadding(direction: LayoutDirection = LocalLayoutDirection.current): Padding {
    return remember {
        Padding(
            start = ParentPadding.calculateStartPadding(direction) + 4.dp,
            top = ParentPadding.calculateTopPadding(),
            end = ParentPadding.calculateEndPadding(direction) + 4.dp,
            bottom = ParentPadding.calculateBottomPadding()
        )
    }
}

@Composable
fun DashboardScreen(
    openCategoryMovieList: (categoryId: String) -> Unit = {},
    openMovieDetailsScreen: (movieId: String) -> Unit = {},
    openTvShowDetailsScreen: (tvShowId: String) -> Unit = {},
    openVideoPlayer: (movieId: String) -> Unit = {},
    selectedMovie: MovieNew? = null,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    selectedTvShow: TvShow? = null,
    clearFilmSelection: () -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    onLogOutClick: () -> Unit
) {
    val navController = rememberNavController()
    val backgroundState = backgroundImageState()
    val contentFocusRequester = remember { FocusRequester() }


    Box(modifier = Modifier.fillMaxSize()) {
        val targetBitmap by remember(backgroundState) { backgroundState.drawable }

        val overlayColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)

        Crossfade(targetState = targetBitmap) {
            it?.let {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
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


    HomeDrawer(
        navController = navController,
        content = {
            Body(
                openCategoryMovieList = openCategoryMovieList,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openVideoPlayer = openVideoPlayer,
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
//                    .focusRequester(contentFocusRequester)
//                    .focusable()
                    .background(Color.Black),
                setSelectedMovie = setSelectedMovie,
                setSelectedTvShow = setSelectedTvShow,
                openTvShowDetailsScreen = openTvShowDetailsScreen,
                onLogOutClick = onLogOutClick
            )
        },
    ) { screen ->
        navController.navigate(screen())
    }
}

@Composable
private fun Body(
    modifier: Modifier = Modifier,
    openCategoryMovieList: (categoryId: String) -> Unit,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openTvShowDetailsScreen: (tvShowId: String) -> Unit,
    openVideoPlayer: (movieId: String) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    navController: NavHostController = rememberNavController(),
    onLogOutClick: () -> Unit
) =
    NavHost(
        navController = navController,
        startDestination = Screens.Home(),
    ) {
        composable(Screens.Profile()) {
            ProfileScreen(
                logOutOnClick = onLogOutClick
            )
        }
        composable(Screens.Home()) {
            HomeScreen(
                onMovieClick = { selectedMovie ->
                    openMovieDetailsScreen(selectedMovie.id.toString())
                },
                goToVideoPlayer = { selectedMovie ->
                    openVideoPlayer(selectedMovie.id.toString())
                },
                setSelectedMovie = setSelectedMovie
            )
        }

        composable(Screens.Movies()) {
            MoviesScreen(
                onMovieClick = { selectedMovie ->
                    openMovieDetailsScreen(selectedMovie.id.toString())
                },
                goToVideoPlayer = { selectedMovie ->
                    openVideoPlayer(selectedMovie.id.toString())
                },
                setSelectedMovie = setSelectedMovie
            )
        }

        composable(Screens.Shows()) {
            TVShowScreen(
                onTVShowClick = { show -> openTvShowDetailsScreen(show.id.toString()) },
                goToVideoPlayer = { selectedMovie ->
                    openVideoPlayer(selectedMovie.id.toString())
                },
                setSelectedTvShow = setSelectedTvShow
            )
        }

        composable(Screens.Categories()) {
            CategoriesScreen(
                onCategoryClick = openCategoryMovieList,
            )
        }
        composable(Screens.Search()) {
            SearchScreen(
                onMovieClick = { movie -> openMovieDetailsScreen(movie.id) },
                onScroll = { }
            )
        }


////        composable(Screens.Favourites()) {
////            FavouritesScreen(
////                onMovieClick = openMovieDetailsScreen,
////                onScroll = updateTopBarVisibility,
////                isTopBarVisible = isTopBarVisible
////            )
////        }
    }


@Composable
private fun Body(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) =
    Loading(modifier = modifier)

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun DashboardScreenNewPreview() {
    DashboardScreen(
        openCategoryMovieList = { },
        openMovieDetailsScreen = { },
        openTvShowDetailsScreen = { },
        openVideoPlayer = { },
        selectedMovie = null,
        setSelectedMovie = { },
        selectedTvShow = null,
        clearFilmSelection = { },
        setSelectedTvShow = { },
        onLogOutClick = { },
    )
}