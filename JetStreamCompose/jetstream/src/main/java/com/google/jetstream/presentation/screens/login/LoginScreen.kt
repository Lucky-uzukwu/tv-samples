import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.jetstream.R
import com.google.jetstream.presentation.screens.login.LoginScreenUiEvent.NavigateToDashboard
import com.google.jetstream.presentation.screens.login.LoginScreenViewModel
import com.google.jetstream.state.UserStateHolder
import com.google.jetstream.util.DeviceNetworkInfo
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel = hiltViewModel(),
    userStateHolder: UserStateHolder = hiltViewModel(),
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val deviceMacAddress = remember { DeviceNetworkInfo.getMacAddress(context) }
    val deviceName = remember { DeviceNetworkInfo.getDeviceName(context) }
    val clientIp = remember { DeviceNetworkInfo.getIPAddress() }
    val password = remember { mutableStateOf("") }


    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()

    val userState by userStateHolder.userState.collectAsState()

    val accessCode = remember { mutableStateOf("") }

    LaunchedEffect(userState.user?.accessCode) {
        accessCode.value = userState.user?.accessCode ?: ""
    }

    LaunchedEffect(uiEvent) {
        if (uiEvent is NavigateToDashboard) {
            userStateHolder.updateUser(
                userState.user!!.copy(
                    token = uiState.token
                )
            )
            onSubmitSuccess()
            viewModel.clearEvent()
        }
    }

    fun loginCustomer() = viewModel.viewModelScope.launch {
        viewModel.loginCustomer(
            identifier = accessCode.value,
            deviceMacAddress = deviceMacAddress,
            clientIp = clientIp,
            deviceName = deviceName,
            password = password.value
        )
    }
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
                    .fillMaxSize()
                    .background(Color(0xFF2C1F1A)) // dark background
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Section with Logo
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Transparent),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.auth_background), // Replace with your logo resource
                            contentDescription = "Popcorn Logo",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Wil",
                            fontSize = 36.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "TV",
                            fontSize = 36.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Right Section with Form
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Access Code",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = accessCode.value,
                            onValueChange = { accessCode.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(2.dp, Color.Gray, shape = RoundedCornerShape(14.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedTextColor = Color.Gray,
                                disabledTextColor = Color.Gray,
                            )
                        )
                        if (uiState.accessCodeError != null) {
                            Text(
                                text = uiState.accessCodeError!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password",
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("*", color = Color.Red.copy(alpha = 0.7f))
                        }

                        OutlinedTextField(
                            value = password.value,
                            onValueChange = { password.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(2.dp, Color.Gray, shape = RoundedCornerShape(14.dp)),
                            placeholder = { Text("password") },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.Gray,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedPlaceholderColor = Color.Gray,
                                disabledTextColor = Color.Gray,
                            ),
                        )

                        if (uiState.passwordError != null) {
                            Text(
                                text = uiState.passwordError!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            )
                        }


                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                loginCustomer()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBA133)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("submit", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                        }
                    }
                }
            }
        }

    }


}

// Preview for testing the UI
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            viewModel = hiltViewModel(),
            onSubmitSuccess = {}
        )
    }
}