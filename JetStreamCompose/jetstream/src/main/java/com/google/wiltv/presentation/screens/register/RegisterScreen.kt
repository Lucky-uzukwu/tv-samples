import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.lifecycle.viewModelScope
import com.google.wiltv.R
import com.google.wiltv.presentation.screens.register.RegisterScreenUiEvent.NavigateToDashboard
import com.google.wiltv.presentation.screens.register.RegisterScreenViewModel
import com.google.wiltv.state.UserStateHolder
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: RegisterScreenViewModel = hiltViewModel(),
    userStateHolder: UserStateHolder = hiltViewModel(),
    onSubmitSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState()
    val userState by userStateHolder.userState.collectAsState()

    // Form State
    val accessCode = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val passwordError = remember { mutableStateOf("") }
    val confirmPasswordError = remember { mutableStateOf("") }

    // Initialize Access Code, Email, and Name
    LaunchedEffect(userState.user?.identifier) {
        accessCode.value = userState.user?.identifier ?: ""
        email.value = userState.user?.email ?: ""
        name.value = userState.user?.name ?: ""
    }

    // Handle Navigation Event
    LaunchedEffect(uiEvent) {
        if (uiEvent is NavigateToDashboard) {
            userStateHolder.updateUser(
                userState.user!!.copy(token = uiState.token)
            )
            onSubmitSuccess()
            viewModel.clearEvent()
        }
    }

    // Password Validation Logic
    fun validatePassword(password: String, confirmPassword: String) {
        passwordError.value = if (password.length < 8) {
            "Password must be at least 8 characters"
        } else {
            ""
        }

        confirmPasswordError.value = if (confirmPassword != password) {
            "Passwords do not match"
        } else {
            ""
        }
    }

    fun registerCustomer() {
        validatePassword(password.value, confirmPassword.value)

        if (passwordError.value.isEmpty() && confirmPasswordError.value.isEmpty()) {
            viewModel.viewModelScope.launch {
                viewModel.registerCustomer(
                    password = password.value,
                    password_confirmation = confirmPassword.value,
                    email = email.value.toString(),
                    name = name.value.toString(),
                    identifier = accessCode.value.toString()
                )
            }
        }
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
            // Logo and Title
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Wil TV", fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Bold)
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

            // Access Code (Disabled)
            OutlinedTextField(
                value = accessCode.value,
                enabled = false,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Access Code", color = Color.White) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                    disabledContainerColor = Color.Transparent
                )
            )

            // Name
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Name", color = Color.White) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                )
            )

            // Email
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Email", color = Color.White) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                )
            )

            // Password
            OutlinedTextField(
                value = password.value,
                onValueChange = {
                    password.value = it
                    validatePassword(it, confirmPassword.value)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError.value.isNotEmpty(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                )
            )
            if (passwordError.value.isNotEmpty()) {
                Text(passwordError.value, color = Color.Red, fontSize = 12.sp)
            }

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword.value,
                onValueChange = {
                    confirmPassword.value = it
                    validatePassword(password.value, it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Confirm Password", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError.value.isNotEmpty(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedTextColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                )
            )
            if (confirmPasswordError.value.isNotEmpty()) {
                Text(confirmPasswordError.value, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = { registerCustomer() },
                enabled = password.value.isNotEmpty() && confirmPassword.value.isNotEmpty() &&
                        passwordError.value.isEmpty() && confirmPasswordError.value.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
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
                    Text("Submit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
