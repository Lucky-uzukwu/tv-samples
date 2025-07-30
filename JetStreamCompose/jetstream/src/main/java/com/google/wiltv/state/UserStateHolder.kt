package com.google.wiltv.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.User
import com.google.wiltv.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserStateHolder @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    // Expose User state
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState

    init {
        viewModelScope.launch {
            combine(
                userRepository.userAccessCode,
                userRepository.userName,
                userRepository.userEmail,
                userRepository.userProfilePhotoPath,
                userRepository.userProfilePhotoUrl,
                userRepository.userToken,
                userRepository.userPassword,
                userRepository.userClientIp,
                userRepository.userDeviceName,
                userRepository.userDeviceMacAddress,
            ) { user ->
                UserState(
                    user = User(
                        identifier = user[0] ?: "",
                        name = user[1] ?: "",
                        email = user[2] ?: "",
                        profilePhotoPath = user[3],
                        profilePhotoUrl = user[4],
                        token = user[5],
                        password = user[6] ?: "",
                        clientIp = user[7] ?: "",
                        deviceName = user[8] ?: "",
                        deviceMacAddress = user[9] ?: ""
                    )
                )
            }.collect { newState ->
                _userState.value = newState
            }
        }
    }

    suspend fun updateUser(user: User) {
        // Atomic read-modify-write
        _userState.update { current ->
            current.copy(user = user)
        }
        user.token?.let { userRepository.saveUserToken(it) }
        userRepository.saveUserName(user.name)
        userRepository.saveUserEmail(user.email)
        user.password?.let { userRepository.saveUserPassword(it) }
        userRepository.saveUserClientIp(user.clientIp)
        userRepository.saveUserDeviceName(user.deviceName)
        userRepository.saveUserDeviceMacAddress(user.deviceMacAddress)
        userRepository.saveUserAccessCode(user.identifier)
        user.profilePhotoPath?.let { userRepository.saveUserProfilePhotoPath(it) }
        user.profilePhotoUrl?.let { userRepository.saveUserProfilePhotoUrl(it) }
    }

    suspend fun updateToken(token: String) {
        userRepository.saveUserToken(token)
    }

    fun clearUser() {
        // Atomically reset
        _userState.update { UserState() }
        viewModelScope.launch {
            userRepository.clearUserData()
        }
    }
}
