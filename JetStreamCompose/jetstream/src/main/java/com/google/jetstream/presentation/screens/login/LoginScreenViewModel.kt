package com.google.jetstream.presentation.screens.login

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


data class LoginScreenUiState(
    val accessCode: String = "",
    val isLoading: Boolean = false,
    val customerData: CustomerDataResponse? = null,
    val error: String? = null
)


@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow(LoginScreenUiState())
    val uiState: StateFlow<LoginScreenUiState> = _uiState


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