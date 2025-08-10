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
import com.google.wiltv.presentation.screens.auth.AuthScreenUiState.Idle
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import dagger.hilt.android.lifecycle.HiltViewModel
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

    data class CodeGenerated(
        val registrationCode: String,
        val loginRequestCode: String
    ) : AuthScreenUiState()

    data class Connected(val message: String) : AuthScreenUiState()
    data class Success<T>(val body: T, val message: String) : AuthScreenUiState()
    data class Error(val message: String) : AuthScreenUiState()
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
        deviceName: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthScreenUiState.Loading

                val registrationTokenResponse = authRepository.requestTokenForNewCustomer(
                    deviceMacAddress = deviceMacAddress,
                    clientIp = clientIp,
                    deviceName = deviceName
                ).body()!!

                val loginTokenResponse = authRepository.requestTokenForExistingCustomer(
                    deviceMacAddress = deviceMacAddress,
                    clientIp = clientIp,
                    deviceName = deviceName
                ).body()!!

                // Log the code here
                Logger.d { "User Identifier: ${registrationTokenResponse.identifier}" }

                _uiState.value = AuthScreenUiState.CodeGenerated(
                    registrationCode = registrationTokenResponse.identifier.padStart(6, '0'),
                    loginRequestCode = loginTokenResponse.code.padStart(6, '0')
                )

                setupPusherConnection(
                    registrationTokenResponse.identifier.padStart(6, '0'),
                    deviceMacAddress,
                    clientIp,
                    deviceName
                )
                setupPusherConnection(
                    loginTokenResponse.code.padStart(6, '0'),
                    deviceMacAddress,
                    clientIp,
                    deviceName
                )
            } catch (e: Exception) {
                _uiState.value = AuthScreenUiState.Error("Failed to initialize: ${e.message}")
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
                _uiState.value = AuthScreenUiState.Error("Connection failed: $message")
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
                _uiState.value = AuthScreenUiState.Error("Failed to subscribe to channel")
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
                    _uiState.value = AuthScreenUiState.Error("Failed to process login confirmation")
                }
            }

            // You might also want to bind to other events like "login-denied", "login-timeout", etc.
            channel?.bind("login-denied") { event ->
                Logger.w { "⚠️ Login request denied" }
                Logger.d { "Denial reason: ${event.data}" }
                _uiState.value = AuthScreenUiState.Error("Login request was denied")
            }

            channel?.bind("login-timeout") { event ->
                Logger.w { "⏱️ Login request timed out" }
                _uiState.value = AuthScreenUiState.Error("Login request timed out")
            }

        } catch (e: Exception) {
            Logger.e { "💥 Error subscribing to channel: ${e.message}" }
            _uiState.value = AuthScreenUiState.Error("Failed to setup channel subscription")
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
                _uiState.value = AuthScreenUiState.Error("Authentication failed: ${e.message}")
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
        response.collect { responsePair ->
            val code = responsePair.first
            val body = responsePair.second

            when (code) {
                201 -> {
                    _uiState.update {
                        AuthScreenUiState.Success(
                            body = body,
                            message = "Login successful"
                        )
                    }
                    emit(body)
                    _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }
                }

                400 -> {
                    _uiState.update { AuthScreenUiState.Error("Invalid input") }
                    emit(null)

                }

                404 -> {
                    _uiState.update { AuthScreenUiState.Error("User not found please check identifer or password") }
                    _uiEvent.update { AuthScreenUiEvent.NavigateToRegister }
                    emit(null)
                }

                422 -> {
                    _uiState.update { AuthScreenUiState.Error("Validation error") }
                    emit(null)
                }
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
        response.collect { responsePair ->
            val code = responsePair.first
            val body = responsePair.second

            when (code) {
                201 -> {
                    _uiState.update {
                        AuthScreenUiState.Success(
                            body = body,
                            message = "Login successful"
                        )
                    }
                    emit(body)
                    _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }
                }

                400 -> {
                    _uiState.update { AuthScreenUiState.Error("Invalid input") }
                    emit(null)

                }

                404 -> {
                    _uiState.update { AuthScreenUiState.Error("User not found please check accessCode") }
                    _uiEvent.update { AuthScreenUiEvent.NavigateToRegister }
                    emit(null)
                }

                422 -> {
                    _uiState.update { AuthScreenUiState.Error("Validation error") }
                    emit(null)
                }
            }
        }
    }


    fun getUser(identifier: String): Flow<UserResponse?> = flow {
        _uiState.update { AuthScreenUiState.Loading }
        val token = userRepository.userToken.firstOrNull() ?: return@flow emit(null)
        val response = authRepository.getUser(
            token = token,
            identifier = identifier
        )

        response.collect { userResponse ->
            when (userResponse) {
                null -> {
                    _uiState.update { AuthScreenUiState.Error("User not found") }
                    emit(userResponse)
                }

                else -> {
                    _uiState.update {
                        AuthScreenUiState.Success(
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


data class LoginRequestEvent(
    val loginRequest: String
)