package com.google.wiltv.data.repositories.errors

import com.google.gson.annotations.SerializedName

data class ValidationError(
    @SerializedName("@context")
    val context: String,

    @SerializedName("@id")
    val id: String,

    @SerializedName("@type")
    val atType: String,

    val description: String,

    val type: String,

    val title: String,

    val detail: String,

    val status: Int,

    val violations: List<Violation>
)

data class Violation(
    val propertyPath: String,
    val message: String
)