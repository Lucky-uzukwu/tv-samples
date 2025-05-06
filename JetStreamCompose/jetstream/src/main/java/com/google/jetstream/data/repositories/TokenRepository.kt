package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.LoginResponse
import retrofit2.Response

interface TokenRepository {

    suspend fun login(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<LoginResponse>
}