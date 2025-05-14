package com.google.jetstream.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface MovieService {

    @GET("api/movies")
    suspend fun getMovies(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 30,
        @Query("search") search: String? = null,
        @Query("is_adult_content") isAdultContent: String? = null,
        @Query("is_kids_content") isKidsContent: String? = null,
        @Query("show_in_hero_section") showInHeroSection: Int? = null,
        @Query("genres[]") genres: List<String>? = null,
        @Query("catalogs[]") catalogs: List<String>? = null,
        @Query("streaming_providers[]") streamingProviders: List<String>? = null,
        @Query("languages[]") languages: List<String>? = null,
        @Query("countries[]") countries: List<String>? = null,
        @Query("people[]") people: List<String>? = null,
        @Query("releaseDate") releaseDate: String? = null,
        @QueryMap sort: Map<String, String>? = null
    ): Response<MovieResponse>

    @GET("api/movies/{movieId}")
    suspend fun getMovieById(
        @Header("Authorization") authToken: String,
        @Path("movieId") movieId: String
    ): Response<Movie>

}

data class MovieResponse(
    val items: List<Movie>,
    val totalItems: Int? = null,
    val page: Int? = null,
    val itemsPerPage: Int? = null
)

data class Movie(
    val id: Int,
    val title: String,
    val tagLine: String?,
    val plot: String?,
    val releaseDate: String?,
    val duration: Int?,
    val imdbRating: String?,
    val imdbVotes: Int?,
    val backdropImagePath: String?,
    val posterImagePath: String?,
    val youtubeTrailerUrl: String?,
    val contentRating: String?,
    val isAdultContent: Boolean,
    val isKidsContent: Boolean,
    val views: Int?,
    val active: Boolean,
    val showInHeroSection: Boolean,
    val tvShowSeasonId: Int?,
    val tvShowSeasonPriority: Int?,
    val moviePeopleCount: Int?,
    val video: String?,
    val moviePeople: List<MoviePerson>,
    val genres: List<Genre>,
    val countries: List<Country>,
    val languages: List<Language>,
    val streamingProviders: List<StreamingProvider>,
    val catalogs: List<Any> // Empty list in JSON, using Any for flexibility
)

data class MoviePerson(
    val id: Int,
    val character: String?,
    val personType: PersonType,
    val person: Person
)

data class PersonType(
    val id: Int,
    val name: String
)

data class Person(
    val id: Int,
    val name: String,
    val profilePath: String?,
    val birthday: String?,
    val deathday: String?,
    val imdbId: String?,
    val knownForDepartment: String?,
    val placeOfBirth: String?,
    val biography: String?,
    val popularity: String?,
    val isAdult: Boolean
)

data class Genre(
    val id: Int,
    val name: String,
    val isMovieGenre: Boolean,
    val isTvShowGenre: Boolean,
    val isAdultGenre: Boolean,
    val isTvChannelGenre: Boolean,
    val active: Boolean
)

data class Country(
    val id: Int,
    val iso31661: String,
    val englishName: String
)

data class Language(
    val id: Int,
    val iso6391: String,
    val englishName: String,
    val name: String?,
    val pivot: Pivot
)

data class Pivot(
    val movieId: Int,
    val languageId: Int,
    val isSpoken: Int,
    val isOriginalLanguage: Int
)

data class StreamingProvider(
    val id: Int,
    val name: String,
    val logoPath: String?
)