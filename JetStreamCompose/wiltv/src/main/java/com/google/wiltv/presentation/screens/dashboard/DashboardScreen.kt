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
import com.google.wiltv.data.models.Genre
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.StreamingProvider
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.screens.Screens
import com.google.wiltv.presentation.screens.watchlist.WatchlistScreen
import com.google.wiltv.presentation.screens.dashboard.navigation.drawer.HomeDrawer
import com.google.wiltv.presentation.screens.home.HomeScreen
import com.google.wiltv.presentation.screens.movies.MoviesScreen
import com.google.wiltv.presentation.screens.profile.ProfileScreen
import com.google.wiltv.presentation.screens.search.SearchScreen
import com.google.wiltv.presentation.screens.tvchannels.TvChannelScreen
import com.google.wiltv.presentation.screens.tvshows.TVShowScreen
import com.google.wiltv.presentation.screens.sports.SportsScreen
import com.google.wiltv.presentation.utils.Padding
import java.net.URLEncoder

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
    openGenreTvChannelsList: (genre: Genre) -> Unit = {},
    openAllChannels: () -> Unit = {},
    openMovieDetailsScreen: (movieId: String) -> Unit = {},
    openTvShowDetailsScreen: (tvShowId: String) -> Unit = {},
    openSportGameDetails: (gameData: String) -> Unit = {},
    openVideoPlayer: (contentId: String, title: String?) -> Unit = { _, _ -> },
    openStreamingProviderMovieList: (streamingProvider: StreamingProvider) -> Unit = {},
    openStreamingProvideShowList: (streamingProvider: StreamingProvider) -> Unit = {},
    setSelectedMovie: (movie: MovieNew) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    onLogOutClick: () -> Unit,
    onNavigateToProfileSelection: () -> Unit = {},
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val selectedProfile = dashboardViewModel.selectedProfile.collectAsStateWithLifecycle().value

    HomeDrawer(
        navController = navController,
        selectedProfile = selectedProfile,
        content = {
            Body(
                openCategoryMovieList = openCategoryMovieList,
                openGenreTvChannelsList = openGenreTvChannelsList,
                openAllChannels = openAllChannels,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openSportGameDetails = openSportGameDetails,
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
                onNavigateToProfileSelection = onNavigateToProfileSelection,
                onNavigateToScreen = { screen -> 
                    // Navigate and let drawer know about the change
                    navController.navigate(screen())
                }
            )
        },
        onScreenSelected = { screen ->
            navController.navigate(screen())
        }
    )
}

@Composable
private fun Body(
    modifier: Modifier = Modifier,
    openCategoryMovieList: (categoryId: String) -> Unit,
    openGenreTvChannelsList: (genre: Genre) -> Unit,
    openAllChannels: () -> Unit,
    openStreamingProviderMovieList: (streamingProvider: StreamingProvider) -> Unit,
    openStreamingProvideShowList: (streamingProvider: StreamingProvider) -> Unit,
    openMovieDetailsScreen: (movieId: String) -> Unit,
    openTvShowDetailsScreen: (tvShowId: String) -> Unit,
    openSportGameDetails: (gameData: String) -> Unit,
    openVideoPlayer: (contentId: String, title: String?) -> Unit,
    setSelectedMovie: (movie: MovieNew) -> Unit,
    setSelectedTvShow: (tvShow: TvShow) -> Unit,
    navController: NavHostController = rememberNavController(),
    onLogOutClick: () -> Unit,
    onNavigateToProfileSelection: () -> Unit = {},
    onNavigateToScreen: (screen: Screens) -> Unit
) {
    val navGraph = remember(
        openMovieDetailsScreen,
        openVideoPlayer,
        openTvShowDetailsScreen,
        openCategoryMovieList,
        openGenreTvChannelsList,
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
                    onNavigateToProfileSelection = onNavigateToProfileSelection,
                    onMovieClick = { selectedMovie ->
                        openMovieDetailsScreen(selectedMovie.id.toString())
                    },
                    onTvShowClick = { selectedTvShow ->
                        openTvShowDetailsScreen(selectedTvShow.id.toString())
                    }
                )
            }
            composable(Screens.Home()) {
                HomeScreen(
                    onMovieClick = { selectedMovie ->
                        openMovieDetailsScreen(selectedMovie.id.toString())
                    },
                    goToVideoPlayer = { selectedMovie ->
                        openVideoPlayer(selectedMovie.id.toString(), selectedMovie.title)
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
                        openVideoPlayer(selectedMovie.id.toString(), selectedMovie.title)
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
                        openVideoPlayer(selectedMovie.id.toString(), selectedMovie.title)
                    },
                    setSelectedTvShow = setSelectedTvShow,
                    onStreamingProviderClick = openStreamingProvideShowList
                )
            }

            composable(Screens.Sports()) {
                SportsScreen(
                    onGameClick = { game ->
                        val gameJson = kotlinx.serialization.json.Json.encodeToString(
                            com.google.wiltv.data.entities.CompetitionGame.serializer(),
                            game
                        )
                        val encodedGameData = URLEncoder.encode(gameJson, "UTF-8")
                        openSportGameDetails(encodedGameData)
                    },
                    navController = navController
                )
            }

            composable(Screens.TvChannels()) {
                TvChannelScreen(
                    goToVideoPlayer = { channel ->
                        openVideoPlayer(channel.playLink, channel.name)
                    },
                    onGenreClick = openGenreTvChannelsList,
                    onViewAllChannelsClick = openAllChannels
                )
            }

            composable(Screens.Watchlist()) {
                WatchlistScreen(
                    onMovieClick = { movie -> openMovieDetailsScreen(movie.id.toString()) },
                    onTvShowClick = { show -> openTvShowDetailsScreen(show.id.toString()) }
                )
            }
            composable(Screens.Search()) {
                SearchScreen(
                    onMovieClick = { movie -> openMovieDetailsScreen(movie.id.toString()) },
                    onScroll = { },
                    onShowClick = { show -> openTvShowDetailsScreen(show.id.toString()) },
                    onChannelClick = { channel -> openVideoPlayer(channel.playLink, channel.name) },
                    onGameClick = { game ->
                        game.streamingLinks.firstOrNull()?.let { link ->
                            openVideoPlayer(link, game.description)
                        }
                    },
                    onBrowseCategoriesClick = { onNavigateToScreen(Screens.Watchlist) },
                    onTrendingContentClick = { onNavigateToScreen(Screens.Home) }
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
        openVideoPlayer = { _, _ -> },
        setSelectedMovie = { },
        setSelectedTvShow = { },
        onLogOutClick = { },
        onNavigateToProfileSelection = { }
    )
}