package com.google.wiltv.presentation

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
import com.google.wiltv.presentation.screens.Screens
import com.google.wiltv.presentation.screens.categories.CategoryMovieListScreen
import com.google.wiltv.presentation.screens.dashboard.DashboardScreen
import com.google.wiltv.presentation.screens.moviedetails.MovieDetailsScreen
import com.google.wiltv.presentation.screens.streamingprovider.movie.StreamingProviderMoviesListScreen
import com.google.wiltv.presentation.screens.streamingprovider.show.StreamingProviderShowsListScreen
import com.google.wiltv.presentation.screens.tvshowsdetails.TvShowDetailsScreen
import com.google.wiltv.presentation.screens.videoPlayer.VideoPlayerScreen
import com.google.wiltv.state.UserStateHolder

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
                DashboardScreen(
                    openCategoryMovieList = { categoryId ->
                        navController.navigate(
                            Screens.CategoryMovieList.withArgs(categoryId)
                        )
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
                    openVideoPlayer = { movieId ->
                        navController.navigate(Screens.VideoPlayer.withArgs(movieId))
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
