package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.MovieCategoryDetails
import com.google.jetstream.data.entities.MovieCategoryList
import com.google.jetstream.data.models.MovieNew
import com.google.jetstream.data.network.MovieResponse
import com.google.jetstream.data.repositories.mock.MockData
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@Singleton
class MockMovieRepositoryImpl @Inject constructor(
    private val movieDataSource: MovieDataSource,
    private val movieCategoryDataSource: MovieCategoryDataSource,
) : MovieRepository {
    override fun getMovieCategories() = flow {
        val list = movieCategoryDataSource.getMovieCategoryList()
        emit(list)
    }

    override suspend fun getMovieCategoryDetails(categoryId: String): MovieCategoryDetails {
        val categoryList = movieCategoryDataSource.getMovieCategoryList()
        val category = categoryList.find { categoryId == it.id } ?: categoryList.first()

        val movieList = movieDataSource.getMovieList().shuffled().subList(0, 20)

        return MovieCategoryDetails(
            id = category.id,
            name = category.name,
            movies = movieList
        )
    }

    override fun getMoviesToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): Flow<MovieResponse> = flow {
        emit(
            MovieResponse(
                member = listOf(MockData.getMovie())
            )
        )
    }

    override fun getMoviesToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieResponse> = flow {
        emit(
            MovieResponse(
                member = listOf(MockData.getMovie())
            )
        )
    }

    override fun getMoviesToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): Flow<MovieResponse> = flow {
        emit(
            MovieResponse(
                member = listOf(MockData.getMovie())
            )
        )
    }

    override fun getMovieDetailsNew(
        token: String,
        movieId: String
    ): Flow<MovieNew> = flow {
        emit(
            MockData.getMovie()
        )
    }
}