package com.google.jetstream.presentation.screens.auth.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

@Composable
fun AccessCodeAndInfoText(
    accessCode: String
) {
    Text("Access Code", color = Color.White.copy(alpha = 0.7f))
    Text(
        text = accessCode,
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Provide this code to your developer account.\nOnce activated, you'll set a password and login.",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp
    )
}
