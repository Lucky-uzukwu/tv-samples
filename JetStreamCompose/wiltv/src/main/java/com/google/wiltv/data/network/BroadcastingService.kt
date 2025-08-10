package com.google.wiltv.data.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class BroadcastingAuthResponse(
    val auth: String
)

interface BroadcastingService {
    
    @FormUrlEncoded
    @POST("/broadcasting/auth")
    suspend fun authenticateChannel(
        @Field("channel_name") channelName: String,
        @Field("socket_id") socketId: String
    ): Response<BroadcastingAuthResponse>
}