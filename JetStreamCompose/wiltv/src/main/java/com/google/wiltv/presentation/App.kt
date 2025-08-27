package com.google.wiltv.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Stable
import java.net.URLEncoder
import com.google.wiltv.presentation.screens.auth.AuthScreen
import com.google.wiltv.presentation.screens.profileselection.ProfileSelectionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.models.TvShow
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.presentation.screens.Screens
import com.google.wiltv.presentation.screens.allchannels.AllChannelsGridScreen
import com.google.wiltv.presentation.screens.categories.CategoryMovieListScreen
import com.google.wiltv.presentation.screens.dashboard.DashboardScreen
import com.google.wiltv.presentation.screens.genre.tvchannels.GenreTvChannelsListScreen
import com.google.wiltv.presentation.screens.moviedetails.MovieDetailsScreen
import com.google.wiltv.presentation.screens.streamingprovider.movie.StreamingProviderMoviesListScreen
import com.google.wiltv.presentation.screens.streamingprovider.show.StreamingProviderShowsListScreen
import com.google.wiltv.presentation.screens.tvchannels.TvChannelScreenViewModel
import com.google.wiltv.presentation.screens.tvshowsdetails.TvShowDetailsScreen
import com.google.wiltv.presentation.screens.videoPlayer.VideoPlayerScreen
import com.google.wiltv.state.UserStateHolder
import androidx.paging.compose.collectAsLazyPagingItems

@Stable
private data class DashboardCallbacks(
    val openCategoryMovieList: (String) -> Unit,
    val openGenreTvChannelsList: (com.google.wiltv.data.models.Genre) -> Unit,
    val openAllChannels: () -> Unit,
    val openStreamingProviderMovieList: (com.google.wiltv.data.models.StreamingProvider) -> Unit,
    val openStreamingProvideShowList: (com.google.wiltv.data.models.StreamingProvider) -> Unit,
    val openTvShowDetailsScreen: (String) -> Unit,
    val openMovieDetailsScreen: (String) -> Unit,
    val openVideoPlayer: (String, String?) -> Unit,
    val setSelectedMovie: (MovieNew) -> Unit,
    val setSelectedTvShow: (TvShow) -> Unit,
    val onLogOutClick: () -> Unit,
    val onNavigateToProfileSelection: () -> Unit
)

@Composable
fun App(
    userStateHolder: UserStateHolder = hiltViewModel(),
    onBackPressed: () -> Unit
) {

    val navController = rememberNavController()
    var isComingBackFromDifferentScreen by remember { mutableStateOf(false) }
    val userState by userStateHolder.userState.collectAsState()
    val startDestination =
        if (userState.user?.token !== null) {
            // If authenticated, go directly to profile selection
            Screens.ProfileSelection()
        } else {
            // If not authenticated, start with auth screen
            Screens.AuthScreen()
        }

    val selectedMovie = remember { mutableStateOf<MovieNew?>(null) }
    val selectedTvShow = remember { mutableStateOf<TvShow?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        builder = {
            composable(route = Screens.AuthScreen()) {
                AuthScreen(
                    onNavigateToDashboard = {
                        navController.navigate(Screens.ProfileSelection())
                    },
                )
            }
            composable(route = Screens.ProfileSelection()) {
                ProfileSelectionScreen(
                    onProfileSelected = { profile ->
                        navController.navigate(Screens.Dashboard())
                    },
                    onManageProfiles = {
                        // TODO: Navigate to profile management
                    },
                    onLogout = {
                        userStateHolder.clearUser()
                        navController.navigate(Screens.AuthScreen())
                    }
                )
            }
            composable(
                route = Screens.CategoryMovieList(),
                arguments = listOf(
                    navArgument(CategoryMovieListScreen.CategoryIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                CategoryMovieListScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    },
                    onMovieSelected = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        )
                    }
                )
            }
            composable(
                route = Screens.StreamingProviderMoviesList(),
                arguments = listOf(
                    navArgument(StreamingProviderMoviesListScreen.StreamingProviderIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                StreamingProviderMoviesListScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onMovieSelected = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        )
                    }
                )
            }
            composable(
                route = Screens.GenreTvChannelsList(),
                arguments = listOf(
                    navArgument(GenreTvChannelsListScreen.GenreIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                GenreTvChannelsListScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onChannelSelected = { channel ->
                        // Check if this is a TV channel URL, encode it
                        val encodedUrl = URLEncoder.encode(channel.playLink, "UTF-8")
                        navController.navigate(Screens.VideoPlayer.withArgs(encodedUrl))
                    }
                )
            }
            composable(route = Screens.AllChannels()) {
                val tvChannelScreenViewModel: TvChannelScreenViewModel = hiltViewModel()
                AllChannelsGridScreen(
                    allChannels = tvChannelScreenViewModel.allChannels.collectAsLazyPagingItems(),
                    onChannelClick = { channel ->
                        // Check if this is a TV channel URL, encode it
                        val encodedUrl = URLEncoder.encode(channel.playLink, "UTF-8")
                        navController.navigate(Screens.VideoPlayer.withArgs(encodedUrl))
                    }
                )
            }
            composable(
                route = Screens.StreamingProviderShowsList(),
                arguments = listOf(
                    navArgument(StreamingProviderShowsListScreen.StreamingProviderIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                StreamingProviderShowsListScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    },
                    onShowSelected = { show ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(show.id)
                        )
                    }
                )
            }
            composable(
                route = Screens.MovieDetails(),
                arguments = listOf(
                    navArgument(MovieDetailsScreen.MovieIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                MovieDetailsScreen(
                    openVideoPlayer = { movieId ->
                        navController.navigate(Screens.VideoPlayer.withArgs(movieId))
                    },
                    refreshScreenWithNewMovie = { movie ->
                        navController.navigate(
                            Screens.MovieDetails.withArgs(movie.id)
                        ) {
                            popUpTo(Screens.MovieDetails()) {
                                inclusive = true
                            }
                        }
                    },
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    }
                )
            }
            composable(
                route = Screens.TvShowDetails(),
                arguments = listOf(
                    navArgument(TvShowDetailsScreen.TvShowIdBundleKey) {
                        type = NavType.StringType
                    }
                )
            ) {
                TvShowDetailsScreen(
                    openVideoPlayer = { tvShowId ->
                        navController.navigate(Screens.VideoPlayer.withArgs(tvShowId))
                    },
                    onNewTvShowSelected = { tvShow ->
                        navController.navigate(
                            Screens.TvShowDetails.withArgs(tvShow.id)
                        ) {
                            popUpTo(Screens.TvShowDetails()) {
                                inclusive = true
                            }
                        }
                    },
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    }
                )
            }
            composable(route = Screens.Dashboard()) {
                val dashboardCallbacks =
                    remember(navController, selectedMovie, selectedTvShow, userStateHolder) {
                        DashboardCallbacks(
                            openCategoryMovieList = { categoryId ->
                                navController.navigate(
                                    Screens.CategoryMovieList.withArgs(categoryId)
                                )
                            },
                            openGenreTvChannelsList = { genre ->
                                navController.navigate(
                                    Screens.GenreTvChannelsList.withArgs("${genre.id}-${genre.name}")
                                )
                            },
                            openAllChannels = {
                                navController.navigate(Screens.AllChannels())
                            },
                            openStreamingProviderMovieList = { streamingProvider ->
                                navController.navigate(
                                    Screens.StreamingProviderMoviesList.withArgs("${streamingProvider.id}-${streamingProvider.name}")
                                )
                            },
                            openStreamingProvideShowList = { streamingProvider ->
                                navController.navigate(
                                    Screens.StreamingProviderShowsList.withArgs("${streamingProvider.id}-${streamingProvider.name}")
                                )
                            },
                            openTvShowDetailsScreen = { tvShowId ->
                                navController.navigate(
                                    Screens.TvShowDetails.withArgs(tvShowId)
                                )
                            },
                            openMovieDetailsScreen = { movieId ->
                                navController.navigate(
                                    Screens.MovieDetails.withArgs(movieId)
                                )
                            },
                            openVideoPlayer = { contentId, title ->
                                // Check if this is a TV channel URL or a movie/show ID
                                if (contentId.startsWith("http")) {
                                    // This is a TV channel URL, encode it
                                    val encodedUrl = URLEncoder.encode(contentId, "UTF-8")
                                    navController.navigate(Screens.VideoPlayer.withArgs(encodedUrl))
                                } else {
                                    // This is a movie/show ID, use as-is
                                    navController.navigate(Screens.VideoPlayer.withArgs(contentId))
                                }
                            },
                            setSelectedMovie = {
                                selectedMovie.value = it
                                selectedTvShow.value = null
                            },
                            setSelectedTvShow = {
                                selectedTvShow.value = it
                                selectedMovie.value = null
                            },
                            onLogOutClick = {
                                userStateHolder.clearUser()
                                navController.navigate(Screens.AuthScreen())
                            },
                            onNavigateToProfileSelection = {
                                navController.navigate(Screens.ProfileSelection())
                            }
                        )
                    }

                // Handle back button to exit app when at dashboard root
                BackHandler(onBack = onBackPressed)

                DashboardScreen(
                    openCategoryMovieList = dashboardCallbacks.openCategoryMovieList,
                    openGenreTvChannelsList = dashboardCallbacks.openGenreTvChannelsList,
                    openAllChannels = dashboardCallbacks.openAllChannels,
                    openStreamingProviderMovieList = dashboardCallbacks.openStreamingProviderMovieList,
                    openStreamingProvideShowList = dashboardCallbacks.openStreamingProvideShowList,
                    openTvShowDetailsScreen = dashboardCallbacks.openTvShowDetailsScreen,
                    openMovieDetailsScreen = dashboardCallbacks.openMovieDetailsScreen,
                    openVideoPlayer = dashboardCallbacks.openVideoPlayer,
                    setSelectedMovie = dashboardCallbacks.setSelectedMovie,
                    setSelectedTvShow = dashboardCallbacks.setSelectedTvShow,
                    onLogOutClick = dashboardCallbacks.onLogOutClick,
                    onNavigateToProfileSelection = dashboardCallbacks.onNavigateToProfileSelection
                )
            }
            composable(route = Screens.VideoPlayer()) {
                VideoPlayerScreen(
                    onBackPressed = {
                        if (navController.navigateUp()) {
                            isComingBackFromDifferentScreen = true
                        }
                    }
                )
            }
        }
    )
}
