package com.google.jetstream.presentation.screens.auth

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.google.jetstream.data.network.CustomerDataResponse
import com.google.jetstream.data.network.TokenForCustomerResponse
import com.google.jetstream.data.repositories.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

sealed class AuthScreenUiEvent {
    object NavigateToLogin : AuthScreenUiEvent()
    object NavigateToDashboard : AuthScreenUiEvent()
    object NavigateToRegister : AuthScreenUiEvent()
}


@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(AuthScreenUiState())
    val uiState: StateFlow<AuthScreenUiState> = _uiState

    // Expose screen UI events
    private val _uiEvent = MutableStateFlow<AuthScreenUiEvent?>(null)
    val uiEvent: StateFlow<AuthScreenUiEvent?> = _uiEvent

    suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<TokenForCustomerResponse> {

        _uiState.update { it.copy(isRequestTokenForCustomerLoading = true) }

        val response = customerRepository.requestTokenForCustomer(
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )
        if (response.isSuccessful) {
            _uiState.update {
                it.copy(
                    generatedAccessCode = response.body()?.identifier.toString(),
                    isRequestTokenForCustomerLoading = false
                )
            }

            Logger.i { "Customer access code: ${response.body()?.identifier}" }

        } else {
            // Handle error response
            _uiState.update {
                it.copy(
                    accessCodeError = "Error: ${response.code()}",
                    isRequestTokenForCustomerLoading = false
                )
            }
        }

        return response
    }

    suspend fun getCustomer(identifier: String) {
        _uiState.update { it.copy(isGetCustomerLoading = true) }

        val response = customerRepository.getCustomer(identifier = identifier)

        when (response.code()) {
            404 -> _uiState.update {
                it.copy(
                    accessCodeError = "Something went wrong. Ensure the access code is correctly inputed",
                    isGetCustomerLoading = false
                )
            }

            200 -> {
                _uiState.update {
                    it.copy(
                        customerData = response.body(),
                        isGetCustomerLoading = false,
                        userInputedAccessCode = response.body()?.identifier!!
                    )
                }
                _uiEvent.value = AuthScreenUiEvent.NavigateToLogin
            }

            202 -> {
                _uiState.update {
                    it.copy(
                        customerData = response.body(),
                        isGetCustomerLoading = false,
                        userInputedAccessCode = response.body()?.identifier!!
                    )
                }
                _uiEvent.value = AuthScreenUiEvent.NavigateToRegister
            }
        }
    }

    suspend fun registerCustomer(
        password: String,
        password_confirmation: String,
        email: String,
        name: String,
    ) {
        _uiState.update {
            it.copy(
                isGetCustomerLoading = true,
                passwordError = null,
                accessCodeError = null
            )
        }

        val response = customerRepository.register(
            password = password,
            password_confirmation = password_confirmation,
            email = email,
            name = name,
            identifier = _uiState.value.userInputedAccessCode
        )

        when (response.code()) {
            400 -> _uiState.update {
                it.copy(
                    passwordError = "Something went wrong. Please check your password and try again",
                    isGetCustomerLoading = false
                )
            }

            200 -> {
                _uiState.update {
                    it.copy(
                        customerData = CustomerDataResponse(
                            id = response.body()?.name!!,
                            identifier = response.body()?.identifier.toString(),
                            name = response.body()?.name!!,
                            email = response.body()?.email!!,
                            profilePhotoUrl = if (response.body()?.profilePhotoUrl != null) response.body()?.profilePhotoUrl!! else "",
                            profilePhotoPath =
                                if (response.body()?.profilePhotoPath != null) response.body()?.profilePhotoPath!! else "",
                        ),
                        isGetCustomerLoading = false,
                    )
                }
                _uiEvent.value = AuthScreenUiEvent.NavigateToDashboard
            }

            422 -> {
                _uiState.update {
                    it.copy(
                        passwordError = "Password is already set, try and login",
                        isGetCustomerLoading = false,
                    )
                }
            }
        }


    }

    fun clearEvent() {
        _uiEvent.value = null
    }
}
