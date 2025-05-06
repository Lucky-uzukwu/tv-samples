package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.network.CustomerService
import com.google.jetstream.data.network.TokenForCustomerRequest
import com.google.jetstream.data.network.TokenForCustomerResponse
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerService: CustomerService,
) : CustomerRepository {

    override suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<TokenForCustomerResponse> {
        return customerService.requestTokenForCustomer(
            TokenForCustomerRequest(
                deviceMacAddress = deviceMacAddress,
                clientIp = clientIp,
                deviceName = deviceName
            )
        )

    }

    override suspend fun getCustomer(identifier: String): Response<CustomerDataResponse> {
        return customerService.getCustomer(
            identifier = identifier
        )
    }
}