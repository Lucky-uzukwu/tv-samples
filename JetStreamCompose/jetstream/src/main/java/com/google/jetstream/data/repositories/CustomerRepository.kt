package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.network.TokenForCustomerResponse
import retrofit2.Response

interface CustomerRepository {

    suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<TokenForCustomerResponse>

    suspend fun getCustomer(identifier: String): Response<CustomerDataResponse>
}