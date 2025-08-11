package com.google.wiltv.data.repositories.errors

import com.google.gson.annotations.SerializedName

data class Error(
    @SerializedName("@context")
    val context: String,
    @SerializedName("@id")
    val id: String,
    @SerializedName("@type")
    val atType: String,
    val type: String,

    val title: String,

    val detail: String,

    val status: Int,

    )