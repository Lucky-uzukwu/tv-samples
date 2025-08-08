package com.google.wiltv.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.wiltv.data.models.Language
import com.google.wiltv.data.models.StreamingProvider

class StreamingProviderConverter {
    private val gson = Gson()

    @TypeConverter
    fun toJsonString(streamingProviders: List<StreamingProvider>?): String? {
        return gson.toJson(streamingProviders)
    }

    @TypeConverter
    fun fromJsonString(json: String?): List<StreamingProvider>? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<List<StreamingProvider>>() {}.type)
        }
    }
}