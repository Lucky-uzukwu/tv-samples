package com.google.jetstream.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.jetstream.data.models.MovieNew

@Dao
interface MoviesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieNew>)

    @Query("Select * From movie")
    fun getMovies(): PagingSource<Int, MovieNew>

    @Query("Delete From movie")
    suspend fun clearAllMovies()
}