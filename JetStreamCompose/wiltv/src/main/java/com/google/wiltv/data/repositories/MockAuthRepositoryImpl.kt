package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.LoginResponse
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class MockAuthRepositoryImpl : AuthRepository {
    override suspend fun requestTokenForNewCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<UserResponse, DataError.Network> =
        ApiResult.Success(
            UserResponse(
                identifier = "identifier",
                username = "username",
                name = "name",
                email = "email",
                deviceAllowed = 1,
                profilePhotoPath = "profilePhotoPath",
                registrationRequired = true,
                registrationRequiredMessage = "registrationRequiredMessage"
            )
        )

    override suspend fun requestTokenForExistingCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<LoginResponse, DataError.Network> = ApiResult.Success(
        LoginResponse(
            code = "code",
            deviceName = "code",
            validUntil = "code",
            createdAt = "code",
            qrCode = "code",
            confirmedAt = "code"
        )
    )

    override suspend fun getUser(
        token: String,
        identifier: String
    ): Flow<UserResponse?> = flow {
        emit(
            UserResponse(
                identifier = "identifier",
                name = "name",
                email = "email",
                profilePhotoPath = "profilePhotoPath",
                profilePhotoUrl = "profilePhotoUrl",
                deviceAllowed = 1,
                registrationRequired = true,
                username = "username",
                registrationRequiredMessage = "registrationRequiredMessage",
            )
        )
    }

    override suspend fun loginWithAccessCode(
        accessCode: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<TokenResponse, DataError.Network> {
        return ApiResult.Success(TokenResponse(token = "token"))
    }


    override suspend fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<TokenResponse, DataError.Network> {
        return ApiResult.Success(TokenResponse(token = "token"))
    }
}