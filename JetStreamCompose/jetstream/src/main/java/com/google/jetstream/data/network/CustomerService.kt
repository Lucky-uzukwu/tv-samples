package com.google.jetstream.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.Path

interface CustomerService {

    @POST("api/customers")
    suspend fun requestTokenForCustomer(@Body request: TokenForCustomerRequest): Response<TokenForCustomerResponse>

    @GET("api/customers/{identifier}")
    suspend fun getCustomer(@Path("identifier") identifier: String): Response<CustomerDataResponse>


    @POST("api/token")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Headers(
        "Content-Type: application/merge-patch+json",
        "Accept: application/json"
    )
    @PATCH("api/customers/{identifier}/set_password")
    suspend fun setPassword(
        @Path("identifier") identifier: String,
        @Body request: SetPasswordRequest
    ): Response<CustomerDataResponse>
}

data class SetPasswordRequest(
    val password: String,
    val password_confirmation: String,
    val email: String,
    val name: String,
)

data class TokenForCustomerRequest(
    val deviceMacAddress: String,
    val clientIp: String,
    val deviceName: String,
)

data class TokenForCustomerResponse(
    val identifier: String,
)

data class CustomerDataResponse(
    val id: String,
    val identifier: String,
    val name: String,
    val email: String,
    val profilePhotoPath: String?,
    val profilePhotoUrl: String?,
)

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