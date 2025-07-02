package com.google.jetstream.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.google.jetstream.data.models.MovieRemoteKey

@Dao
interface MovieRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<MovieRemoteKey>)

    @Query("Select * From movie_remote_key Where movie_id = :id")
    suspend fun getRemoteKeyByMovieID(id: Int): MovieRemoteKey?

    @Query("Delete From movie_remote_key")
    suspend fun clearRemoteKeys()

    @Query("Select created_at From movie_remote_key Order By created_at DESC LIMIT 1")
    suspend fun getCreationTime(): Long?
}