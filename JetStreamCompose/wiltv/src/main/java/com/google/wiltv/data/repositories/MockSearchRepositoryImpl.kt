package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.ContentType
import com.google.wiltv.data.network.TvChannel
import com.google.wiltv.data.models.SearchContent
import com.google.wiltv.data.models.UnifiedSearchResponse
import com.google.wiltv.data.repositories.mock.MockData
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError

class MockSearchRepositoryImpl : SearchRepository {
    override suspend fun searchContent(
        token: String,
        query: String,
        itemsPerPage: Int,
        page: Int,
        contentTypes: List<ContentType>?,
        genreId: Int?
    ): ApiResult<UnifiedSearchResponse, DataError.Network> {
        val mockMovies = MockData.getMovies().map { 
            SearchContent.MovieContent(it) 
        }
        val mockShows = MockData.getTvShows().map { 
            SearchContent.TvShowContent(it) 
        }
        val mockChannels = listOf(
            TvChannel(
                id = 1,
                name = "CNN",
                showInHeroSection = false,
                isAdultContent = false,
                isKidsContent = false,
                priority = 1,
                logoPath = "/cnn-logo.png",
                active = true,
                logoUrl = "https://example.com/cnn-logo.png",
                playLink = "https://example.com/cnn-stream",
                language = "English",
                genres = listOf(),
                countries = listOf()
            ),
            TvChannel(
                id = 2,
                name = "BBC",
                showInHeroSection = false,
                isAdultContent = false,
                isKidsContent = false,
                priority = 2,
                logoPath = "/bbc-logo.png",
                active = true,
                logoUrl = "https://example.com/bbc-logo.png",
                playLink = "https://example.com/bbc-stream",
                language = "English",
                genres = listOf(),
                countries = listOf()
            )
        ).map { SearchContent.TvChannelContent(it) }
        
        val allContent = (mockMovies + mockShows + mockChannels).take(itemsPerPage)
        
        return ApiResult.Success(
            UnifiedSearchResponse(
                context = "/contexts/Search",
                id = "/search",
                type = "Collection",
                totalItems = allContent.size,
                member = allContent
            )
        )
    }

    override suspend fun getSearchSuggestions(
        token: String,
        query: String,
        contentType: ContentType?
    ): ApiResult<List<String>, DataError.Network> {
        // Mock suggestions based on query
        val mockSuggestions = when {
            query.lowercase().startsWith("a") -> listOf("Avatar", "Avengers", "Alien", "Alice in Wonderland")
            query.lowercase().startsWith("b") -> listOf("Batman", "Breaking Bad", "Black Mirror", "Blade Runner")
            query.lowercase().startsWith("c") -> listOf("Casablanca", "Citizen Kane", "Crash", "Casino")
            query.lowercase().startsWith("d") -> listOf("Dune", "Dark Knight", "Django", "Deadpool")
            query.lowercase().startsWith("e") -> listOf("Eternal Sunshine", "Ex Machina", "Elf", "E.T.")
            query.lowercase().startsWith("f") -> listOf("Fight Club", "Forrest Gump", "Frozen", "Fury")
            query.lowercase().startsWith("g") -> listOf("Godfather", "Gladiator", "Goodfellas", "Gravity")
            query.lowercase().startsWith("h") -> listOf("Heat", "Her", "Home Alone", "Harry Potter")
            query.lowercase().startsWith("i") -> listOf("Inception", "Iron Man", "Inside Out", "Interstellar")
            query.lowercase().startsWith("j") -> listOf("Joker", "Jurassic Park", "John Wick", "Jaws")
            query.lowercase().startsWith("k") -> listOf("Kill Bill", "King Kong", "Kingsman", "Knives Out")
            query.lowercase().startsWith("l") -> listOf("Lion King", "Lord of the Rings", "Lost", "La La Land")
            query.lowercase().startsWith("m") -> listOf("Matrix", "Mission Impossible", "Marvel", "Mad Max")
            query.lowercase().startsWith("n") -> listOf("No Country for Old Men", "Napoleon", "Notebook", "Netflix")
            query.lowercase().startsWith("o") -> listOf("Once Upon a Time", "Ocean's Eleven", "Oppenheimer", "Oldboy")
            query.lowercase().startsWith("p") -> listOf("Pulp Fiction", "Pirates", "Parasite", "Princess")
            query.lowercase().startsWith("q") -> listOf("Quantum of Solace", "Queen's Gambit", "Quiet Place", "Quiz Show")
            query.lowercase().startsWith("r") -> listOf("Rocky", "Raiders", "Reservoir Dogs", "Roma")
            query.lowercase().startsWith("s") -> listOf("Star Wars", "Shawshank", "Spider-Man", "Stranger Things")
            query.lowercase().startsWith("t") -> listOf("Titanic", "Terminator", "Top Gun", "The Office")
            query.lowercase().startsWith("u") -> listOf("Up", "Unbreakable", "Unforgiven", "Under the Skin")
            query.lowercase().startsWith("v") -> listOf("Vertigo", "V for Vendetta", "Venom", "Vikings")
            query.lowercase().startsWith("w") -> listOf("Wonder Woman", "Wall-E", "Watchmen", "Walking Dead")
            query.lowercase().startsWith("x") -> listOf("X-Men", "X-Files", "XXX", "Xanadu")
            query.lowercase().startsWith("y") -> listOf("Yesterday", "You", "Young Sheldon", "Yellowstone")
            query.lowercase().startsWith("z") -> listOf("Zootopia", "Zodiac", "Zero Dark Thirty", "Zombieland")
            else -> listOf("Popular Movie", "Trending Show", "Action Film", "Drama Series")
        }.filter { it.lowercase().contains(query.lowercase()) }
        
        return ApiResult.Success(mockSuggestions.take(8))
    }
}