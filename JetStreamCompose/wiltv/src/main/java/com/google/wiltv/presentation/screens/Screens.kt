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

package com.google.wiltv.presentation.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.wiltv.presentation.screens.categories.CategoryMovieListScreen
import com.google.wiltv.presentation.screens.moviedetails.MovieDetailsScreen
import com.google.wiltv.presentation.screens.streamingprovider.movie.StreamingProviderMoviesListScreen
import com.google.wiltv.presentation.screens.streamingprovider.show.StreamingProviderShowsListScreen
import com.google.wiltv.presentation.screens.tvshowsdetails.TvShowDetailsScreen
import com.google.wiltv.presentation.screens.videoPlayer.VideoPlayerScreen

enum class Screens(
    private val args: List<String>? = null,
    val isTabItem: Boolean = false,
    val tabIcon: ImageVector? = null
) {
    AuthScreen,
    ProfileSelection,
    Login,
    Register,
    Search(isTabItem = true, tabIcon = Icons.Default.Search),
    Home(isTabItem = true, tabIcon = Icons.Default.Home),
    Movies(isTabItem = true, tabIcon = Icons.Default.Movie),
    Shows(isTabItem = true, tabIcon = Icons.Default.Tv),
    TvChannels(isTabItem = true, tabIcon = Icons.Default.LiveTv),
    Categories(isTabItem = true, tabIcon = Icons.Default.Category),
    Profile(isTabItem = true, tabIcon = Icons.Default.Person),

    //    Favourites(isTabItem = true),
    CategoryMovieList(listOf(CategoryMovieListScreen.CategoryIdBundleKey)),
    StreamingProviderMoviesList(listOf(StreamingProviderMoviesListScreen.StreamingProviderIdBundleKey)),
    StreamingProviderShowsList(listOf(StreamingProviderShowsListScreen.StreamingProviderIdBundleKey)),
    MovieDetails(listOf(MovieDetailsScreen.MovieIdBundleKey)),
    TvShowDetails(listOf(TvShowDetailsScreen.TvShowIdBundleKey)),
    Dashboard,
    VideoPlayer(listOf(VideoPlayerScreen.MovieIdBundleKey));

    operator fun invoke(): String {
        val argList = StringBuilder()
        args?.let { nnArgs ->
            nnArgs.forEach { arg -> argList.append("/{$arg}") }
        }
        return name + argList
    }

    fun withArgs(vararg args: Any): String {
        val destination = StringBuilder()
        args.forEach { arg -> destination.append("/$arg") }
        return name + destination
    }
}
