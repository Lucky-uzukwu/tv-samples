package com.google.wiltv.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.wiltv.data.entities.MovieEntity

@Dao
interface MoviesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("Select * From movie")
    fun getMovies(): PagingSource<Int, MovieEntity>

    @Query("Delete From movie")
    suspend fun clearAllMovies()
}