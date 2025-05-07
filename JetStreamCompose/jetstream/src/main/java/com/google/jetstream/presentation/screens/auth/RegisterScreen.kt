import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.jetstream.R
import com.google.jetstream.presentation.screens.auth.AuthScreenUiEvent
import com.google.jetstream.presentation.screens.auth.AuthScreenViewModel
import kotlinx.coroutines.runBlocking

@Composable
fun RegisterScreen(
    viewModel: AuthScreenViewModel,
    onSubmitSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()

    var accessCode = remember { mutableStateOf(uiState.userInputedAccessCode) }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val email = remember { mutableStateOf(uiState.customerData?.email ?: "") }
    val name = remember { mutableStateOf(uiState.customerData?.name ?: "") }

    LaunchedEffect(uiEvent) {
        if (uiEvent is AuthScreenUiEvent.NavigateToDashboard) {
            onSubmitSuccess()
            viewModel.clearEvent()
        }
    }

    fun registerCustomer() = runBlocking {
        viewModel.registerCustomer(
            password = password.value,
            password_confirmation = confirmPassword.value,
            email = email.value.toString(),
            name = name.value.toString()
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(
                Color.Black.copy(alpha = 0.6f),
                blendMode = BlendMode.Darken
            )
        )

        // Main Content Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Wil TV",
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Register Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Set a password for your account. Use your access code and password to log in later.",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            val fields = listOf(
                "Access Code" to accessCode,
                "Name" to name,
                "Email" to email,
                "Password" to password,
                "Confirm Password" to confirmPassword
            )

            for ((label, state) in fields) {
                if (label.contains("Password")) {
                    OutlinedTextField(
                        value = state.value,
                        onValueChange = { state.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        label = { Text(label, color = Color.White) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedTextColor = Color.Gray,
                            disabledTextColor = Color.Gray,
                        )
                    )
                } else if (label.contains("Access Code")) {
                    OutlinedTextField(
                        value = state.value,
                        enabled = false,
                        onValueChange = { state.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        label = { Text(label, color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedTextColor = Color.Gray,
                            disabledTextColor = Color.Gray,
                            disabledContainerColor = Color.Transparent
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = state.value,
                        onValueChange = { state.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        label = { Text(label, color = Color.White) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedTextColor = Color.Gray,
                            disabledTextColor = Color.Gray,
                        )
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { registerCustomer() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBA133)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
