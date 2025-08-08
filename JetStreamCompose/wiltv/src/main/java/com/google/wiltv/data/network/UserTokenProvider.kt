package com.google.wiltv.data.network

import com.google.wiltv.data.repositories.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserTokenProvider @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun getToken(): String? {
        return userRepository.userToken.first()
    }
}