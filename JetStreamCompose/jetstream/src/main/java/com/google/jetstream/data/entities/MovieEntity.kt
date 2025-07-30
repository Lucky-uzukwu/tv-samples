package com.google.jetstream.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.jetstream.data.models.Country
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.Language
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.MoviePerson
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.Subtitle
import com.google.jetstream.data.models.Video
import com.google.jetstream.data.models.toVideo


@Entity(tableName = "movie")
data class MovieEntity(
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
    val video: VideoEntity?,
    val moviePeople: List<MoviePerson>,
    val genres: List<Genre>,
    val countries: List<Country>,
    val languages: List<Language>,
    val streamingProviders: List<StreamingProvider>,
    val catalogs: List<Any>,
    @ColumnInfo(name = "page")
    var page: Int? = null,
)


fun MovieEntity.toMovieNew(): MovieNew = MovieNew(
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
    video = video?.toVideo(),
    moviePeople = moviePeople,
    genres = genres,
    countries = countries,
    languages = languages,
    streamingProviders = streamingProviders,
    catalogs = catalogs,
    backdropImageUrl = "TODO()",
    posterImageUrl = "TODO()"
)


fun Video.toVideoEntity(): VideoEntity = VideoEntity(
    id = id,
    hlsPlaylistUrl = hlsPlaylistUrl,
    subtitles = subtitles
)

data class VideoEntity(
    val id: Int,
    val hlsPlaylistUrl: String,
    val subtitles: List<Subtitle>
)

