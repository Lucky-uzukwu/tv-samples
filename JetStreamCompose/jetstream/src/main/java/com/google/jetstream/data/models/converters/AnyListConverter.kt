package com.google.jetstream.data.models.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AnyListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAnyList(list: List<Any>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toAnyList(json: String?): List<Any>? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<List<Any>>() {}.type)
        }
    }
}