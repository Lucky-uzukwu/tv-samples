package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.LoginRequest
import com.google.wiltv.data.network.LoginRequestService
import com.google.wiltv.data.network.LoginResponse
import com.google.wiltv.data.network.TokenRequest
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.TokenService
import com.google.wiltv.data.network.UserRequest
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.data.network.UserService
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.domain.DataError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userService: UserService,
    private val loginRequestService: LoginRequestService,
    private val tokenService: TokenService
) : AuthRepository {

    override suspend fun requestTokenForNewCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): ApiResult<UserResponse, DataError.Network> {
        Logger.i { "Attempting to create new user with device: $deviceName" }
        val response = userService.createUserResource(
            UserRequest(
                mac = deviceMacAddress,
                ip = clientIp,
                device = deviceName
            )
        )

        return mapToResult(response)
    }

    override suspend fun requestTokenForExistingCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<LoginResponse, DataError.Network> {
        Logger.i { "Attempting to create login request for device: $deviceName" }
        val response = loginRequestService.createUserResource(
            LoginRequest(
                requesterMacAddress = deviceMacAddress,
                deviceName = deviceName,
                requesterIpAddress = clientIp
            )
        )
        return mapToResult(response)
    }

    override suspend fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<TokenResponse, DataError.Network> {
        Logger.i { "Attempting to log in user with TV with identifier: $identifier" }
        val tokenRequest = TokenRequest(
            identifier = identifier,
            password = password,
            mac = deviceMacAddress,
            ip = clientIp,
            device = deviceName,
            request = null
        )
        val response = tokenService.createToken(request = tokenRequest)
        return mapToResult(response)
    }

    override suspend fun loginWithAccessCode(
        accessCode: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): ApiResult<TokenResponse, DataError.Network> {
        Logger.i { "Attempting to login with access code for device: $deviceName" }
        val tokenRequest = TokenRequest(
            request = accessCode,
            mac = deviceMacAddress,
            ip = clientIp,
            device = deviceName,
            password = null,
            identifier = null
        )
        val response = tokenService.createToken(request = tokenRequest)
        return mapToResult(response)
    }


    override suspend fun getUser(token: String, identifier: String): ApiResult<UserResponse, DataError.Network> {
        Logger.i { "Attempting to get user with identifier: $identifier" }
        val response = userService.getUserResource(
            authToken = "Bearer $token",
            identifier = identifier
        )
        return mapToResult(response)
    }
}