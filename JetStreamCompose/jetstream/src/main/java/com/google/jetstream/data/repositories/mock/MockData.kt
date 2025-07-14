package com.google.jetstream.data.repositories.mock

import com.google.jetstream.data.models.Country
import com.google.jetstream.data.models.Episode
import com.google.jetstream.data.models.Genre
import com.google.jetstream.data.models.Language
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.models.MoviePerson
import com.google.jetstream.data.models.Person
import com.google.jetstream.data.models.PersonType
import com.google.jetstream.data.models.Season
import com.google.jetstream.data.models.StreamingProvider
import com.google.jetstream.data.models.TvShow
import com.google.jetstream.data.models.Video
import com.google.jetstream.data.network.MovieSearchResponse
import com.google.jetstream.data.network.ShowSearchResponse

object MockData {

    fun getMovie() = MovieNew(
        id = 2,
        title = "The Gorge",
        tagLine = "The world's most dangerous secret lies between them.",
        plot = "Two highly trained operatives grow close from a distance after being sent to guard opposite sides of a mysterious gorge. When an evil below emerges, they must work together to survive what lies within.",
        releaseDate = "2025-02-13 00:00:00",
        duration = 127,
        imdbRating = "7.8",
        imdbVotes = 832,
        backdropImagePath = "images/movies/backdrops/09abc224-5f88-4d3f-ab34-619a08a44fc2.jpg",
        posterImagePath = "images/movies/posters/1b4f845b-922d-4f96-9e10-363a9cea0703.jpg",
        youtubeTrailerUrl = null,
        contentRating = null,
        isAdultContent = false,
        isKidsContent = false,
        views = 1272,
        active = true,
        showInHeroSection = true,
        tvShowSeasonId = null,
        tvShowSeasonPriority = null,
        moviePeopleCount = 10,
        video = Video(
            id = 28,
            hlsPlaylistUrl = "https://stage-stream.nortv.xyz/hls/2fe0482.json/master.m3u8",
            subtitles = listOf()
        ),
        moviePeople = listOf(
            MoviePerson(
                id = 11,
                character = "Levi",
                personType = PersonType(
                    id = 1,
                    name = "Acting"
                ),
                person = Person(
                    id = 11,
                    name = "Miles Teller",
                    profilePath = "images/people/5b37aef6-7aaa-4654-bd79-55c9520e4882.jpg",
                    birthday = null,
                    deathday = null,
                    imdbId = "996701",
                    knownForDepartment = "Acting",
                    placeOfBirth = null,
                    biography = null,
                    popularity = "77.211",
                    isAdult = false
                )
            )
        ),
        genres = listOf(
            Genre(
                id = 1,
                name = "Action",
                isMovieGenre = true,
                isTvShowGenre = false,
                isAdultGenre = false,
                isTvChannelGenre = false,
                active = true
            )
        ),
        countries = listOf(
            Country(
                id = 238,
                iso31661 = "US",
                englishName = "United States"
            )
        ),
        languages = listOf(
            Language(
                id = 1,
                iso6391 = "en",
                englishName = "English",
                name = null
            )
        ),
        streamingProviders = listOf(
            StreamingProvider(
                id = 1,
                name = "Apple TV Plus",
                logoPath = "images/streaming/provider/1bd81243-5ed6-4ea4-b7bf-ad40ee281517.jpg"
            )
        ),
        catalogs = listOf()
    )

    fun getTvShow() = TvShow(
        id = 26,
        title = "Ang Mutya ng Section E",
        tagLine = "",
        plot = "It follows 16 rowdy guys and a fierce young woman from Section E. Their lives become chaotic when Jay-Jay Mariano, a talented singer and former Ppop Generation member, joins the class as the “mutya” of Section E—the lowest and most unruly section in Higher Value International School.",
        releaseDate = "2025-01-03 00:00:00",
        duration = null,
        imdbRating = "8.857",
        imdbVotes = 35,
        backdropImagePath = "images/tvshows/backdrops/8b4a6139-d6c6-4b0a-9d89-2b77dc5ca48f.jpg",
        posterImagePath = "images/tvshows/posters/3967ec0a-00ea-488f-9cb2-ae4831a4eb07.jpg",
        youtubeTrailerUrl = null,
        contentRating = null,
        isAdultContent = false,
        isKidsContent = false,
        views = 0,
        priority = null,
        active = true,
        showInHeroSection = false,
        tvShowPeopleCount = 0,
        seasonsCount = 1,
        tvShowPeople = listOf(),
        seasons = listOf(
            Season(
                id = 52,
                tvShowId = 26,
                number = 1,
                tagLine = "Season 1",
                plot = "Section E is a notorious section for its delinquent all-male students. One female transferee will change their chaotic world, especially their grumpy president, who will do anything to drive her away. An adaptation of the hit Wattpad series by Eatmore2behappy.",
                releaseDate = "2025-01-03 00:00:00",
                imdbRating = 9.0,
                imdbVotes = null,
                backdropImagePath = null,
                posterImagePath = null,
                youtubeTrailerUrl = null,
                contentRating = null,
                views = 0,
                priority = 1,
                active = true,
                episodesCount = 16,
                episodes = listOf(
                    Episode(
                        id = 482,
                        title = "1",
                        tagLine = "Welcome To Section E",
                        plot = "What happens when a girl joins a class full of rowdy boys? It's the notorious section E versus the fierce new girl. All Jay-jay wants is an uneventful and trouble-free new life at her new school, but Section E's grumpy president, Keifer, has other plans.",
                        releaseDate = "2025-01-03 00:00:00",
                        duration = 52,
                        imdbRating = "10",
                        imdbVotes = 1,
                        backdropImagePath = null,
                        posterImagePath = "images/tvshows/backdrops/b2c22eb5-d8c4-468e-89f2-76b041fa0d06.jpg",
                        youtubeTrailerUrl = null,
                        contentRating = null,
                        isAdultContent = false,
                        isKidsContent = false,
                        views = null,
                        active = true,
                        showInHeroSection = false,
                        tvShowSeasonId = 52,
                        tvShowSeasonPriority = null,
                        video = null
                    )
                )
            )
        ),
        genres = listOf(),
        countries = listOf(),
        languages = listOf(),
        streamingProviders = listOf(),
        catalogs = listOf()
    )


    fun getMovieSearchResponse() = MovieSearchResponse(
        member = listOf(getMovie())
    )

    fun getTvShowSearchResponse() = ShowSearchResponse(
        member = listOf(getTvShow())
    )

}