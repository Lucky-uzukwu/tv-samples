package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.Text
import com.google.wiltv.data.entities.User
import com.google.wiltv.state.UserStateHolder
import com.google.wiltv.util.DeviceNetworkInfo
import kotlinx.coroutines.launch
import androidx.tv.material3.Button as TvButton
import androidx.tv.material3.ButtonDefaults as TvButtonDefaults

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

    // Focus requester for the first form field in LoginWithTv
    val tvLoginFirstFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToLogin && uiState is AuthScreenUiState.Success<*>) {
            val identifier = when {
                registrationCode.isNotBlank() -> registrationCode
                loginRequestCode.isNotBlank() -> loginRequestCode
                identifierOrEmail.isNotBlank() -> identifierOrEmail
                else -> null
            }

            identifier?.let { id ->
                authScreenViewModel.getUser(identifier = id).collect { user ->
                    user?.let {
                        userStateHolder.updateUser(
                            User(
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

    LaunchedEffect(uiState) {
        if (uiState is AuthScreenUiState.CodeGenerated) {
            registrationCode = (uiState as AuthScreenUiState.CodeGenerated).registrationCode
            loginRequestCode = (uiState as AuthScreenUiState.CodeGenerated).loginRequestCode
        }
    }

    LaunchedEffect(Unit) {
        authScreenViewModel.initializeActivation(
            deviceMacAddress = macAddress,
            clientIp = clientIp,
            deviceName = deviceName
        )
    }


    // Current selected auth option
    var selectedAuthOption by remember { mutableStateOf(AuthRoute.REGISTER) }

    fun handleSubmitForTvLogin(
        emailAddress: String,
        password: String,
    ) {
        identifierOrEmail = emailAddress
        authScreenViewModel.viewModelScope.launch {
            authScreenViewModel.loginWithTv(
                identifier = emailAddress,
                password = password,
                deviceMacAddress = macAddress,
                clientIp = clientIp,
                deviceName = deviceName,
            ).collect {
                it?.token?.let { token -> userStateHolder.updateToken(token) }
            }
        }
    }

    fun handleSubmitForAccessCodeLogin(
        accessCode: String,
    ) {
        identifierOrEmail = accessCode
        authScreenViewModel.viewModelScope.launch {
            authScreenViewModel.loginWithAccessCode(
                accessCode = accessCode,
                deviceMacAddress = macAddress,
                clientIp = clientIp,
                deviceName = deviceName,
            ).collect {
                it?.token?.let { token -> userStateHolder.updateToken(token) }
            }
        }
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
            isTvLoginWithTvError = uiState is AuthScreenUiState.Error,
            errorMessage = (uiState as? AuthScreenUiState.Error)?.message,
            tvLoginFirstFieldFocusRequester = tvLoginFirstFieldFocusRequester,
            generatedRegistrationCode = registrationCode,
            generatedLoginCode = loginRequestCode,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f),
        )
    }

}

@Composable
fun LeftWelcomePanel(
    selectedAuthOption: AuthRoute,
    onAuthOptionSelected: (AuthRoute) -> Unit,
    tvLoginFirstFieldFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        // Welcome Title
        Text(
            text = "Welcome To WilTV",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Other Auth Options with consistent spacing
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthOptionItem(
                text = "Register an account",
                isSelected = selectedAuthOption == AuthRoute.REGISTER,
                onClick = { onAuthOptionSelected(AuthRoute.REGISTER) }
            )

            AuthOptionItem(
                text = "Login using access code",
                isSelected = selectedAuthOption == AuthRoute.LOGIN_WITH_ACCESS_CODE,
                onClick = { onAuthOptionSelected(AuthRoute.LOGIN_WITH_ACCESS_CODE) }
            )

            AuthOptionItem(
                text = "Login with smartphone",
                isSelected = selectedAuthOption == AuthRoute.LOGIN_WITH_SMART_PHONE,
                onClick = { onAuthOptionSelected(AuthRoute.LOGIN_WITH_SMART_PHONE) }
            )

            AuthOptionItem(
                text = "Login using the TV",
                isSelected = selectedAuthOption == AuthRoute.LOGIN_WITH_TV,
                onClick = { onAuthOptionSelected(AuthRoute.LOGIN_WITH_TV) },
                rightFocusRequester = if (selectedAuthOption == AuthRoute.LOGIN_WITH_TV) tvLoginFirstFieldFocusRequester else null
            )
        }
    }
}

@Composable
fun AuthOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    rightFocusRequester: FocusRequester? = null
) {
    TvButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .let { modifier ->
                if (rightFocusRequester != null) {
                    modifier.focusProperties { right = rightFocusRequester }
                } else {
                    modifier
                }
            },
        colors = TvButtonDefaults.colors(
            containerColor = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.1f),
            contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
            focusedContainerColor = if (isSelected) Color(0xFFA855F7) else Color(0xFFBD9DF1),
            focusedContentColor = Color.White,
            pressedContainerColor = if (isSelected) Color(0xFFA855F7) else Color(0xFFBD9DF1),
            pressedContentColor = Color.White
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 9.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RightContentPanel(
    selectedAuthOption: AuthRoute,
    generatedRegistrationCode: String,
    generatedLoginCode: String,
    handleLoginWithTvOnSubmit: (String, String) -> Unit,
    handleLoginWithAccessCodeOnSubmit: (String) -> Unit,
    isTvLoginLoading: Boolean,
    isTvLoginWithTvError: Boolean,
    errorMessage: String?,
    tvLoginFirstFieldFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        when (selectedAuthOption) {
            AuthRoute.REGISTER -> {
                RegisterAccount(
                    accessCode = generatedRegistrationCode,
                )
            }

            AuthRoute.LOGIN_WITH_TV -> {
                LoginWithTv(
                    onSubmit = handleLoginWithTvOnSubmit,
                    isLoading = isTvLoginLoading,
                    isError = isTvLoginWithTvError,
                    errorMessage = errorMessage,
                    firstFieldFocusRequester = tvLoginFirstFieldFocusRequester
                )
            }

            AuthRoute.LOGIN_WITH_ACCESS_CODE -> {
                LoginWithAccessCode(
                    onSubmit = handleLoginWithAccessCodeOnSubmit,
                    isLoading = isTvLoginLoading,
                    isError = isTvLoginWithTvError,
                    errorMessage = errorMessage,
                    firstFieldFocusRequester = tvLoginFirstFieldFocusRequester
                )
            }

            AuthRoute.LOGIN_WITH_SMART_PHONE -> {
                LoginWithSmartphone(
                    accessCode = generatedLoginCode,
                )
            }
        }
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