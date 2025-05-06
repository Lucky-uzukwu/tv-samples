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

    @GET("api/customers/{identifier}")
    suspend fun getCustomer(@Path("identifier") identifier: String): Response<CustomerDataResponse>


}


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
)