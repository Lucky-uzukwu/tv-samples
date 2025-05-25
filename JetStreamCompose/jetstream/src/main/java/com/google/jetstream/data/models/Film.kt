package com.google.jetstream.data.models

sealed interface Film {
    val id: Int
    val title: String
    val tagLine: String?
    val plot: String?
    val releaseDate: String?
    val duration: Int?
    val imdbRating: String?
    val imdbVotes: Int?
    val backdropImagePath: String?
    val posterImagePath: String?
    val youtubeTrailerUrl: String?
    val contentRating: String?
    val isAdultContent: Boolean
    val isKidsContent: Boolean
    val views: Int?
    val active: Boolean
    val showInHeroSection: Boolean
    val moviePeople: List<MoviePerson>
    val genres: List<Genre>
    val countries: List<Country>
    val languages: List<Language>
    val streamingProviders: List<StreamingProvider>
    val catalogs: List<Any> // Empty list in JSON, using Any for flexibility
}

data class MovieNew(
    override val id: Int,
    override val title: String,
    override val tagLine: String?,
    override val plot: String?,
    override val releaseDate: String?,
    override val duration: Int?,
    override val imdbRating: String?,
    override val imdbVotes: Int?,
    override val backdropImagePath: String?,
    override val posterImagePath: String?,
    override val youtubeTrailerUrl: String?,
    override val contentRating: String?,
    override val isAdultContent: Boolean,
    override val isKidsContent: Boolean,
    override val views: Int?,
    override val active: Boolean,
    override val showInHeroSection: Boolean,
    val tvShowSeasonId: Int?,
    val tvShowSeasonPriority: Int?,
    val moviePeopleCount: Int?,
    val video: Video?,
    override val moviePeople: List<MoviePerson>,
    override val genres: List<Genre>,
    override val countries: List<Country>,
    override val languages: List<Language>,
    override val streamingProviders: List<StreamingProvider>,
    override val catalogs: List<Any>
) : Film


data class TvShows(
    override val id: Int,
    override val title: String,
    override val tagLine: String?,
    override val plot: String?,
    override val releaseDate: String?,
    override val duration: Int?,
    override val imdbRating: String?,
    override val imdbVotes: Int?,
    override val backdropImagePath: String?,
    override val posterImagePath: String?,
    override val youtubeTrailerUrl: String?,
    override val contentRating: String?,
    override val isAdultContent: Boolean,
    override val isKidsContent: Boolean,
    override val views: Int?,
    override val active: Boolean,
    override val showInHeroSection: Boolean,
    val tvShowPeopleCount: Int?,
    val seasonsCount: Int?,
    override val genres: List<Genre>,
    override val countries: List<Country>,
    override val languages: List<Language>,
    override val streamingProviders: List<StreamingProvider>,
    override val moviePeople: List<MoviePerson>,
    override val catalogs: List<Any>,
    val tvShowPerson: List<TvShowPerson>,
    val priority: Any?
) : Film

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

data class TvShowPerson(
    val id: Int,
    val tvShowId: Int,
    val personId: Int,
    val character: String,
    val personTypeId: Int,
    val personType: PersonType,
    val person: Person
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