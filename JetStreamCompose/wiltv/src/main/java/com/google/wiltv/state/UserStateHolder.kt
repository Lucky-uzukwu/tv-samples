package com.google.wiltv.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.wiltv.data.entities.User
import com.google.wiltv.data.entities.Profile
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.data.repositories.ProfileRepository
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
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    // Expose User state
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState

    init {
        viewModelScope.launch {
            // Initialize default profiles on first launch
            profileRepository.initializeDefaultProfiles()
            
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
                profileRepository.getAllProfiles(),
                profileRepository.getSelectedProfile()
            ) { userParams ->
                UserState(
                    user = User(
                        id = java.util.UUID.randomUUID().toString(), // Generate UUID for existing users
                        identifier = userParams[0] as? String ?: "",
                        name = userParams[1] as? String ?: "",
                        email = userParams[2] as? String ?: "",
                        profilePhotoPath = userParams[3] as? String,
                        profilePhotoUrl = userParams[4] as? String,
                        token = userParams[5] as? String,
                        password = userParams[6] as? String ?: "",
                        clientIp = userParams[7] as? String ?: "",
                        deviceName = userParams[8] as? String ?: "",
                        deviceMacAddress = userParams[9] as? String ?: ""
                    ),
                    profiles = userParams[10] as? List<Profile> ?: emptyList(),
                    selectedProfile = userParams[11] as? Profile
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

    suspend fun selectProfile(profileId: String) {
        profileRepository.selectProfile(profileId)
    }

    suspend fun clearSelectedProfile() {
        profileRepository.selectProfile("")
    }

    fun clearAllProfiles() {
        viewModelScope.launch {
            profileRepository.clearAllProfiles()
        }
    }
}
