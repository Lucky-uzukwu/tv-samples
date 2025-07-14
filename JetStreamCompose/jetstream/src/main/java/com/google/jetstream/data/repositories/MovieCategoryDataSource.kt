package com.google.jetstream.data.repositories

import com.google.jetstream.data.entities.toMovieCategory
import com.google.jetstream.data.util.AssetsReader
import com.google.jetstream.data.util.StringConstants
import javax.inject.Inject

class MovieCategoryDataSource @Inject constructor(
    assetsReader: AssetsReader
) {

    private val movieCategoryDataReader = CachedDataReader {
        readMovieCategoryData(assetsReader, StringConstants.Assets.MovieCategories).map {
            it.toMovieCategory()
        }
    }

    suspend fun getMovieCategoryList() = movieCategoryDataReader.read()
}
