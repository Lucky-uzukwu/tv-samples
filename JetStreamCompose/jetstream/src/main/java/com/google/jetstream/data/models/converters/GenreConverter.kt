package com.google.jetstream.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.jetstream.data.models.Genre

class GenreConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromGenreListToJson(genres: List<Genre>?): String? {
        return gson.toJson(genres)
    }

    @TypeConverter
    fun fromJsonToGenreList(genresJson: String?): List<Genre>? {
        return genresJson?.let {
            gson.fromJson(it, object : TypeToken<List<Genre>>() {}.type)
        }
    }
}