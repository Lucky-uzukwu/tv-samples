import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.data.entities.User
import com.google.jetstream.presentation.screens.auth.AuthRoute
import com.google.jetstream.presentation.screens.auth.AuthScreenUiEvent
import com.google.jetstream.presentation.screens.auth.AuthScreenUiStateNew
import com.google.jetstream.presentation.screens.auth.AuthScreenViewModel
import com.google.jetstream.presentation.screens.auth.components.AuthContentPager
import com.google.jetstream.presentation.screens.auth.components.AuthTabsMenu
import com.google.jetstream.state.UserStateHolder
import com.google.jetstream.util.DeviceNetworkInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    var email by remember { mutableStateOf("") }

    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToLogin && uiState is AuthScreenUiStateNew.Success<*>) {
            authScreenViewModel.getUser(identifier = email).collect {
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
            onNavigateToLogin()
            authScreenViewModel.clearEvent()
        }
    }

//    LaunchedEffect(uiEvent) {
//        if (uiEvent is AuthScreenUiEvent.NavigateToRegister) {
//            userStateHolder.updateUser(
//                User(
//                    id = uiState.customerData!!.id,
//                    identifier = uiState.customerData!!.identifier,
//                    name = uiState.customerData!!.name,
//                    email = uiState.customerData!!.email,
//                    profilePhotoPath = uiState.customerData?.profilePhotoPath,
//                    profilePhotoUrl = uiState.customerData?.profilePhotoUrl,
//                    clientIp = clientIp,
//                    deviceName = deviceName,
//                    deviceMacAddress = macAddress,
//                )
//            )
//            onNavigateToRegister()
//            authScreenViewModel.clearEvent()
//        }
//    }


//    if (uiState.isRequestTokenForCustomerLoading) {
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Background Image
//            Image(
//                painter = painterResource(id = R.drawable.auth_background),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//
//            // Main Content Overlay
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//                    .verticalScroll(rememberScrollState()),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                CircularProgressIndicator(
//                    modifier = Modifier
//                        .align(Alignment.CenterHorizontally)
//                        .padding(16.dp)
//                        .size(48.dp),
//                    color = Color(0xFFFFA736)
//                )
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    } else {
//
//        Box(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Background Image
//            Image(
//                painter = painterResource(id = R.drawable.auth_background),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize(),
//                colorFilter = ColorFilter.tint(
//                    Color.Black.copy(alpha = 0.5f),
//                    blendMode = BlendMode.Darken
//                )
//            )
//
//            // Main Content Overlay
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//                    .verticalScroll(rememberScrollState()),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(
//                            color = Color(0xFF2A2A2A).copy(alpha = 0.85f),
//                            shape = RoundedCornerShape(16.dp)
//                        )
//                        .padding(16.dp)
//                ) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
//                    ) {
//                        Text(
//                            text = "Unlimited Movies,\nTV Shows and More",
//                            color = Color.White,
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.Medium,
//                            textAlign = TextAlign.Center,
//                            lineHeight = 30.sp
//                        )
//
//                        AccessCodeAndInfoText(
//                            accessCode = uiState.generatedAccessCode
//                        )
//
//                        AccessCodeTextInputAndContinueButton(
//                            accessCode = accessCode,
//                            accessCodeError = uiState.accessCodeError,
//                            uiState = uiState,
//                            onContinueButtonClicked = { authenticateCustomer(accessCode.value) }
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    }

    // List of tab titles
    val tabs = AuthRoute.entries.toList()
    // Pager state to manage the current page
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    // Coroutine scope for animations
    val coroutineScope = rememberCoroutineScope()

    fun handleSubmitForTvLogin(
        emailAddress: String,
        password: String,
    ) {
        email = emailAddress
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {
            // Vertical tabs in a Column
            AuthTabsMenu(pagerState, tabs, coroutineScope)

            // Content area with HorizontalPager
            AuthContentPager(
                pagerState,
                tabs,
                handleLoginWithTvOnSubmit = { emailAddress, password ->
                    handleSubmitForTvLogin(
                        emailAddress,
                        password
                    )
                },
                isLoginWithTvLoading = uiState is AuthScreenUiStateNew.Loading,
                isLoginWithTvError = uiState is AuthScreenUiStateNew.Error,
                errorMessage = (uiState as? AuthScreenUiStateNew.Error)?.message
            )
        }

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