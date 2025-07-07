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
import com.google.jetstream.data.entities.MovieDetails
import com.google.jetstream.data.entities.MovieList
import com.google.jetstream.data.entities.MovieReviewsAndRatings
import com.google.jetstream.data.entities.ThumbnailType
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.MovieResponse
import com.google.jetstream.data.network.MovieService
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.DefaultCount
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.DefaultRating
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.FreshTomatoes
import com.google.jetstream.data.util.StringConstants.Movie.Reviewer.ReviewerName
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val movieDataSource: MovieDataSource,
    private val tvDataSource: TvDataSource,
    private val movieCastDataSource: MovieCastDataSource,
    private val movieCategoryDataSource: MovieCategoryDataSource,
    private val movieService: MovieService,
    private val customerRepository: CustomerRepository,
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

    override suspend fun getMovieDetails(movieId: String): MovieDetails {
        val movieList = movieDataSource.getMovieList()
        val movie = movieList.find { it.id == movieId } ?: movieList.first()
        val similarMovieList = movieList.subList(1, 4)
        val castList = movieCastDataSource.getMovieCastList()

        return MovieDetails(
            id = movie.id,
            videoUri = movie.videoUri,
            subtitleUri = movie.subtitleUri,
            posterUri = movie.posterUri,
            name = movie.name,
            description = movie.description,
            pgRating = "PG-13",
            releaseDate = "2021 (US)",
            categories = listOf("Action", "Adventure", "Fantasy", "Comedy"),
            duration = "1h 59m",
            director = "Larry Page",
            screenplay = "Sundai Pichai",
            music = "Sergey Brin",
            castAndCrew = castList,
            status = "Released",
            originalLanguage = "English",
            budget = "$15M",
            revenue = "$40M",
            similarMovies = similarMovieList,
            reviewsAndRatings = listOf(
                MovieReviewsAndRatings(
                    reviewerName = FreshTomatoes,
                    reviewerIconUri = StringConstants.Movie.Reviewer.FreshTomatoesImageUrl,
                    reviewCount = "22",
                    reviewRating = "89%"
                ),
                MovieReviewsAndRatings(
                    reviewerName = ReviewerName,
                    reviewerIconUri = StringConstants.Movie.Reviewer.ImageUrl,
                    reviewCount = DefaultCount,
                    reviewRating = DefaultRating
                ),
            ),
        )
    }

    override suspend fun searchMovies(query: String): MovieList {
        return movieDataSource.getMovieList().filter {
            it.name.contains(other = query, ignoreCase = true)
        }
    }

    override fun getMoviesWithLongThumbnail() = flow {
        val list = movieDataSource.getMovieList(ThumbnailType.Long)
        emit(list)
    }

    override fun getPopularFilmsThisWeek(): Flow<MovieList> = flow {
        val list = movieDataSource.getPopularFilmThisWeek()
        emit(list)
    }

    override fun getTVShows(): Flow<MovieList> = flow {
        val list = tvDataSource.getTvShowList()
        emit(list)
    }

    override fun getBingeWatchDramas(): Flow<MovieList> = flow {
        val list = tvDataSource.getBingeWatchDramaList()
        emit(list)
    }

    override fun getFavouriteMovies(): Flow<MovieList> = flow {
        val list = movieDataSource.getFavoriteMovieList()
        emit(list)
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
                customerRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.accessCode,
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
                    customerRepository.login(
                        deviceMacAddress = user.deviceMacAddress,
                        clientIp = user.clientIp,
                        deviceName = user.deviceName,
                        identifier = user.accessCode,
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
                customerRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.accessCode,
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
                customerRepository.login(
                    deviceMacAddress = user.deviceMacAddress,
                    clientIp = user.clientIp,
                    deviceName = user.deviceName,
                    identifier = user.accessCode,
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
