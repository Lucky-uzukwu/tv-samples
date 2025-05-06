package com.google.jetstream.presentation.screens.auth

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.google.jetstream.data.network.TokenForCustomerResponse
import com.google.jetstream.data.repositories.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Response

data class AuthScreenState(
    val accessCode: String = "",
    val isLoading: Boolean = false,
)


@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(AuthScreenState())
    val uiState: StateFlow<AuthScreenState> = _uiState

    suspend fun requestTokenForCustomer(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Response<TokenForCustomerResponse> {

        _uiState.update { it.copy(isLoading = true) }

        val response = customerRepository.requestTokenForCustomer(
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )
        if (response.isSuccessful) {
            _uiState.update {
                it.copy(
                    accessCode = response.body()?.identifier.toString(),
                    isLoading = false
                )
            }

            Logger.i { "Customer access code: ${response.body()?.identifier}" }

        } else {
            // Handle error response
            _uiState.update { it.copy(accessCode = "Error: ${response.code()}", isLoading = false) }
        }

        return response
    }

}