package com.google.wiltv.presentation.screens.register

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.google.wiltv.data.network.CustomerDataResponse
import com.google.wiltv.data.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


data class RegisterScreenUiState(
    val isLoading: Boolean = false,
    val userInputedAccessCode: String = "",
    val customerData: CustomerDataResponse? = null,
    val token: String? = null,
    val accessCodeError: String? = null,
    val passwordError: String? = null
)

sealed class RegisterScreenUiEvent {
    object NavigateToDashboard : RegisterScreenUiEvent()
}


@HiltViewModel
class RegisterScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(RegisterScreenUiState())
    val uiState: StateFlow<RegisterScreenUiState> = _uiState

    // Expose screen UI events
    private val _uiEvent = MutableStateFlow<RegisterScreenUiEvent?>(null)
    val uiEvent: StateFlow<RegisterScreenUiEvent?> = _uiEvent

    suspend fun registerCustomer(
        password: String,
        password_confirmation: String,
        email: String,
        name: String,
        identifier: String
    ) {
        _uiState.update {
            it.copy(
                isLoading = true,
                passwordError = null,
                accessCodeError = null
            )
        }

        val response = authRepository.register(
            password = password,
            password_confirmation = password_confirmation,
            email = email,
            name = name,
            identifier = identifier
        )

        when (response.code()) {
            400 -> _uiState.update {
                it.copy(
                    passwordError = "Something went wrong. Please check your password and try again",
                    isLoading = false
                )
            }

            200 -> {
//                _uiState.update {
//                    it.copy(
//                        customerData = CustomerDataResponse(
//                            id = response.body()?.name!!,
//                            identifier = response.body()?.identifier.toString(),
//                            name = response.body()?.name!!,
//                            email = response.body()?.email!!,
//                            profilePhotoUrl = if (response.body()?.profilePhotoUrl != null) response.body()?.profilePhotoUrl!! else "",
//                            profilePhotoPath =
//                                if (response.body()?.profilePhotoPath != null) response.body()?.profilePhotoPath!! else "",
//                        ),
//                        isLoading = false,
//                    )
//                }
                _uiEvent.value = RegisterScreenUiEvent.NavigateToDashboard
            }

            422 -> {
                _uiState.update {
                    it.copy(
                        passwordError = "Password is already set, try and login",
                        isLoading = false,
                    )
                }
            }

            else -> {
                Logger.e {
                    response.errorBody().toString()
                }
            }
        }


    }

    fun clearEvent() {
        _uiEvent.value = null
    }
}
