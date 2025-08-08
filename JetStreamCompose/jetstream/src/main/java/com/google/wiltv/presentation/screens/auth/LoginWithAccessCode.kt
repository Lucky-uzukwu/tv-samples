package com.google.wiltv.presentation.screens.auth

import android.view.KeyEvent
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.wiltv.presentation.utils.handleDPadKeyEvents

@Composable
fun LoginWithAccessCode(
    onSubmit: (accessCode: String) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    firstFieldFocusRequester: FocusRequester? = null
) {

    var accessCode by remember { mutableStateOf("") }

    val submitButtonFocusRequester = remember { FocusRequester() }

    // Form validation
    val isFormValid = accessCode.isNotBlank()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "WiLTV", style = MaterialTheme.typography.headlineLarge, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sign in to your account",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Email Field
        OutlinedTextField(
            value = accessCode,
            onValueChange = { accessCode = it },
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedLabelColor = Color.Black,
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black,
                unfocusedPlaceholderColor = Color.Black,
                focusedPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
            ),
            label = { Text("Access code") },
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
                .let { modifier ->
                    firstFieldFocusRequester?.let { focusRequester ->
                        modifier.focusRequester(focusRequester)
                    } ?: modifier
                },

            )

        Spacer(modifier = Modifier.height(16.dp))
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
                if (isFormValid) onSubmit(accessCode)
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(submitButtonFocusRequester)
                .handleDPadKeyEvents(
                    onEnter = {
                        if (isFormValid) onSubmit(accessCode)
                    } // Explicit Enter/D-PAD CENTER handling
                )
                .onKeyEvent { keyEvent ->
                    keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP &&
                            keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK
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