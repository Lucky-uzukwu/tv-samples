package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.network.LoginResponse
import com.google.jetstream.data.network.TokenForCustomerResponse
import retrofit2.Response

class MockCustomerRepositoryImpl : CustomerRepository {
    override suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Response<TokenForCustomerResponse> =
        Response.success(TokenForCustomerResponse(identifier = "identifier"))

    override suspend fun getCustomer(identifier: String): Response<CustomerDataResponse> =
        Response.success(
            CustomerDataResponse(
                id = "id",
                identifier = "identifier",
                name = "name",
                email = "email",
                profilePhotoPath = "profilePhotoPath",
                profilePhotoUrl = "profilePhotoUrl"
            )
        )

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
            id = "id",
            identifier = "identifier",
            name = "name",
            email = "email",
            profilePhotoPath = "profilePhotoPath",
            profilePhotoUrl = "profilePhotoUrl"
        )
    )
}