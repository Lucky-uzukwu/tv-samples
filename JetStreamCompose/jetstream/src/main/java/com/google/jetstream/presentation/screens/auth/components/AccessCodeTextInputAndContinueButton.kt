package com.google.jetstream.presentation.screens.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.google.jetstream.presentation.screens.auth.AuthScreenUiState

@Composable
fun AccessCodeTextInputAndContinueButton(
    accessCode: MutableState<String>,
    maxLength: Int = 6,
    uiState: AuthScreenUiState,
    onContinueButtonClicked: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Access Code",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium

            )
            Text("*", color = Color.Red.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Login with an existing code",
                color = Color.Gray.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value = accessCode.value,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    accessCode.value = newValue
                }
            },
            textStyle = TextStyle(
                color = Color.White, // White input text
                fontSize = 16.sp
            ),
            placeholder = {
                Text(
                    text = "e.g. 123456",
                    color = Color.White.copy(alpha = 0.7f) // Lighter placeholder
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),

            )
        if (uiState.accessCodeError != null) {
            Text(
                text = uiState.accessCodeError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onContinueButtonClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA726)
            )
        ) {
            Text(
                "continue",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
