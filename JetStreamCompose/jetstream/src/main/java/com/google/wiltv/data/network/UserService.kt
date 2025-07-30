package com.google.wiltv.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {

    @POST("/users")
    suspend fun createUserResource(@Body request: UserRequest): Response<UserResponse>

    @GET("users/{identifier}")
    suspend fun getUserResource(
        @Header("Authorization") authToken: String,
        @Header("Accept") accept: String = "application/ld+json",
        @Path("identifier") identifier: String
    ): Response<UserResponse>

}

data class UserRequest(
    val device: String,
    val mac: String,
    val ip: String,
)

data class UserResponse(
    val identifier: String,
    val username: String,
    val name: String,
    val email: String,
    val deviceAllowed: Int,
    val profilePhotoPath: String? = null,
    val profilePhotoUrl: String? = null,
    val registrationRequired: Boolean,
    val registrationRequiredMessage: String?,
)