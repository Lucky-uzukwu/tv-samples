package com.google.wiltv.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.google.gson.Gson
import com.google.wiltv.data.network.BroadcastingService
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.data.repositories.AuthRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.domain.ApiResult
import com.google.wiltv.presentation.UiText
import com.google.wiltv.presentation.asUiText
import com.google.wiltv.presentation.screens.auth.AuthScreenUiState.Idle
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

sealed class AuthScreenUiState {
    object Idle : AuthScreenUiState()
    object Loading : AuthScreenUiState()

    data class RegistrationCode(val code: String?) : AuthScreenUiState()
    data class LoginRequestCode(val code: String?) : AuthScreenUiState()

    data class Connected(val message: String) : AuthScreenUiState()
    data class Success<T>(val body: T, val message: String) : AuthScreenUiState()
    data class Error(val message: UiText) : AuthScreenUiState()
    data class RegistrationError(val message: UiText) : AuthScreenUiState()
    data class LoginWithSmartphoneError(val message: UiText) : AuthScreenUiState()
    data class LoginWithAccessCodeError(val message: UiText) : AuthScreenUiState()
    data class LoginWithTvError(val message: UiText) : AuthScreenUiState()
}

sealed class AuthScreenUiEvent {
    object NavigateToLogin : AuthScreenUiEvent()
    object NavigateToRegister : AuthScreenUiEvent()
}


@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val broadcastingService: BroadcastingService,
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow<AuthScreenUiState>(Idle)
    val uiState: StateFlow<AuthScreenUiState> = _uiState

    private val _connectionStatus = MutableStateFlow("Connecting...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private var pusher: Pusher? = null
    private var channel: Channel? = null
    private val gson = Gson()
    private var connectionStartTime: Long = 0
    private var isConnected = false

    // Expose screen UI events
    private val _uiEvent = MutableStateFlow<AuthScreenUiEvent?>(null)
    val uiEvent: StateFlow<AuthScreenUiEvent?> = _uiEvent


    fun initializeActivation(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
        isNewCustomer: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.value = AuthScreenUiState.Loading

            if (isNewCustomer) {
                val registrationResult = authRepository.requestTokenForNewCustomer(
                    deviceMacAddress = deviceMacAddress,
                    clientIp = clientIp,
                    deviceName = deviceName
                )
                when (registrationResult) {
                    is ApiResult.Success -> {
                        Logger.d { "API Success (New Customer): ${registrationResult.data}" }
                        val registrationTokenResponse = registrationResult.data
                        setupPusherConnection(
                            registrationTokenResponse.identifier,
                            deviceMacAddress,
                            clientIp,
                            deviceName
                        )

                        _uiState.value = AuthScreenUiState.RegistrationCode(
                            code = registrationTokenResponse.identifier,
                        )
                    }

                    is ApiResult.Error -> {
                        Logger.e { "API Error (New Customer): ${registrationResult.message}" }
                        _uiState.value = AuthScreenUiState.RegistrationError(
                            registrationResult.error.asUiText(registrationResult.message)
                        )
                    }
                }
            } else {
                val loginResult = authRepository.requestTokenForExistingCustomer(
                    deviceMacAddress = deviceMacAddress,
                    clientIp = clientIp,
                    deviceName = deviceName
                )
                when (loginResult) {
                    is ApiResult.Success -> {
                        Logger.d { "API Success (Login ): ${loginResult.data}" }
                        val loginTokenResponse = loginResult.data
                        setupPusherConnection(
                            loginTokenResponse.code,
                            deviceMacAddress,
                            clientIp,
                            deviceName
                        )

                        _uiState.value = AuthScreenUiState.LoginRequestCode(
                            code = loginTokenResponse.code,
                        )
                    }

                    is ApiResult.Error -> {
                        Logger.e { "API Error (Login fallback): ${loginResult.message}" }
                        _uiState.value = AuthScreenUiState.LoginWithSmartphoneError(
                            loginResult.error.asUiText(loginResult.message)
                        )
                    }
                }
            }
        }
    }


    private fun setupPusherConnection(
        code: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ) {
        Logger.i { "📡 Initiating Pusher connection..." }
        Logger.d { "Connection parameters - Code: $code, Device: $deviceName, MAC: $deviceMacAddress, IP: $clientIp" }

        connectionStartTime = System.currentTimeMillis()
        isConnected = false

        // Initialize Pusher with complete configuration
        val options = PusherOptions().apply {
            setCluster("eu")  // Add cluster configuration
            setHost("reverb-connect.nortv.xyz")
            setWsPort(80)     // Add WebSocket port
            setWssPort(443)   // Add secure WebSocket port
            setUseTLS(true)   // forceTLS: true

            // Improved authorizer without runBlocking
            setAuthorizer { channelName, socketId ->
                Logger.d { "🔐 Authorizing channel: $channelName with socketId: $socketId" }

                // Create a CompletableFuture for async auth
                val future = CompletableFuture<String>()

                viewModelScope.launch {
                    try {
                        Logger.d { "📞 Calling auth endpoint for channel: $channelName" }
                        val response =
                            broadcastingService.authenticateChannel(channelName, socketId)

                        if (response.isSuccessful && response.body() != null) {
                            val authString = response.body()!!.auth
                            Logger.d { "✅ Auth successful for channel: $channelName" }
                            future.complete(authString)
                        } else {
                            val errorMsg = "Auth request failed with code: ${response.code()}"
                            Logger.e { "❌ $errorMsg" }
                            future.completeExceptionally(Exception(errorMsg))
                        }
                    } catch (e: Exception) {
                        Logger.e { "❌ Auth failed for channel $channelName: ${e.message}" }
                        future.completeExceptionally(e)
                    }
                }

                // Wait for the future to complete (with timeout)
                try {
                    future.get(10, java.util.concurrent.TimeUnit.SECONDS)
                } catch (e: Exception) {
                    Logger.e { "Auth timeout or error: ${e.message}" }
                    throw e
                }
            }

        }

        pusher = Pusher("fYUAeE6atNyRV4SXR542Cnct", options)

        // Connection state listener
        pusher?.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                val connectionTime = if (connectionStartTime > 0) {
                    System.currentTimeMillis() - connectionStartTime
                } else 0

                Logger.i { "🔗 Pusher connection state: ${change.currentState}" }

                when (change.currentState) {
                    ConnectionState.CONNECTED -> {
                        isConnected = true
                        Logger.i { "✅ Pusher connection established for code : $code with TLS after ${connectionTime}ms" }
                        _connectionStatus.value = "Connected"

                        // Subscribe to channel AFTER connection is established
                        subscribeToChannel(code, deviceMacAddress, clientIp, deviceName)
                    }

                    ConnectionState.CONNECTING -> {
                        Logger.i { "🔄 Pusher connecting..." }
                        _connectionStatus.value = "Connecting..."
                    }

                    ConnectionState.DISCONNECTED -> {
                        isConnected = false
                        Logger.w { "🔌 Pusher disconnected" }
                        _connectionStatus.value = "Disconnected"
                    }

                    ConnectionState.RECONNECTING -> {
                        Logger.i { "🔄 Pusher reconnecting..." }
                        _connectionStatus.value = "Reconnecting..."
                    }

                    ConnectionState.DISCONNECTING -> {
                        Logger.i { "🔄 Pusher disconnecting..." }
                        _connectionStatus.value = "Disconnecting..."
                    }

                    else -> {
                        Logger.d { "🔄 Pusher state: ${change.currentState}" }
                    }
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                isConnected = false
                Logger.e { "❌ Pusher connection error: $message${code?.let { ", Code: $it" } ?: ""}" }
                e?.let {
                    Logger.e { "Exception: ${it.message}" }
                    Logger.e { "Exception type: ${it::class.simpleName}" }
                }
                _connectionStatus.value = "Connection Error: $message"

                // Update UI state to show error
                _uiState.value =
                    AuthScreenUiState.Error(UiText.DynamicString("Connection failed: $message"))
            }
        })

        Logger.i { "📤 Pusher client configured and connecting..." }
    }

    private fun subscribeToChannel(
        code: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ) {
        val channelName = "login.request.$code"
        Logger.i { "📡 Subscribing to channel: $channelName" }

        try {
            // Subscribe to the channel
            channel = pusher?.subscribe(channelName)

            // Bind to subscription succeeded event
            channel?.bind("pusher:subscription_succeeded") { event ->
                Logger.i { "✅ Successfully subscribed to $channelName" }
                val statusMessage =
                    "[Subscribed to login.request.$code] - [Listening for request-confirmed event]"
                Logger.i { statusMessage }
                _connectionStatus.value = statusMessage
                _uiState.value = AuthScreenUiState.Connected(statusMessage)
            }

            // Bind to subscription error event
            channel?.bind("pusher:subscription_error") { event ->
                Logger.e { "❌ Subscription error for $channelName: ${event.data}" }
                _uiState.value =
                    AuthScreenUiState.Error(UiText.DynamicString("Failed to subscribe to channel"))
            }

            // IMPORTANT: Bind to your custom events
            channel?.bind("request-confirmed") { event ->
                Logger.i { "🎉 Login request confirmed!" }
                Logger.d { "Event data: ${event.data}" }

                // Parse the event data if needed
                try {
                    handleLoginConfirmed(code, deviceMacAddress, clientIp, deviceName)
                } catch (e: Exception) {
                    Logger.e { "Failed to process login confirmation: ${e.message}" }
                    _uiState.value =
                        AuthScreenUiState.Error(UiText.DynamicString("Failed to process login confirmation"))
                }
            }

            // You might also want to bind to other events like "login-denied", "login-timeout", etc.
            channel?.bind("login-denied") { event ->
                Logger.w { "⚠️ Login request denied" }
                Logger.d { "Denial reason: ${event.data}" }
                _uiState.value =
                    AuthScreenUiState.Error(UiText.DynamicString("Login request was denied"))
            }

            channel?.bind("login-timeout") { event ->
                Logger.w { "⏱️ Login request timed out" }
                _uiState.value =
                    AuthScreenUiState.Error(UiText.DynamicString("Login request timed out"))
            }

        } catch (e: Exception) {
            Logger.e { "💥 Error subscribing to channel: ${e.message}" }
            _uiState.value =
                AuthScreenUiState.Error(UiText.DynamicString("Failed to setup channel subscription"))
        }
    }

    override fun onCleared() {
        super.onCleared()

        val uptime = if (connectionStartTime > 0) {
            System.currentTimeMillis() - connectionStartTime
        } else 0

        Logger.i { "🧹 AuthScreenViewModel cleanup initiated" }
        Logger.d { "Connection uptime before cleanup: ${uptime}ms" }

        // Disconnect Pusher client gracefully
        pusher?.let { pusherClient ->
            Logger.i { "🔌 Closing Pusher connection gracefully" }
            pusherClient.disconnect()
        } ?: Logger.d { "Pusher was already null during cleanup" }

        isConnected = false
        pusher = null
        channel = null
        Logger.i { "✅ AuthScreenViewModel cleanup completed" }
    }

    private fun handleLoginConfirmed(
        code: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ) {
        Logger.i { "🔄 Processing login confirmation for code: $code" }

        viewModelScope.launch {
            try {
                Logger.d { "📞 Calling loginWithAccessCode..." }
                val token = loginWithAccessCode(code, deviceMacAddress, clientIp, deviceName)
                val tokenValue = token.collect {
                    val userToken = it?.token
                    if (userToken != null) {
                        _uiState.update {
                            AuthScreenUiState.Success(
                                userToken,
                                message = "Login successful"
                            )
                        }
                        userRepository.saveUserToken(userToken)
                        _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }
                    }


                }
                // Close Pusher connection after successful login
                Logger.i { "🔌 Closing Pusher connection after successful login" }
                pusher?.disconnect()
            } catch (e: Exception) {
                Logger.e { "💥 Authentication failed: ${e.message}" }
                Logger.e { "Exception type: ${e::class.simpleName}" }
                _uiState.value =
                    AuthScreenUiState.Error(UiText.DynamicString("Authentication failed: ${e.message}"))
            }
        }
    }

    fun loginWithTv(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Flow<TokenResponse?> = flow {
        _uiState.update { AuthScreenUiState.Loading }
        val response = authRepository.loginWithTv(
            identifier = identifier,
            password = password,
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )

        when (response) {
            is ApiResult.Success -> {
                Logger.d { "API Success (Login with tv): ${response.data}" }
                val responseData = response.data
                emit(responseData)
                _uiState.value = AuthScreenUiState.Success(
                    body = responseData,
                    message = "Login with tv successful"
                )
                _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }
            }

            is ApiResult.Error -> {
                Logger.e { "API Error (Login with tv): ${response.message}" }
                _uiState.value = AuthScreenUiState.LoginWithTvError(
                    response.error.asUiText(response.message)
                )
            }
        }
    }

    fun loginWithAccessCode(
        accessCode: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ): Flow<TokenResponse?> = flow {
        _uiState.update { AuthScreenUiState.Loading }
        val response = authRepository.loginWithAccessCode(
            accessCode = accessCode,
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )

        when (response) {
            is ApiResult.Success -> {
                Logger.d { "API Success (Login with access code): ${response.data}" }
                val responseData = response.data
                emit(responseData)
                _uiState.value = AuthScreenUiState.Success(
                    body = responseData,
                    message = "Login successful"
                )
                _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }
            }

            is ApiResult.Error -> {
                Logger.e { "API Error (Login with access code): ${response.message}" }
                _uiState.value = AuthScreenUiState.LoginWithAccessCodeError(
                    response.error.asUiText(response.message)
                )
            }
        }
    }

    fun loginWithTvAndStoreToken(
        identifier: String,
        password: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
        onTokenReceived: suspend (String) -> Unit
    ) {
        viewModelScope.launch {
            loginWithTv(
                identifier = identifier,
                password = password,
                deviceMacAddress = deviceMacAddress,
                clientIp = clientIp,
                deviceName = deviceName
            ).collect { tokenResponse ->
                tokenResponse?.token?.let { token -> onTokenReceived(token) }
            }
        }
    }

    fun loginWithAccessCodeAndStoreToken(
        accessCode: String,
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
        onTokenReceived: suspend (String) -> Unit
    ) {
        viewModelScope.launch {
            loginWithAccessCode(
                accessCode = accessCode,
                deviceMacAddress = deviceMacAddress,
                clientIp = clientIp,
                deviceName = deviceName
            ).collect { tokenResponse ->
                tokenResponse?.token?.let { token -> onTokenReceived(token) }
            }
        }
    }

    fun getUser(identifier: String): Flow<UserResponse?> = flow {
        _uiState.update { AuthScreenUiState.Loading }
        delay(2000)
        val token = userRepository.userToken.firstOrNull() ?: return@flow emit(null)
        val response = authRepository.getUser(
            token = token,
            identifier = identifier
        )

        when (response) {
            is ApiResult.Error -> {
                Logger.e { "API Error (Get user): ${response.message}" }
                _uiState.value = AuthScreenUiState.Error(
                    response.error.asUiText(response.message)
                )
                emit(null)
                return@flow
            }

            is ApiResult.Success -> {
                Logger.d { "API Success (Get user): ${response.data}" }
                _uiState.value = AuthScreenUiState.Success(
                    body = response.data,
                    message = "User found"
                )
                emit(response.data)
                return@flow
            }

        }

    }

    fun clearEvent() {
        _uiEvent.value = null
    }
}

sealed interface UserEvent {
    data class Error(val error: UiText) : UserEvent
}