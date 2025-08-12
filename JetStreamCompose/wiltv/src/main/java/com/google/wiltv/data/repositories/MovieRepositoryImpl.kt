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

package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.entities.MovieCategoryDetails
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.network.MovieResponse
import com.google.wiltv.data.network.MovieService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDataSource: MovieDataSource,
    private val movieCategoryDataSource: MovieCategoryDataSource,
    private val movieService: MovieService,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : MovieRepository {

    override fun getMovieCategories() = flow {
        val list = movieCategoryDataSource.getMovieCategoryList()
        emit(list)
    }

    override suspend fun getMovieCategoryDetails(categoryId: String): MovieCategoryDetails {
        val categoryList = movieCategoryDataSource.getMovieCategoryList()
        val category = categoryList.find { categoryId == it.id } ?: categoryList.first()

        val movieList = movieDataSource.getMovieList().shuffled().subList(0, 20)

        return MovieCategoryDetails(
            id = category.id,
            name = category.name,
            movies = movieList
        )
    }

    override suspend fun getMoviesToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<MovieResponse, DataError.Network> {
        Logger.i { "Fetching movies for hero section with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = movieService.getMovies(
            authToken = "Bearer $token",
            showInHeroSection = 1,
            page = page,
            itemsPerPage = itemsPerPage
        )
        return mapToResult(response)
    }

    override suspend fun getMoviesToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieResponse, DataError.Network> {
        Logger.i { "Fetching movies for catalog section with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = movieService.getMovies(
            authToken = "Bearer $token",
            catalogId = catalogId,
            itemsPerPage = itemsPerPage,
            page = page
        )
        return mapToResult(response)
    }

    override suspend fun getMoviesToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieResponse, DataError.Network> {
        Logger.i { "Fetching movies for genre section" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = movieService.getMovies(
            authToken = "Bearer $token",
            genreId = genreId,
            itemsPerPage = itemsPerPage,
            page = page
        )
        return mapToResult(response)
    }

    override suspend fun getMovieDetailsNew(
        token: String,
        movieId: String
    ): ApiResult<MovieNew, DataError.Network> {
        Logger.i { "Fetching movie details with token: $token" }
        val user = userRepository.getUser()
            ?: return ApiResult.Error(DataError.Network.LOCAL_USER_NOT_FOUND)
        val response = movieService.getMovieById(
            authToken = "Bearer $token",
            movieId = movieId
        )
        return mapToResult(response)
    }


}
