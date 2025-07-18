package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.network.LoginResponse
import com.google.jetstream.data.network.TokenResponse
import com.google.jetstream.data.network.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class MockAuthRepositoryImpl : AuthRepository {
    override suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Response<UserResponse> =
        Response.success(
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


    override suspend fun login(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Response<LoginResponse> = Response.success(LoginResponse(token = "token"))

    override suspend fun register(
        password: String,
        password_confirmation: String,
        email: String,
        name: String,
        identifier: String
    ): Response<CustomerDataResponse> = Response.success(
        CustomerDataResponse(
            identifier = "identifier",
            name = "name",
            email = "email",
            username = "username()",
            devicesAllowed = 2,
            registrationRequired = false,
            registrationRequiredMessage = "registrationRequiredMessage",
        )
    )

    override suspend fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Flow<Pair<Int, TokenResponse>> = flow {
        emit(200 to TokenResponse(token = "token"))
    }
}