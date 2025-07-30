package com.google.wiltv.presentation.screens.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.CustomerDataResponse
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.data.repositories.AuthRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.presentation.screens.auth.AuthScreenUiStateNew
import com.google.wiltv.presentation.screens.auth.AuthScreenUiStateNew.Idle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import retrofit2.Response
import javax.inject.Inject


data class AuthScreenUiState(
    val generatedAccessCode: String = "",
    val userInputedAccessCode: String = "",
    val isGetCustomerLoading: Boolean = false,
    val isRequestTokenForCustomerLoading: Boolean = false,
    val customerData: CustomerDataResponse? = null,
    val token: String? = null,
    val accessCodeError: String? = null,
    val passwordError: String? = null
)

sealed class AuthScreenUiStateNew {
    object Idle : AuthScreenUiStateNew()
    object Loading : AuthScreenUiStateNew()
    data class Success<T>(val body: T, val message: String) : AuthScreenUiStateNew()
    data class Error(val message: String) : AuthScreenUiStateNew()
}

sealed class AuthScreenUiEvent {
    object NavigateToLogin : AuthScreenUiEvent()
    object NavigateToRegister : AuthScreenUiEvent()
}


@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow<AuthScreenUiStateNew>(Idle)
    val uiState: StateFlow<AuthScreenUiStateNew> = _uiState

    // Expose screen UI events
    private val _uiEvent = MutableStateFlow<AuthScreenUiEvent?>(null)
    val uiEvent: StateFlow<AuthScreenUiEvent?> = _uiEvent


    fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Flow<TokenResponse?> = flow {
        _uiState.update { AuthScreenUiStateNew.Loading }
        val response = authRepository.loginWithTv(
            identifier = identifier,
            password = password,
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )
        response.collect { responsePair ->
            val code = responsePair.first
            val body = responsePair.second

            when (code) {
                201 -> {
                    _uiState.update {
                        AuthScreenUiStateNew.Success(
                            body = body,
                            message = "Login successful"
                        )
                    }
                    emit(body)
                    _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }
                }

                400 -> {
                    _uiState.update { AuthScreenUiStateNew.Error("Invalid input") }
                    emit(null)

                }

                404 -> {
                    _uiState.update { AuthScreenUiStateNew.Error("User not found please check identifer or password") }
                    _uiEvent.update { AuthScreenUiEvent.NavigateToRegister }
                    emit(null)
                }

                422 -> {
                    _uiState.update { AuthScreenUiStateNew.Error("Validation error") }
                    emit(null)
                }
            }
        }
    }

    fun getUser(identifier: String): Flow<UserResponse?> = flow {
        _uiState.update { AuthScreenUiStateNew.Loading }
        val token = userRepository.userToken.firstOrNull() ?: return@flow emit(null)
        val response = authRepository.getUser(
            token = token,
            identifier = identifier
        )

        response.collect { userResponse ->
            when (userResponse) {
                null -> {
                    _uiState.update { AuthScreenUiStateNew.Error("User not found") }
                    emit(userResponse)
                }

                else -> {
                    _uiState.update {
                        AuthScreenUiStateNew.Success(
                            body = userResponse,
                            message = "User found"
                        )
                    }
                    emit(userResponse)
                }
            }
        }


    }

    fun clearEvent() {
        _uiEvent.value = null
    }
}
