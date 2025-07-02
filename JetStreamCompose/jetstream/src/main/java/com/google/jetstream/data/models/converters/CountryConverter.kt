package com.google.jetstream.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.jetstream.data.models.Country

class CountryConverter {
    private val gson = Gson()

    @TypeConverter
    fun toJsonString(countries: List<Country>?): String? {
        return gson.toJson(countries)
    }

    @TypeConverter
    fun fromJsonString(json: String?): List<Country>? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<List<Country>>() {}.type)
        }
    }
}