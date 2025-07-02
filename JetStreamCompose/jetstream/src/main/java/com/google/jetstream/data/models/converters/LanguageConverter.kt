package com.google.jetstream.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.jetstream.data.models.Language

class LanguageConverter {
    private val gson = Gson()

    @TypeConverter
    fun toJsonString(languages: List<Language>?): String? {
        return gson.toJson(languages)
    }

    @TypeConverter
    fun fromJsonString(json: String?): List<Language>? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<List<Language>>() {}.type)
        }
    }
}