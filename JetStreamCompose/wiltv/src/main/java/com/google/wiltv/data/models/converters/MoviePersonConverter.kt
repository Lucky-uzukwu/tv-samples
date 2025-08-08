package com.google.wiltv.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.wiltv.data.models.MoviePerson

class MoviePersonConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAddressList(moviePersons: List<MoviePerson>?): String {
        return gson.toJson(moviePersons)
    }

    @TypeConverter
    fun toAddressList(personListJson: String): List<MoviePerson> {
        return personListJson.let {
            gson.fromJson(it, object : TypeToken<List<MoviePerson>>() {}.type)
        }
    }
}