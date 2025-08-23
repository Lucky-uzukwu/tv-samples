package com.google.wiltv.data.models


data class TvShowSeasonsResponse(
    val member: List<Season>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
)

data class TvShowEpisodesResponse(
    val member: List<Episode>,
    val totalItems: Int? = null,
    val viewDetails: ViewDetails? = null,
)