import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.Text
import co.touchlab.kermit.Logger
import com.google.jetstream.R
import com.google.jetstream.presentation.screens.auth.AuthScreenUiEvent
import com.google.jetstream.presentation.screens.auth.AuthScreenViewModel
import com.google.jetstream.presentation.screens.auth.components.AccessCodeAndInfoText
import com.google.jetstream.presentation.screens.auth.components.AccessCodeTextInputAndContinueButton
import com.google.jetstream.util.DeviceNetworkInfo
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
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

    var accessCode = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        authScreenViewModel.requestTokenForCustomer(
            deviceMacAddress = macAddress,
            clientIp = clientIp,
            deviceName = deviceName,
        )
    }

    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToLogin) {
            onNavigateToLogin()
            authScreenViewModel.clearEvent()
        }
    }

    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToRegister) {
            onNavigateToRegister()
            authScreenViewModel.clearEvent()
        }
    }

    fun authenticateCustomer(accessCode: String) {
        authScreenViewModel.viewModelScope.launch {
            try {
                authScreenViewModel.getCustomer(accessCode)
            } catch (e: Exception) {
                Logger.e("Authentication failed: ${e.message}")
            }
        }
    }

    if (uiState.isRequestTokenForCustomerLoading) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Main Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                        .size(48.dp),
                    color = Color(0xFFFFA736)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.5f),
                    blendMode = BlendMode.Darken
                )
            )

            // Main Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF2A2A2A).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Unlimited Movies,\nTV Shows and More",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 30.sp
                        )

                        AccessCodeAndInfoText(
                            accessCode = uiState.generatedAccessCode
                        )

                        AccessCodeTextInputAndContinueButton(
                            accessCode = accessCode,
                            accessCodeError = uiState.accessCodeError,
                            uiState = uiState,
                            onContinueButtonClicked = { authenticateCustomer(accessCode.value) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
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