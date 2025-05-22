package com.google.jetstream.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieService {

    @GET("api/movies")
    suspend fun getMovies(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Query("page") page: Int? = 1,
        @Query("itemsPerPage") itemsPerPage: Int? = 15,
//        @Query("search") search: String? = null,
//        @Query("is_adult_content") isAdultContent: String? = null,
//        @Query("is_kids_content") isKidsContent: String? = null,
        @Query("show_in_hero_section") showInHeroSection: Int? = null,
        @Query("genres[]") genreId: Int? = null,
        @Query("catalogs[]") catalogId: String? = null,
//        @Query("streaming_providers[]") streamingProviders: List<String>? = null,
//        @Query("languages[]") languages: List<String>? = null,
//        @Query("countries[]") countries: List<String>? = null,
//        @Query("people[]") people: List<String>? = null,
//        @Query("releaseDate") releaseDate: String? = null,
//        @QueryMap sort: Map<String, String>? = emptyMap()
    ): Response<MovieResponse>

    @GET("api/movies/{id}")
    suspend fun getMovieById(
        @Header("Authorization") authToken: String,
        @Path("id") movieId: String
    ): Response<MovieNew>

}

data class MovieResponse(
    val member: List<MovieNew>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
//    val itemsPerPage: Int? = null
)

data class MovieNew(
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
    val video: Video?,
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

data class Video(
    val id: Int,
    val hlsPlaylistUrl: String,
    val subtitles: List<Subtitle>
)

data class Subtitle(
    val id: Int,
    val language: Language
)

data class Language(
    val id: Int,
    val iso6391: String,
    val englishName: String,
    val name: String? // Assuming 'name' can sometimes be null based on the JSON
)

data class ViewDetails(
    val id: String,         // Corresponds to "@id"
    val type: String,       // Corresponds to "@type"
    val first: String,
    val last: String,
    val next: String?       // 'next' might not always be present (e.g., on the last page)
)