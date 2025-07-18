package com.google.jetstream.presentation.screens.login

import androidx.lifecycle.ViewModel
import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class LoginScreenUiState(
    val isLoading: Boolean = false,
    val customerData: CustomerDataResponse? = null,
    val token: String? = null,
    val accessCodeError: String? = null,
    val passwordError: String? = null
)

sealed class LoginScreenUiEvent {
    object NavigateToDashboard : LoginScreenUiEvent()
}


@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(LoginScreenUiState())
    val uiState: StateFlow<LoginScreenUiState> = _uiState

    // Expose screen UI events
    private val _uiEvent = MutableStateFlow<LoginScreenUiEvent?>(null)
    val uiEvent: StateFlow<LoginScreenUiEvent?> = _uiEvent

    suspend fun loginCustomer(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ) {
        _uiState.update {
            it.copy(
                isLoading = true,
                passwordError = null,
                accessCodeError = null
            )
        }

        val response = authRepository.login(
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName,
            identifier = identifier,
            password = password
        )

        when (response.code()) {
            400 -> _uiState.update {
                it.copy(
                    passwordError = "Something went wrong. Please check your password and try again",
                    isLoading = false
                )
            }

            403 -> _uiState.update {
                it.copy(
                    passwordError = "Something went wrong. Please check your password and try again later",
                    isLoading = false
                )
            }

            201 -> {
                _uiState.update {
                    it.copy(
                        token = response.body()?.token!!,
                        isLoading = false,
                    )
                }
                _uiEvent.value = LoginScreenUiEvent.NavigateToDashboard
            }

            422 -> {
                //TODO:  navigate to register screen
                _uiState.update {
                    it.copy(
                        accessCodeError = "Something went wrong. Please check your identifier and try again",
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun clearEvent() {
        _uiEvent.value = null
    }
}
