package com.google.jetstream.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie")
data class MovieNew(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "tag_line")
    val tagLine: String?,
    @ColumnInfo(name = "plot")
    val plot: String?,
    @ColumnInfo(name = "release_date")
    val releaseDate: String?,
    @ColumnInfo(name = "duration")
    val duration: Int?,
    @ColumnInfo(name = "imdb_rating")
    val imdbRating: String?,
    @ColumnInfo(name = "imdb_votes")
    val imdbVotes: Int?,
    @ColumnInfo(name = "backdrop_image_path")
    val backdropImagePath: String?,
    @ColumnInfo(name = "poster_image_path")
    val posterImagePath: String?,
    @ColumnInfo(name = "youtube_trailer_url")
    val youtubeTrailerUrl: String?,
    @ColumnInfo(name = "content_rating")
    val contentRating: String?,
    @ColumnInfo(name = "is_adult_content")
    val isAdultContent: Boolean,
    @ColumnInfo(name = "is_kids_content")
    val isKidsContent: Boolean,
    @ColumnInfo(name = "views")
    val views: Int?,
    @ColumnInfo(name = "active")
    val active: Boolean,
    @ColumnInfo(name = "show_in_hero_section")
    val showInHeroSection: Boolean,
    @ColumnInfo(name = "tv_show_season_id")
    val tvShowSeasonId: Int?,
    @ColumnInfo(name = "tv_show_season_priority")
    val tvShowSeasonPriority: Int?,
    @ColumnInfo(name = "movie_people_count")
    val moviePeopleCount: Int?,
    @Embedded(prefix = "video_")
    val video: Video?,
    val moviePeople: List<MoviePerson>,
    val genres: List<Genre>,
    val countries: List<Country>,
    val languages: List<Language>,
    val streamingProviders: List<StreamingProvider>,
    val catalogs: List<Any>,
    @ColumnInfo(name = "page")
    var page: Int? = null,
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
    @ColumnInfo("hls_playlist_url")
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
    val name: String?
)

data class ViewDetails(
    val id: String,         // Corresponds to "@id"
    val type: String,       // Corresponds to "@type"
    val first: String,
    val last: String,
    val next: String?       // 'next' might not always be present (e.g., on the last page)
)