package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.CustomerDataResponse
import com.google.wiltv.data.network.LoginResponse
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.UserResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface AuthRepository {

    suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<UserResponse>

    suspend fun getUser(token: String, identifier: String): Flow<UserResponse?>

    suspend fun login(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<LoginResponse>

    suspend fun register(
        password: String,
        password_confirmation: String,
        email: String,
        name: String,
        identifier: String
    ): Response<CustomerDataResponse>

    suspend fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Flow<Pair<Int, TokenResponse>>

}