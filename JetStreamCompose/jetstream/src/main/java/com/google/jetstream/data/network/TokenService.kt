package com.google.jetstream.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface TokenService {

    @PATCH("api/token")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>


}

data class LoginRequest(
    val identifier: String,
    val password: String,
    val deviceMacAddress: String,
    val clientIp: String,
    val deviceName: String,
)

data class LoginResponse(
    val token: String,
)