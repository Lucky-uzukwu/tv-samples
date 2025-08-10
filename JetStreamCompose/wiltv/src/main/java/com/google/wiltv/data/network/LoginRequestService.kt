package com.google.wiltv.data.network

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginRequestService {

    @POST("/login_requests")
    suspend fun createUserResource(@Body request: LoginRequest): Response<LoginResponse>

}


data class LoginRequest(
    val requesterMacAddress: String,
    val deviceName: String,
    val requesterIpAddress: String
)

data class LoginResponse(
    val code: String,
    val deviceName: String,
    val validUntil: String,
    val createdAt: String,
    val qrCode: String,
    val confirmedAt: String?
)