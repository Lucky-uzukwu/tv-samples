package com.google.wiltv.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.wiltv.data.models.Subtitle

class SubtitleConverter {
    private val gson = Gson()

    @TypeConverter
    fun toJsonString(subtitles: List<Subtitle>?): String? {
        return gson.toJson(subtitles)
    }

    @TypeConverter
    fun fromJsonString(json: String?): List<Subtitle>? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<List<Subtitle>>() {}.type)
        }
    }
}