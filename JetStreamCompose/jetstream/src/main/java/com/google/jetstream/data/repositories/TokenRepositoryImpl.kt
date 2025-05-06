package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.LoginRequest
import com.google.jetstream.data.network.LoginResponse
import com.google.jetstream.data.network.TokenService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepositoryImpl @Inject constructor(
    private val tokenService: TokenService
) : TokenRepository {
    override suspend fun login(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Response<LoginResponse> = tokenService.login(
        LoginRequest(
            identifier = identifier,
            password = password,
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )
    )
}