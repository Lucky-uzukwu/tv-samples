package com.google.wiltv.data.network

import com.google.wiltv.data.models.Genre
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GenreService {

    @GET("/genres")
    suspend fun getGenres(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("isMovieGenre") isMovieGenre: Int? = null,
        @Query("isTvShowGenre") isTvShowGenre: Int? = null,
    ): Response<GenreResponse>
}

data class GenreResponse(
    val member: List<Genre>,
    val totalItems: Int? = null,
)