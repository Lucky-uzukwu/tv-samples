package com.google.wiltv.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {

    @POST("/token")
    suspend fun createToken(@Body request: TokenRequest): Response<TokenResponse>

}

//If you set request (which is equivalent to the loginRequest code or anonymous login request) then is invalid to set username and password.
//If you set username and password then loginRequest is invalid.
//If you set request then the user is authenticated via login request code, otherwise the user is authenticated via username/email and password.
//mac, device and ip are always required.
data class TokenRequest(
    val request: String?,
    val identifier: String?, //identifier is the same as the username (and access code), You can either send the username (identifier) or the user's email address.
    val password: String?,
    val device: String,
    val mac: String,
    val ip: String,
)

data class TokenResponse(
    val token: String?,
)