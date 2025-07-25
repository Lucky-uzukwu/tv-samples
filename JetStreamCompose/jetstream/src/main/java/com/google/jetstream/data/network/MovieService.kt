package com.google.jetstream.data.network

import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.ViewDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {

    @GET("/movies")
    suspend fun getMovies(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 15,
//        @Query("search") search: String? = null,
//        @Query("isAdultContent") isAdultContent: String? = null,
//        @Query("isKidsContent") isKidsContent: String? = null,
        @Query("showInHeroSection") showInHeroSection: Int? = null,
        @Query("genres[]") genreId: Int? = null,
        @Query("catalogs[]") catalogId: String? = null,
//        @Query("streamingProviders[]") streamingProviders: List<String>? = null,
//        @Query("languages[]") languages: List<String>? = null,
//        @Query("countries[]") countries: List<String>? = null,
//        @Query("people[]") people: List<String>? = null,
//        @Query("releaseDate") releaseDate: String? = null,
//        @QueryMap sort: Map<String, String>? = emptyMap()
    ): Response<MovieResponse>

    @GET("/movies/{id}")
    suspend fun getMovieById(
        @Header("Authorization") authToken: String,
        @Path("id") movieId: String
    ): Response<MovieNew>

}

data class MovieResponse(
    val member: List<MovieNew>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
)