package com.google.wiltv.presentation.screens.auth

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.DeviceInfo
import co.touchlab.kermit.Logger
import com.google.gson.Gson
import com.google.wiltv.data.network.TokenResponse
import com.google.wiltv.data.network.UserResponse
import com.google.wiltv.data.repositories.AuthRepository
import com.google.wiltv.data.repositories.UserRepository
import com.google.wiltv.presentation.screens.auth.AuthScreenUiState.Idle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class AuthScreenUiState {
    object Idle : AuthScreenUiState()
    object Loading : AuthScreenUiState()

    data class CodeGenerated(
        val code: String,
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
) : ViewModel() {

    // Expose screen UI state
    private val _uiState = MutableStateFlow<AuthScreenUiState>(Idle)
    val uiState: StateFlow<AuthScreenUiState> = _uiState

    private val _connectionStatus = MutableStateFlow("Connecting...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val gson = Gson()
    private var connectionStartTime: Long = 0
    private var heartbeatJob: kotlinx.coroutines.Job? = null
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
                val userResponse = createUser(
                    deviceMacAddress = deviceMacAddress,
                    clientIp = clientIp,
                    deviceName = deviceName
                )

                // Log the code here
                Logger.d { "User Identifier: ${userResponse.identifier}" }

                _uiState.value = AuthScreenUiState.CodeGenerated(
                    code = userResponse.identifier,
                )

                setupWebSocketConnection(
                    userResponse.identifier,
                    deviceMacAddress,
                    clientIp,
                    deviceName
                )
            } catch (e: Exception) {
                _uiState.value = AuthScreenUiState.Error("Failed to initialize: ${e.message}")
            }
        }
    }

    private suspend fun createUser(
        deviceMacAddress: String,
        clientIp: String,
        deviceName: String
    ): UserResponse = authRepository.requestTokenForCustomer(
        deviceMacAddress = deviceMacAddress,
        clientIp = clientIp,
        deviceName = deviceName
    ).body()!!

    private fun setupWebSocketConnection(
        code: String, deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ) {
        val websocketUrl =
            "wss://reverb-connect.nortv.xyz/app/fYUAeE6atNyRV4SXR542Cnct?protocol=7&client=js&version=8.4.0&flash=false"

        Logger.i { "📡 Initiating WebSocket connection..." }
        Logger.d { "WebSocket URL: $websocketUrl" }
        Logger.d { "Connection parameters - Code: $code, Device: $deviceName, MAC: $deviceMacAddress, IP: $clientIp" }

        connectionStartTime = System.currentTimeMillis()
        isConnected = false

        val request = Request.Builder()
            .url(websocketUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val connectionTime = System.currentTimeMillis() - connectionStartTime
                isConnected = true
                _connectionStatus.value = "Connected"

                Logger.i { "✅ WebSocket connection established successfully" }
                Logger.d { "Connection time: ${connectionTime}ms" }
                Logger.d { "Response code: ${response.code}" }
                Logger.d { "Protocol: ${response.protocol}" }

                // Subscribe to the channel
                val subscribeMessage = mapOf(
                    "event" to "pusher:subscribe",
                    "data" to mapOf("channel" to "login.request.$code")
                )
                val subscribeJson = gson.toJson(subscribeMessage)

                Logger.i { "📤 Subscribing to channel: login.request.$code" }
                Logger.d { "Subscribe message: $subscribeJson" }

                webSocket.send(subscribeJson)

                // Start heartbeat
                startHeartbeat(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Logger.d { "📥 Incoming WebSocket message (${text.length} chars): $text" }

                try {
                    val message = gson.fromJson(text, Map::class.java)
                    val event = message["event"] as? String
                    val channel = message["channel"] as? String
                    val data = message["data"]

                    Logger.i { "📨 Processing event: '$event'" }
                    if (channel != null) {
                        Logger.d { "Channel: $channel" }
                    }
                    if (data != null) {
                        Logger.d { "Data payload: $data" }
                    }

                    when (event) {
                        "pusher:subscription_succeeded" -> {
                            Logger.i { "✅ Successfully subscribed to channel: login.request.$code" }
                            _connectionStatus.value =
                                "Subscribed to login.request.$code - Listening for request-confirmed event"
                        }

                        "pusher_internal:subscription_succeeded" -> {
                            Logger.i { "🔧 Internal Pusher subscription succeeded" }
                            val channelData = (data as? Map<*, *>)?.get("channel")
                            Logger.d { "Internal subscription channel: $channelData" }
                            if (channelData != null) {
                                Logger.d { "Internal subscription data: $data" }
                            }
                        }

                        "request-confirmed" -> {
                            val dataString = message["data"] as? String
                            Logger.i { "🎯 Login confirmation received!" }
                            Logger.d { "Confirmation data: $dataString" }
                            _connectionStatus.value = "Event Received: $dataString"
                            handleLoginConfirmed(code, deviceMacAddress, clientIp, deviceName)
                        }

                        "pusher:error" -> {
                            val errorData = message["data"] as? Map<*, *>
                            val errorMessage =
                                errorData?.get("message") as? String ?: "Unknown error"
                            val errorCode = errorData?.get("code") as? Number
                            Logger.e { "❌ WebSocket error received - Code: $errorCode, Message: $errorMessage" }
                            _connectionStatus.value = "WebSocket Error: $errorMessage"
                        }

                        "pusher:connection_established" -> {
                            Logger.i { "🔗 Pusher connection established" }
                            val socketId = (data as? Map<*, *>)?.get("socket_id")
                            Logger.d { "Socket ID: $socketId" }
                        }

                        "pusher:pong" -> {
                            Logger.d { "💓 Heartbeat pong received" }
                        }

                        else -> {
                            Logger.w { "⚠️ Unhandled event type: '$event'" }
                        }
                    }
                } catch (e: Exception) {
                    Logger.e { "💥 Error parsing WebSocket message: ${e.message}" }
                    Logger.d { "Failed message content: $text" }
                    _connectionStatus.value = "Error parsing message: ${e.message}"
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                val errorMessage = t.message ?: "Unknown error"

                Logger.e { "💥 WebSocket connection failed: $errorMessage" }
                Logger.e { "Exception type: ${t::class.simpleName}" }

                response?.let {
                    Logger.e { "Response code: ${it.code}" }
                    Logger.e { "Response message: ${it.message}" }
                    Logger.d { "Response headers: ${it.headers}" }
                }

                // Categorize common network errors
                when {
                    t is java.net.UnknownHostException -> {
                        Logger.e { "🌐 Network issue: Unable to resolve host" }
                        _connectionStatus.value = "Connection Error: Unable to resolve host"
                    }

                    t is java.net.ConnectException -> {
                        Logger.e { "🔌 Network issue: Connection refused" }
                        _connectionStatus.value = "Connection Error: Connection refused"
                    }

                    t is java.net.SocketTimeoutException -> {
                        Logger.e { "⏱️ Network issue: Connection timeout" }
                        _connectionStatus.value = "Connection Error: Timeout"
                    }

                    t is javax.net.ssl.SSLException -> {
                        Logger.e { "🔒 SSL/TLS issue: ${t.message}" }
                        _connectionStatus.value = "Connection Error: SSL issue"
                    }

                    else -> {
                        Logger.e { "❓ Unrecognized connection error: $errorMessage" }
                        _connectionStatus.value = "Connection Error: $errorMessage"
                    }
                }

                // Stop heartbeat on failure
                stopHeartbeat()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                val uptime = if (connectionStartTime > 0) {
                    System.currentTimeMillis() - connectionStartTime
                } else 0

                Logger.i { "🔌 WebSocket connection closed" }
                Logger.d { "Close code: $code" }
                Logger.d { "Close reason: $reason" }
                Logger.d { "Connection uptime: ${uptime}ms" }

                // Interpret common close codes
                val closeReason = when (code) {
                    1000 -> "Normal closure"
                    1001 -> "Going away"
                    1002 -> "Protocol error"
                    1003 -> "Unsupported data"
                    1005 -> "No status received"
                    1006 -> "Abnormal closure"
                    1007 -> "Invalid frame payload data"
                    1008 -> "Policy violation"
                    1009 -> "Message too big"
                    1010 -> "Mandatory extension"
                    1011 -> "Internal server error"
                    1015 -> "TLS handshake failure"
                    else -> "Unknown close code"
                }

                Logger.i { "📋 Close code meaning: $closeReason" }
                _connectionStatus.value = "Connection Closed: $closeReason"

                // Stop heartbeat on close
                stopHeartbeat()
            }


        })
    }

    private fun startHeartbeat(webSocket: WebSocket) {
        Logger.d { "💓 Starting WebSocket heartbeat..." }

        heartbeatJob = viewModelScope.launch {
            while (isConnected) {
                try {
                    delay(30_000) // Send ping every 30 seconds

                    if (isConnected) {
                        val pingMessage =
                            mapOf("event" to "pusher:ping", "data" to emptyMap<String, Any>())
                        val pingJson = gson.toJson(pingMessage)

                        Logger.d { "💓 Sending heartbeat ping" }
                        Logger.v { "Ping message: $pingJson" }

                        webSocket.send(pingJson)

                        Logger.d { "💓 Heartbeat - Connection healthy (uptime: ${System.currentTimeMillis() - connectionStartTime}ms)" }
                    }
                } catch (e: Exception) {
                    Logger.e { "💥 Heartbeat error: ${e.message}" }
                    break
                }
            }
        }
    }

    private fun stopHeartbeat() {
        Logger.d { "🔴 Stopping WebSocket heartbeat" }
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    override fun onCleared() {
        super.onCleared()

        val uptime = if (connectionStartTime > 0) {
            System.currentTimeMillis() - connectionStartTime
        } else 0

        Logger.i { "🧹 AuthScreenViewModel cleanup initiated" }
        Logger.d { "Connection uptime before cleanup: ${uptime}ms" }

        // Stop heartbeat first
        stopHeartbeat()

        // Close WebSocket connection gracefully
        webSocket?.let { ws ->
            Logger.i { "🔌 Closing WebSocket connection gracefully" }
            ws.close(1000, "ViewModel cleared")
        } ?: Logger.d { "WebSocket was already null during cleanup" }

        isConnected = false
        Logger.i { "✅ AuthScreenViewModel cleanup completed" }
    }

    private fun handleLoginConfirmed(
        code: String, deviceMacAddress: String,
        clientIp: String,
        deviceName: String,
    ) {
        Logger.i { "🔄 Processing login confirmation for code: $code" }

        viewModelScope.launch {
            try {
                Logger.d { "📞 Calling loginWithAccessCode..." }
                val token = loginWithAccessCode(code, deviceMacAddress, clientIp, deviceName)
                val tokenValue = token.firstOrNull()?.token

                if (tokenValue != null) {
                    Logger.i { "✅ Authentication successful - Token received" }
                    Logger.d { "Token preview: ${tokenValue.take(10)}..." }
                } else {
                    Logger.w { "⚠️ Authentication completed but no token received" }
                }

                _uiState.update {
                    AuthScreenUiState.Success(
                        tokenValue,
                        message = "Login successful"
                    )
                }

                _uiEvent.update { AuthScreenUiEvent.NavigateToLogin }


                // Close WebSocket after successful login
                Logger.i { "🔌 Closing WebSocket after successful login" }
                webSocket?.close(1000, "Login successful")

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
