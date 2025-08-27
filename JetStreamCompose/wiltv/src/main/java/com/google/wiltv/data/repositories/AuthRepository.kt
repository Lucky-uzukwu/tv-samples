package com.google.wiltv.data.repositories

import com.google.wiltv.data.network.LoginResponse
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun requestTokenForNewCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): ApiResult<UserResponse, DataError.Network>

    suspend fun requestTokenForExistingCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): ApiResult<LoginResponse, DataError.Network>

    suspend fun getUser(token: String, identifier: String): ApiResult<UserResponse, DataError.Network>

    suspend fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): ApiResult<TokenResponse, DataError.Network>


    suspend fun loginWithAccessCode(
        accessCode: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): ApiResult<TokenResponse, DataError.Network>

}