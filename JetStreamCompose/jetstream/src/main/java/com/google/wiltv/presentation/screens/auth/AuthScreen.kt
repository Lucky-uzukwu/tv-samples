import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.Text
import com.google.wiltv.data.entities.User
import com.google.wiltv.presentation.common.QRCodeDisplay
import com.google.wiltv.presentation.screens.auth.AuthRoute
import com.google.wiltv.presentation.screens.auth.AuthScreenUiEvent
import com.google.wiltv.presentation.screens.auth.AuthScreenUiStateNew
import com.google.wiltv.presentation.screens.auth.AuthScreenViewModel
import com.google.wiltv.presentation.screens.auth.LoginWithAccessCode
import com.google.wiltv.presentation.screens.auth.LoginWithSmartphone
import com.google.wiltv.presentation.screens.auth.LoginWithTv
import com.google.wiltv.state.UserStateHolder
import com.google.wiltv.util.DeviceNetworkInfo
import kotlinx.coroutines.launch
import androidx.tv.material3.Button as TvButton
import androidx.tv.material3.ButtonDefaults as TvButtonDefaults

@Composable
fun AuthScreen(
    userStateHolder: UserStateHolder = hiltViewModel(),
    authScreenViewModel: AuthScreenViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val macAddress = remember { DeviceNetworkInfo.getMacAddress(context) }
    val deviceName = remember { DeviceNetworkInfo.getDeviceName(context) }
    val clientIp = remember { DeviceNetworkInfo.getIPAddress() }

    val uiState by authScreenViewModel.uiState.collectAsState()
    val uiEvent by authScreenViewModel.uiEvent.collectAsState()

    var identifierOrEmail by remember { mutableStateOf("") }

    // Focus requester for the first form field in LoginWithTv
    val tvLoginFirstFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToLogin && uiState is AuthScreenUiStateNew.Success<*>) {
            if (identifierOrEmail.isNotBlank()) {
                authScreenViewModel.getUser(identifier = identifierOrEmail).collect {
                    it?.let {
                        userStateHolder.updateUser(
                            User(
                                identifier = it.identifier,
                                name = it.name,
                                email = it.email,
                                profilePhotoPath = it.profilePhotoPath,
                                profilePhotoUrl = it.profilePhotoUrl,
                                clientIp = clientIp,
                                deviceName = deviceName,
                                deviceMacAddress = macAddress,
                            )
                        )
                    }
                }
            }
            onNavigateToLogin()
            authScreenViewModel.clearEvent()
        }
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
            isTvLoginLoading = uiState is AuthScreenUiStateNew.Loading,
            isTvLoginWithTvError = uiState is AuthScreenUiStateNew.Error,
            errorMessage = (uiState as? AuthScreenUiStateNew.Error)?.message,
            tvLoginFirstFieldFocusRequester = tvLoginFirstFieldFocusRequester,
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f)
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
                RegisterContent()
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
                    onSubmit = { _, _ -> }
                )
            }
        }
    }
}

@Composable
fun RegisterContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Scan the QR Code below to register an account",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // QR Code for registration
        QRCodeDisplay(
            data = "https://nortv.xyz/account/register?accessCode=955271",
            size = 200
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Or visit the following link to register:",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "https://nortv.xyz/account/register?accessCode=955271",
            fontSize = 14.sp,
            color = Color(0xFFA855F7),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
//            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Or provide the access code ",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
            Text(
                text = "955271",
                fontSize = 14.sp,
                color = Color(0xFFA855F7),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " to customer support to activate ",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "your account",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            overflow = TextOverflow.Ellipsis,
        )
    }
}


// Preview for testing the UI
@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
//    JetStreamTheme {
    AuthScreen(
        onNavigateToLogin = {},
        onNavigateToRegister = {},
    )
//    }
}