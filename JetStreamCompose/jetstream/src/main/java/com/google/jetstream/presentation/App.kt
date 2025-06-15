/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetstream.presentation

import AuthScreen
import LoginScreen
import RegisterScreen
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
import co.touchlab.kermit.Logger
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.categories.CategoryMovieListScreen
import com.google.jetstream.presentation.screens.dashboard.DashboardScreen
import com.google.jetstream.presentation.screens.moviedetails.MovieDetailsScreen
import com.google.jetstream.presentation.screens.tvshowsdetails.TvShowDetailsScreen
import com.google.jetstream.presentation.screens.videoPlayer.VideoPlayerScreen
import com.google.jetstream.state.UserStateHolder

@Composable
fun App(
    userStateHolder: UserStateHolder = hiltViewModel(),
    onBackPressed: () -> Unit
) {

    val navController = rememberNavController()
    var isComingBackFromDifferentScreen by remember { mutableStateOf(false) }
    val userState by userStateHolder.userState.collectAsState()
    Logger.i { "user token: ${userState.user?.token}" }
    val startDestination =
        if (userState.user?.token !== null) Screens.Dashboard() else Screens.AuthScreen()

    val selectedMovie = remember { mutableStateOf<MovieNew?>(null) }
    val selectedTvShow = remember { mutableStateOf<TvShow?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        builder = {
            composable(route = Screens.AuthScreen()) {
                AuthScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screens.Login())
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screens.Register())
                    }
                )
            }
            composable(route = Screens.Login()) { backStackEntry ->
                LoginScreen(
                    onSubmitSuccess = {
                        navController.navigate(Screens.Dashboard())
                    }
                )
            }
            composable(route = Screens.Register()) { backStackEntry ->
                RegisterScreen(
                    onSubmitSuccess = {
                        navController.navigate(Screens.Dashboard())
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
                    selectedMovie = selectedMovie.value,
                    setSelectedMovie = {
                        selectedMovie.value = it
                        selectedTvShow.value = null
                    },
                    selectedTvShow = selectedTvShow.value,
                    setSelectedTvShow = {
                        selectedTvShow.value = it
                        selectedMovie.value = null
                    },
                    onLogOutClick = {
                        userStateHolder.clearUser()
                        navController.navigate(Screens.AuthScreen())
                    },
                    clearFilmSelection = {
                        selectedMovie.value = null
                        selectedTvShow.value = null
                    }
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
