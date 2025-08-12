package com.google.wiltv.data.repositories

import com.google.wiltv.data.entities.MovieCategoryDetails
import com.google.wiltv.data.entities.MovieCategoryList
import com.google.wiltv.data.models.MovieNew
import com.google.wiltv.data.network.MovieResponse
import com.google.wiltv.data.repositories.mock.MockData
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
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

    override suspend fun getMoviesToShowInHeroSection(
        token: String,
        page: Int,
        itemsPerPage: Int
    ): ApiResult<MovieResponse, DataError.Network> {
        return ApiResult.Success(
            MovieResponse(
                member = listOf(MockData.getMovie())
            )
        )
    }

    override suspend fun getMoviesToShowInCatalogSection(
        token: String,
        catalogId: String,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieResponse, DataError.Network> {
        return ApiResult.Success(
            MovieResponse(
                member = listOf(MockData.getMovie())
            )
        )
    }

    override suspend fun getMoviesToShowInGenreSection(
        token: String,
        genreId: Int,
        itemsPerPage: Int,
        page: Int
    ): ApiResult<MovieResponse, DataError.Network> {
        return ApiResult.Success(
            MovieResponse(
                member = listOf(MockData.getMovie())
            )
        )
    }

    override suspend fun getMovieDetailsNew(
        token: String,
        movieId: String
    ): ApiResult<MovieNew, DataError.Network> {
        return ApiResult.Success(
            MockData.getMovie()
        )
    }
}