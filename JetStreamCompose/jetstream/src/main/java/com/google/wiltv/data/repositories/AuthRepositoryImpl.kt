package com.google.wiltv.data.repositories

import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.CustomerDataResponse
import com.google.wiltv.data.network.CustomerService
import com.google.wiltv.data.network.LoginRequest
import com.google.wiltv.data.network.LoginResponse
import com.google.wiltv.data.network.SetPasswordRequest
import com.google.wiltv.data.network.TokenRequest
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.TokenService
import com.google.wiltv.data.network.UserRequest
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.data.network.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val customerService: CustomerService,
    private val userService: UserService,
    private val tokenService: TokenService
) : AuthRepository {

    override suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<UserResponse> {
        return userService.createUserResource(
            UserRequest(
                mac = deviceMacAddress,
                ip = clientIp,
                device = deviceName
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

    override suspend fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): Flow<Pair<Int, TokenResponse>> = flow {
        Logger.i { "Attempting to log in user with TV with identifier: $identifier" }
        val tokenRequest = TokenRequest(
            identifier = identifier,
            password = password,
            mac = deviceMacAddress,
            ip = clientIp,
            device = deviceName,
            request = null
        )
        val response = tokenService.createToken(tokenRequest)

        when (response.code()) {
            404 -> {
                Logger.i { "Customer not found with identifier: $identifier" }
                emit(response.code() to TokenResponse(null))
            }

            201 -> {
                Logger.i { "Successfully logged in user with TV with identifier: $identifier" }
                emit(response.code() to response.body()!!)
            }

            422 -> {
                Logger.e { "Validation error occurred while login user with identifier : $identifier" }
                emit(response.code() to TokenResponse(null))
            }

            400 -> {
                Logger.e { "Invalid input while login user with identifier : $identifier" }
                emit(response.code() to TokenResponse(null))
            }
        }
    }

    override suspend fun loginWithAccessCode(
        accessCode: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ) = flow {
        Logger.i { "Attempting to log in user with access code: $accessCode" }
        val tokenRequest = TokenRequest(
            request = accessCode,
            mac = deviceMacAddress,
            ip = clientIp,
            device = deviceName,
            password = null,
            identifier = null
        )
        val response = tokenService.createToken(tokenRequest)

        when (response.code()) {
            404 -> {
                Logger.i { "Customer not found with accessCode: $accessCode" }
                emit(response.code() to TokenResponse(null))
            }

            201 -> {
                Logger.i { "Successfully logged in user with accessCode: $accessCode" }
                emit(response.code() to response.body()!!)
            }

            422 -> {
                Logger.e { "Validation error occurred while login user with accessCode : $accessCode" }
                emit(response.code() to TokenResponse(null))
            }

            400 -> {
                Logger.e { "Invalid input while login user with accessCode : $accessCode" }
                emit(response.code() to TokenResponse(null))
            }
        }
    }


    override suspend fun getUser(token: String, identifier: String): Flow<UserResponse?> = flow {
        Logger.i { "Attempting to get user with identifier: $identifier" }
        val response = userService.getUserResource(
            authToken = "Bearer $token",
            identifier = identifier
        )
        when (response.code()) {
            200 -> {
                Logger.i { "Successfully got user with identifier: $identifier" }
                emit(response.body()!!)
            }

            404 -> {
                Logger.e { "User not found with identifier: $identifier" }
                emit(null)
            }

            401 -> {
                Logger.e { "Unauthorized access with identifier: $identifier" }
                emit(null)
            }
        }
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