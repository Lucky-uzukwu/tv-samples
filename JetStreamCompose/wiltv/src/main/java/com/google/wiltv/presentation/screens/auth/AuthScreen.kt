package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.wiltv.data.entities.User
import com.google.wiltv.state.UserStateHolder
import com.google.wiltv.util.DeviceNetworkInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(
    userStateHolder: UserStateHolder = hiltViewModel(),
    authScreenViewModel: AuthScreenViewModel = hiltViewModel(),
    onNavigateToDashboard: () -> Unit,
) {
    val context = LocalContext.current
    val macAddress = remember { DeviceNetworkInfo.getMacAddress(context) }
    val deviceName = remember { DeviceNetworkInfo.getDeviceName(context) }
    val clientIp = remember { DeviceNetworkInfo.getIPAddress() }

    val uiState by authScreenViewModel.uiState.collectAsState()
    val uiEvent by authScreenViewModel.uiEvent.collectAsState()

    var identifierOrEmail by remember { mutableStateOf("") }

    var registrationCode by remember { mutableStateOf("") }
    var loginRequestCode by remember { mutableStateOf("") }

    var registrationErrorMessage by remember { mutableStateOf<String?>(null) }
    var loginWithSmartphoneErrorMessage by remember { mutableStateOf<String?>(null) }
    var loginWithTvErrorMessage by remember { mutableStateOf<String?>(null) }
    var loginWithAccessCodeErrorMessage by remember { mutableStateOf<String?>(null) }


    // Current selected auth option
    var selectedAuthOption by remember { mutableStateOf(AuthRoute.REGISTER) }

    // Focus requester for the first form field in LoginWithTv
    val tvLoginFirstFieldFocusRequester = remember { FocusRequester() }


    // Effect to initialize activation
    LaunchedEffect(Unit) {
        authScreenViewModel.initializeActivation(
            deviceMacAddress = macAddress,
            clientIp = clientIp,
            deviceName = deviceName,
            isNewCustomer = true
        )
        delay(1000)

        authScreenViewModel.initializeActivation(
            deviceMacAddress = macAddress,
            clientIp = clientIp,
            deviceName = deviceName,
            isNewCustomer = false
        )
    }

    // Effect to handle navigation based on UI events
    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToLogin && uiState is AuthScreenUiState.Success<*>) {
            val identifier = when {
                registrationCode.isNotBlank() && identifierOrEmail.isBlank() && loginRequestCode.isBlank() -> registrationCode
                loginRequestCode.isNotBlank() && identifierOrEmail.isBlank() && registrationCode.isBlank() -> loginRequestCode
                identifierOrEmail.isNotBlank() -> identifierOrEmail
                else -> null
            }

            identifier?.let { id ->
                authScreenViewModel.getUser(identifier = id)
                    .collectLatest { user ->
                        user?.let {
                            userStateHolder.updateUser(
                                User(
                                    id = user.identifier, // Use identifier as consistent user ID
                                    identifier = user.identifier,
                                    name = user.name,
                                    email = user.email,
                                    profilePhotoPath = user.profilePhotoPath,
                                    profilePhotoUrl = user.profilePhotoUrl,
                                    clientIp = clientIp,
                                    deviceName = deviceName,
                                    deviceMacAddress = macAddress,
                                )
                            )
                        }
                    }
            }

            onNavigateToDashboard()
            authScreenViewModel.clearEvent()
        }
    }


    // Effect to update UI state based on the UI state
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthScreenUiState.RegistrationCode -> {
                registrationCode = (uiState as AuthScreenUiState.RegistrationCode).code!!
            }

            is AuthScreenUiState.LoginRequestCode -> {
                loginRequestCode = (uiState as AuthScreenUiState.LoginRequestCode).code!!
            }

            is AuthScreenUiState.RegistrationError -> {
                registrationErrorMessage =
                    (uiState as AuthScreenUiState.RegistrationError).message.asString(context)
            }

            is AuthScreenUiState.LoginWithSmartphoneError -> {
                loginWithSmartphoneErrorMessage =
                    (uiState as AuthScreenUiState.LoginWithSmartphoneError).message.asString(
                        context
                    )
            }

            is AuthScreenUiState.LoginWithAccessCodeError -> {
                loginWithAccessCodeErrorMessage =
                    (uiState as AuthScreenUiState.LoginWithAccessCodeError).message.asString(
                        context
                    )
            }

            is AuthScreenUiState.LoginWithTvError -> {
                loginWithTvErrorMessage =
                    (uiState as AuthScreenUiState.LoginWithTvError).message.asString(
                        context
                    )
            }

            else -> {}
        }
    }


    fun handleSubmitForTvLogin(
        emailAddress: String,
        password: String,
    ) {
        identifierOrEmail = emailAddress
        authScreenViewModel.loginWithTvAndStoreToken(
            identifier = emailAddress,
            password = password,
            deviceMacAddress = macAddress,
            clientIp = clientIp,
            deviceName = deviceName,
            onTokenReceived = { token -> userStateHolder.updateToken(token) }
        )
    }

    fun handleSubmitForAccessCodeLogin(
        accessCode: String,
    ) {
        identifierOrEmail = accessCode
        authScreenViewModel.loginWithAccessCodeAndStoreToken(
            accessCode = accessCode,
            deviceMacAddress = macAddress,
            clientIp = clientIp,
            deviceName = deviceName,
            onTokenReceived = { token -> userStateHolder.updateToken(token) }
        )
    }


    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Left Panel - Welcome Section
        LeftWelcomePanel(
            selectedAuthOption = selectedAuthOption,
            onAuthOptionSelected = { option -> selectedAuthOption = option },
            tvLoginFirstFieldFocusRequester = tvLoginFirstFieldFocusRequester,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f)
        )

        // Right Panel - Content Section
        RightContentPanel(
            selectedAuthOption = selectedAuthOption,
            handleLoginWithTvOnSubmit = { emailAddress, password ->
                handleSubmitForTvLogin(
                    emailAddress,
                    password
                )
            },
            handleLoginWithAccessCodeOnSubmit = { accessCode ->
                handleSubmitForAccessCodeLogin(accessCode)
            },
            isTvLoginLoading = uiState is AuthScreenUiState.Loading,
            registrationErrorMessage = registrationErrorMessage,
            loginWithSmartphoneErrorMessage = loginWithSmartphoneErrorMessage,
            loginWithTvErrorMessage = loginWithTvErrorMessage,
            loginWithAccessCodeErrorMessage = loginWithAccessCodeErrorMessage,
            tvLoginFirstFieldFocusRequester = tvLoginFirstFieldFocusRequester,
            generatedRegistrationCode = registrationCode,
            generatedLoginCode = loginRequestCode,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f),
        )
    }

}

// Preview for testing the UI
@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onNavigateToDashboard = {},
    )
}