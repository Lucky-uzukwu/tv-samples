package com.google.jetstream.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.google.jetstream.data.entities.User
import com.google.jetstream.data.repositories.UserRepository
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
                userRepository.userId,
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
                Logger.i(user.contentToString())
                UserState(
                    user = User(
                        id = user[0] ?: "",
                        accessCode = user[1] ?: "",
                        name = user[2] ?: "",
                        email = user[3] ?: "",
                        profilePhotoPath = user[4],
                        profilePhotoUrl = user[5],
                        token = user[6],
                        password = user[7] ?: "",
                        clientIp = user[8] ?: "",
                        deviceName = user[9] ?: "",
                        deviceMacAddress = user[10] ?: ""
                    )
                )
            }.collect { newState ->
                _userState.value = newState
            }
        }
    }

    suspend fun updateUser(user: User) {
        // Atomic read-modify-write
        Logger.i { "User to update ${userState.value.user}" }
        Logger.i { "new user received $user" }
        _userState.update { current ->
            current.copy(user = user)
        }
        userRepository.saveUserId(user.id)
        user.token?.let { userRepository.saveUserToken(it) }
        userRepository.saveUserName(user.name)
        userRepository.saveUserEmail(user.email)
        user.password?.let { userRepository.saveUserPassword(it) }
        userRepository.saveUserClientIp(user.clientIp)
        userRepository.saveUserDeviceName(user.deviceName)
        userRepository.saveUserDeviceMacAddress(user.deviceMacAddress)
        userRepository.saveUserAccessCode(user.accessCode)
        user.profilePhotoPath?.let { userRepository.saveUserProfilePhotoPath(it) }
        user.profilePhotoUrl?.let { userRepository.saveUserProfilePhotoUrl(it) }
    }

    fun clearUser() {
        // Atomically reset
        _userState.update { UserState() }
    }
}
