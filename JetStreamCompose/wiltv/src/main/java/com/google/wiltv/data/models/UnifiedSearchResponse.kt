// ABOUTME: Unified search response model that handles mixed content types from search API
// ABOUTME: Uses custom Gson deserializer to deserialize different content types based on the 'type' field

package com.google.wiltv.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.wiltv.data.network.TvChannel
import java.lang.reflect.Type

data class UnifiedSearchResponse(
    @SerializedName("@context") val context: String? = null,
    @SerializedName("@id") val id: String? = null,
    @SerializedName("@type") val type: String? = null,
    val totalItems: Int? = null,
    val member: List<SearchContent>,
    val search: SearchTemplate? = null,
    val view: PartialCollectionView? = null
)

data class SearchTemplate(
    @SerializedName("@type") val type: String? = null,
    val template: String? = null,
    val variableRepresentation: String? = null,
    val mapping: List<IriTemplateMapping>? = null
)

data class IriTemplateMapping(
    @SerializedName("@type") val type: String? = null,
    val variable: String? = null,
    val property: String? = null
)

data class PartialCollectionView(
    @SerializedName("@id") val id: String? = null,
    @SerializedName("@type") val type: String? = null
)

class SearchContentDeserializer : JsonDeserializer<SearchContent> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SearchContent {
        val jsonObject = json.asJsonObject
        val contentType = jsonObject.get("type")?.asString
        
        return when (contentType) {
            "App\\Models\\Movie" -> {
                val movie = context.deserialize<MovieNew>(json, MovieNew::class.java)
                SearchContent.MovieContent(movie)
            }
            "App\\Models\\TvShow" -> {
                val tvShow = context.deserialize<TvShow>(json, TvShow::class.java)
                SearchContent.TvShowContent(tvShow)
            }
            "App\\Models\\TvChannel" -> {
                val tvChannel = context.deserialize<TvChannel>(json, TvChannel::class.java)
                SearchContent.TvChannelContent(tvChannel)
            }
            else -> throw JsonParseException("Unknown content type: $contentType")
        }
    }
}