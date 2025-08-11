package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.google.wiltv.presentation.common.QRCodeDisplay

@Composable
fun LoginWithSmartphone(
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    accessCode: String,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage == null) {
            Text(
                text = "Scan the QR Code below to login",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // QR Code for registration
            QRCodeDisplay(
                data = "https://nortv.xyz/account/login?code=$accessCode",
                size = 200
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Or visit the following link to register:",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "https://nortv.xyz/account/login?code=$accessCode",
                fontSize = 14.sp,
                color = Color(0xFFA855F7),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
//            verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Or provide the access code ",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Text(
                    text = accessCode,
                    fontSize = 14.sp,
                    color = Color(0xFFA855F7),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " to customer support to activate ",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = "your account",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
        }
    }
}