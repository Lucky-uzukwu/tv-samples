package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import android.view.KeyEvent
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.presentation.utils.handleDPadKeyEvents

@Composable
fun LoginWithTv(
    modifier: Modifier = Modifier,
    onSubmit: (emailAddress: String, password: String) -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    firstFieldFocusRequester: FocusRequester? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val passwordFieldFocusRequester = remember { FocusRequester() }
    val submitButtonFocusRequester = remember { FocusRequester() }

    // Form validation
    val isFormValid = email.isNotBlank() && password.isNotBlank()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color(0xFF2A2A2A), shape = RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "WiLTV", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sign in to your account",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedLabelColor = Color.White,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            label = { Text("E-Mail Adresse") },
            placeholder = { Text("Email/Access Code") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    passwordFieldFocusRequester.requestFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .let { modifier ->
                    firstFieldFocusRequester?.let { focusRequester ->
                        modifier.focusRequester(focusRequester)
                    } ?: modifier
                },

            )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        //TODO: look into https://developer.android.com/develop/ui/compose/quick-guides/content/show-hide-password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedLabelColor = Color.White,
                focusedLabelColor = Color.White,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    submitButtonFocusRequester.requestFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFieldFocusRequester)
                .onKeyEvent { keyEvent ->
                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK
                    ) {
                        firstFieldFocusRequester?.requestFocus()
                        true
                    } else false
                },
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(24.dp))

        // render error if exist
        if (isError) {
            Text(
                text = errorMessage ?: "Unknown Error",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                if (isFormValid) onSubmit(email, password)
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(submitButtonFocusRequester)
                .handleDPadKeyEvents(
                    onEnter = {
                        if (isFormValid) onSubmit(email, password)
                    } // Explicit Enter/D-PAD CENTER handling
                )
                .onKeyEvent { keyEvent ->
                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK
                    ) {
                        passwordFieldFocusRequester.requestFocus()
                        true
                    } else false
                },
            colors = ButtonDefaults.colors(
                containerColor = if (isFormValid) Color(0xFF2196F3) else Color.Gray, // Blue when valid, gray when disabled
                contentColor = Color.White,
                focusedContainerColor = if (isFormValid) Color(0xFF1976D2) else Color.DarkGray, // Darker blue when focused and valid
                focusedContentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            scale = ButtonDefaults.scale(focusedScale = 1.1f) // 10% scale increase when focused
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Submit")
            }
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color(0xFF2196F3)
            )
        }
    }

}