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

import co.touchlab.kermit.Logger
import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.MovieResponse
import com.google.jetstream.data.network.MovieService
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

    override fun getMoviesToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): Flow<MovieResponse> = flow {
        Logger.i { "Fetching movies for hero section with token: $token" }
        val user = userRepository.getUser() ?: return@flow
        val response = movieService.getMovies(
            authToken = "Bearer $token",
            showInHeroSection = 1,
            page = page,
            itemsPerPage = itemsPerPage
        )

        if (response.isSuccessful) {
            val movies = response.body()
            Logger.i { "API Response: $movies" }
            Logger.i { "Successfully fetched ${movies?.member?.size} movies for hero section." }
            if (movies != null) {
                emit(movies)
            }
        } else {
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error: ${response.code()} - ${response.message()}. Error body: $errorBody" }
            val loginResponse = user.password?.let {
                authRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.identifier,
                    password = it
                )
            }
            when (loginResponse?.code()) {
                201 -> {
                    userRepository.saveUserToken(loginResponse.body()!!.token)
                    getMoviesToShowInHeroSection(token, page, itemsPerPage)
                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }
    }

    override fun getMoviesToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieResponse> =
        flow {
            Logger.i { "Fetching movies for catalog section with token: $token" }
            val user = userRepository.getUser() ?: return@flow
            val response = movieService.getMovies(
                authToken = "Bearer $token",
                catalogId = catalogId,
                itemsPerPage = itemsPerPage,
                page = page
            )

            if (response.isSuccessful) {
                val moviesResponse = response.body()
                Logger.i { "Successfully fetched ${moviesResponse?.member?.size} movies for catalog section out of ${moviesResponse?.totalItems}." }
                if (moviesResponse != null) {
                    emit(moviesResponse)
                }
            } else {
                // Handle HTTP error codes
                val errorBody =
                    response.errorBody()?.string() // Get error message from server if available
                Logger.e { "API Error: ${response.code()} - ${response.message()}. Error body: $errorBody" }
                val loginResponse = user.password?.let {
                    authRepository.login(
                        deviceMacAddress = user.deviceMacAddress,
                        clientIp = user.clientIp,
                        deviceName = user.deviceName,
                        identifier = user.identifier,
                        password = it
                    )
                }
                when (loginResponse?.code()) {
                    201 -> {
                        userRepository.saveUserToken(loginResponse.body()!!.token)
                        getMoviesToShowInCatalogSection(token, catalogId, itemsPerPage, page)
                    }
                }

                Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
            }
        }

    override fun getMoviesToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieResponse> = flow {
        val user = userRepository.getUser() ?: return@flow
        val response = movieService.getMovies(
            authToken = "Bearer $token",
            genreId = genreId,
            itemsPerPage = itemsPerPage,
            page = page
        )

        if (response.isSuccessful) {
            val moviesResponse = response.body()
            Logger.i { "Successfully fetched ${moviesResponse?.member?.size} movies for genre section out of ${moviesResponse?.totalItems}." }
            if (moviesResponse != null) {
                emit(moviesResponse)
            }
        } else {
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error: ${response.code()} - ${response.message()}. Error body: $errorBody" }
            val loginResponse = user.password?.let {
                authRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.identifier,
                    password = it
                )
            }
            when (loginResponse?.code()) {
                201 -> {
                    userRepository.saveUserToken(loginResponse.body()!!.token)
                    getMoviesToShowInGenreSection(token, genreId, itemsPerPage, page)
                }

                else -> {
                    Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                    // todo navigate to login

                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }
    }

    override fun getMovieDetailsNew(
        token: String,
        movieId: String
    ): Flow<MovieNew> = flow {
        Logger.i { "Fetching movie details for genre section with token: $token" }
        val user = userRepository.getUser() ?: return@flow
        val response = movieService.getMovieById(
            authToken = "Bearer $token",
            movieId = movieId
        )

        if (response.isSuccessful) {
            val movieData = response.body()
            Logger.i { "Successfully fetched ${movieData?.id}." }
            if (movieData != null) {
                emit(movieData)
            }
        } else {
            // Handle HTTP error codes
            val errorBody =
                response.errorBody()?.string() // Get error message from server if available
            Logger.e { "API Error: ${response.code()} - ${response.message()}. Error body: $errorBody" }
            val loginResponse = user.password?.let {
                authRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.identifier,
                    password = it
                )
            }
            when (loginResponse?.code()) {
                201 -> {
                    userRepository.saveUserToken(loginResponse.body()!!.token)
                    getMovieDetailsNew(token, movieId)
                }

                else -> {
                    Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
                    // todo navigate to login

                }
            }

            Logger.e { "Unexpected HTTP error: ${loginResponse?.code()}" }
        }
    }


}
