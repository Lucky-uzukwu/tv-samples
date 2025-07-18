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

package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.entities.MovieCategoryList
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.MovieResponse
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMovieCategories(): Flow<MovieCategoryList>
    suspend fun getMovieCategoryDetails(categoryId: String): MovieCategoryDetails
    fun getMoviesToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int,
    ): Flow<MovieResponse>

    fun getMoviesToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieResponse>

    fun getMoviesToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieResponse>

    fun getMovieDetailsNew(
        token: String,
        movieId: String
    ): Flow<MovieNew>

}
