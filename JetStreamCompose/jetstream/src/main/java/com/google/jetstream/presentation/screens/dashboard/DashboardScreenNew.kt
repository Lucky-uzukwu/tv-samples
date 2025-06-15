package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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

@Composable
fun DashboardScreenNew(
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

    var selectedTab: String by remember { mutableStateOf(Screens.Home()) }

    LaunchedEffect(key1 = Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectedTab = destination.route ?: return@addOnDestinationChangedListener
        }
    }

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
        selectedTab = selectedTab,
        content = {
            Body(
                openCategoryMovieList = openCategoryMovieList,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openVideoPlayer = openVideoPlayer,
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(contentFocusRequester)
                    .focusable()
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

//    // Background with Netflix-like gradient
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                brush = Brush.verticalGradient(
//                    colors = listOf(Color(0xFF1F1F1F), Color(0xFF000000))
//                )
//            )
//    ) {
//        Row(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Sidebar
//            DashboardSideBar(
//                selectedTabIndex = selectedTab,
//                onTabSelected = { screen ->
//                    selectedTab = TopBarTabs.indexOf(screen)
//                    // Navigate to the corresponding screen
//                    navController.navigate(screen()) {
//                        popUpTo(navController.graph.startDestinationId)
//                        launchSingleTop = true
//                    }
//                },
//                contentFocusRequester = contentFocusRequester
//            )
//
//            Spacer(Modifier.width(16.dp))
//            Body(
//                openCategoryMovieList = openCategoryMovieList,
//                openMovieDetailsScreen = openMovieDetailsScreen,
//                openVideoPlayer = openVideoPlayer,
////                    updateTopBarVisibility = { isTopBarVisible = it },
////                    isTopBarVisible = isTopBarVisible,
//                navController = navController,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .focusRequester(contentFocusRequester)
//                    .focusable()
////                        .offset(y = navHostTopPaddingDp)
//                    .background(Color.Black),
//                setSelectedMovie = setSelectedMovie,
//                setSelectedTvShow = setSelectedTvShow,
//                openTvShowDetailsScreen = openTvShowDetailsScreen,
//                onLogOutClick = onLogOutClick
//            )
//
////                // Body content
////                Box(
////                    modifier = Modifier
////                        .fillMaxSize()
////                        .focusRequester(contentFocusRequester)
////                        .focusable()
////                        .background(Color.Black)
////                ) {
////                    NavHost(
////                        navController = navController,
////                        startDestination = "home",
////                        modifier = Modifier
////                            .fillMaxHeight()
////                            .padding(end = 48.dp)
////                    ) {
////                        composable(Screens.Home()) { BodyContent("Home Content") }
////                        composable(Screens.Movies()) { BodyContent("Movies Content") }
////                        composable(Screens.Shows()) { BodyContent("Series Content") }
////                        composable(Screens.Categories()) { BodyContent("Categories Content") }
////                        composable(Screens.Search()) { BodyContent("Search Content") }
////                        composable(Screens.Profile()) { BodyContent("Profile Content") }
////                    }
////                }
//        }
//    }

}

@Composable
fun BodyContent(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun BackPressHandledArea(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) =
    Box(
        modifier = Modifier
            .onPreviewKeyEvent {
                if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                    onBackPressed()
                    true
                } else {
                    false
                }
            }
            .then(modifier),
        content = content
    )


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
    DashboardScreenNew(
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