package com.google.jetstream.data.models

import com.google.jetstream.data.entities.MovieEntity
import com.google.jetstream.data.entities.VideoEntity
import com.google.jetstream.data.entities.toVideoEntity

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
    val catalogs: List<Any>,
)

fun MovieNew.toMovieEntity(): MovieEntity = MovieEntity(
    id = id,
    title = title,
    tagLine = tagLine,
    plot = plot,
    releaseDate = releaseDate,
    duration = duration,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    backdropImagePath = backdropImagePath,
    posterImagePath = posterImagePath,
    youtubeTrailerUrl = youtubeTrailerUrl,
    contentRating = contentRating,
    isAdultContent = isAdultContent,
    isKidsContent = isKidsContent,
    views = views,
    active = active,
    showInHeroSection = showInHeroSection,
    tvShowSeasonId = tvShowSeasonId,
    tvShowSeasonPriority = tvShowSeasonPriority,
    moviePeopleCount = moviePeopleCount,
    video = video?.toVideoEntity(),
    moviePeople = moviePeople,
    genres = genres,
    countries = countries,
    languages = languages,
    streamingProviders = streamingProviders,
    catalogs = catalogs
)


data class TvShow(
    val id: Int,
    val title: String?,
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
    val priority: Any?,
    val active: Boolean,
    val showInHeroSection: Boolean,
    val tvShowPeopleCount: Int?,
    val seasonsCount: Int?,
    val tvShowPeople: List<TvShowPerson>?,
    val seasons: List<Season>?,
    val genres: List<Genre>?,
    val countries: List<Country>?,
    val languages: List<Language>?,
    val streamingProviders: List<StreamingProvider>?,
    val catalogs: List<Any>?
)

data class Season(
    val id: Int,
    val tvShowId: Int,
    val number: Int?,
    val tagLine: String?,
    val plot: String?,
    val releaseDate: String?,
    val imdbRating: Double?,
    val imdbVotes: String?,
    val backdropImagePath: String?,
    val posterImagePath: String?,
    val youtubeTrailerUrl: String?,
    val contentRating: String?,
    val views: Int?,
    val priority: Int?,
    val active: Boolean,
    val episodesCount: Int?,
    val episodes: List<Episode>?
)

data class Episode(
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
    val video: Video?,
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

fun VideoEntity.toVideo(): Video = Video(
    id = id,
    hlsPlaylistUrl = hlsPlaylistUrl,
    subtitles = subtitles
)


data class Subtitle(
    val id: Int,
    val language: Language
)

data class Language(
    val id: Int,
    val iso6391: String,
    val englishName: String,
    val name: String?
)

data class ViewDetails(
    val id: String,         // Corresponds to "@id"
    val type: String,       // Corresponds to "@type"
    val first: String,
    val last: String,
    val next: String?       // 'next' might not always be present (e.g., on the last page)
)