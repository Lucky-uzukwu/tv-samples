package com.google.wiltv.data.repositories

import com.google.wiltv.data.entities.toMovieCategory
import com.google.wiltv.data.util.AssetsReader
import com.google.wiltv.data.util.StringConstants
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
