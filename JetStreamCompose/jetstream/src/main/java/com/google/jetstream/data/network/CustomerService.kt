package com.google.jetstream.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CustomerService {

    @POST("api/customers")
    suspend fun requestTokenForCustomer(@Body request: TokenForCustomerRequest): Response<TokenForCustomerResponse>
}


data class TokenForCustomerRequest(
    val deviceMacAddress: String,
    val clientIp: String,
    val deviceName: String,
)

data class TokenForCustomerResponse(
    val identifier: String,
)