package com.google.wiltv.presentation.screens.dashboard

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.presentation.screens.Screens
import com.google.wiltv.presentation.screens.categories.CategoriesScreen
import com.google.wiltv.presentation.screens.dashboard.navigation.drawer.HomeDrawer
import com.google.wiltv.presentation.screens.home.HomeScreen
import com.google.wiltv.presentation.screens.movies.MoviesScreen
import com.google.wiltv.presentation.screens.profile.ProfileScreen
import com.google.wiltv.presentation.screens.search.SearchScreen
import com.google.wiltv.presentation.screens.tvshows.TVShowScreen
import com.google.wiltv.presentation.utils.Padding

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
    openStreamingProviderMovieList: (streamingProvider: StreamingProvider) -> Unit = {},
    openStreamingProvideShowList: (streamingProvider: StreamingProvider) -> Unit = {},
    setSelectedMovie: (movie: MovieNew) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    onLogOutClick: () -> Unit,
    onNavigateToProfileSelection: () -> Unit = {}
) {
    val navController = rememberNavController()

    HomeDrawer(
        navController = navController,
        content = {
            Body(
                openCategoryMovieList = openCategoryMovieList,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openVideoPlayer = openVideoPlayer,
                navController = navController,
                modifier = Modifier
                    .fillMaxSize(),
                setSelectedMovie = setSelectedMovie,
                setSelectedTvShow = setSelectedTvShow,
                openTvShowDetailsScreen = openTvShowDetailsScreen,
                onLogOutClick = onLogOutClick,
                openStreamingProviderMovieList = openStreamingProviderMovieList,
                openStreamingProvideShowList = openStreamingProvideShowList,
                onNavigateToProfileSelection = onNavigateToProfileSelection
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
    openStreamingProviderMovieList: (streamingProvider: StreamingProvider) -> Unit,
    openStreamingProvideShowList: (streamingProvider: StreamingProvider) -> Unit,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openTvShowDetailsScreen: (tvShowId: String) -> Unit,
    openVideoPlayer: (movieId: String) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    navController: NavHostController = rememberNavController(),
    onLogOutClick: () -> Unit,
    onNavigateToProfileSelection: () -> Unit = {}
) {
    val navGraph = remember(
        openMovieDetailsScreen,
        openVideoPlayer,
        openTvShowDetailsScreen,
        openCategoryMovieList,
        setSelectedMovie,
        setSelectedTvShow,
        openStreamingProviderMovieList,
        openStreamingProvideShowList,
        onLogOutClick
    ) {
        navController.createGraph(startDestination = Screens.Home()) {
            composable(Screens.Profile()) {
                ProfileScreen(
                    logOutOnClick = onLogOutClick,
                    onNavigateToProfileSelection = onNavigateToProfileSelection
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
                    setSelectedMovie = setSelectedMovie,
                    onStreamingProviderClick = openStreamingProviderMovieList,
                    navController = navController
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
                    setSelectedMovie = setSelectedMovie,
                    onStreamingProviderClick = openStreamingProviderMovieList,
                    navController = navController
                )
            }

            composable(Screens.Shows()) {
                TVShowScreen(
                    onTVShowClick = { show -> openTvShowDetailsScreen(show.id.toString()) },
                    goToVideoPlayer = { selectedMovie ->
                        openVideoPlayer(selectedMovie.id.toString())
                    },
                    setSelectedTvShow = setSelectedTvShow,
                    onStreamingProviderClick = openStreamingProvideShowList
                )
            }

            composable(Screens.Categories()) {
                CategoriesScreen(
                    onCategoryClick = openCategoryMovieList,
                )
            }
            composable(Screens.Search()) {
                SearchScreen(
                    onMovieClick = { movie -> openMovieDetailsScreen(movie.id.toString()) },
                    onScroll = { },
                    onShowClick = { show -> openTvShowDetailsScreen(show.id.toString()) }
                )
            }
        }
    }
    NavHost(navController, navGraph)
}

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun DashboardScreenNewPreview() {
    DashboardScreen(
        openCategoryMovieList = { },
        openMovieDetailsScreen = { },
        openTvShowDetailsScreen = { },
        openVideoPlayer = { },
        setSelectedMovie = { },
        setSelectedTvShow = { },
        onLogOutClick = { },
        onNavigateToProfileSelection = { }
    )
}