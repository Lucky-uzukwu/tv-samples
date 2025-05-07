package com.google.jetstream.data.repositories

import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.network.CustomerService
import com.google.jetstream.data.network.LoginRequest
import com.google.jetstream.data.network.LoginResponse
import com.google.jetstream.data.network.SetPasswordRequest
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

    override suspend fun register(
        password: String,
        password_confirmation: String,
        email: String,
        name: String,
        identifier: String
    ): Response<CustomerDataResponse> {
        return customerService.setPassword(
            identifier = identifier,
            request = SetPasswordRequest(
                password = password,
                password_confirmation = password_confirmation,
                email = email,
                name = name,
            )
        )
    }

    override suspend fun getCustomer(identifier: String): Response<CustomerDataResponse> {
        return customerService.getCustomer(
            identifier = identifier
        )
    }

    override suspend fun login(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Response<LoginResponse> = customerService.login(
        LoginRequest(
            identifier = identifier,
            password = password,
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )
    )
}