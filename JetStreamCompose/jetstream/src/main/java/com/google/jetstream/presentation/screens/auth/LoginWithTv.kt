package com.google.jetstream.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun LoginWithTv(
    modifier: Modifier = Modifier,
    onSubmit: (emailAddress: String, password: String) -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
            value = email,
            onValueChange = { email = it },
            colors = OutlinedTextFieldDefaults.colors().copy(
                unfocusedLabelColor = Color.Black,
                focusedLabelColor = Color.Black,
                cursorColor = Color.Black,
                unfocusedPlaceholderColor = Color.Black,
                focusedPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
            ),
            label = { Text("E-Mail Adresse") },
            modifier = Modifier.fillMaxWidth(),

            )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        //TODO: look into https://developer.android.com/develop/ui/compose/quick-guides/content/show-hide-password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
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

        // Back Button
        Button(
            onClick = { onSubmit(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }

}