package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button as TvButton
import androidx.tv.material3.ButtonDefaults as TvButtonDefaults
import androidx.tv.material3.Text

@Composable
fun LeftWelcomePanel(
    selectedAuthOption: AuthRoute,
    onAuthOptionSelected: (AuthRoute) -> Unit,
    tvLoginFirstFieldFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        // Welcome Title
        Text(
            text = "Welcome To WilTV",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Other Auth Options with consistent spacing
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthOptionItem(
                text = "Register an account",
                isSelected = selectedAuthOption == AuthRoute.REGISTER,
                onClick = { onAuthOptionSelected(AuthRoute.REGISTER) }
            )

            AuthOptionItem(
                text = "Login using access code",
                isSelected = selectedAuthOption == AuthRoute.LOGIN_WITH_ACCESS_CODE,
                onClick = { onAuthOptionSelected(AuthRoute.LOGIN_WITH_ACCESS_CODE) }
            )

            AuthOptionItem(
                text = "Login with smartphone",
                isSelected = selectedAuthOption == AuthRoute.LOGIN_WITH_SMART_PHONE,
                onClick = { onAuthOptionSelected(AuthRoute.LOGIN_WITH_SMART_PHONE) }
            )

            AuthOptionItem(
                text = "Login using the TV",
                isSelected = selectedAuthOption == AuthRoute.LOGIN_WITH_TV,
                onClick = { onAuthOptionSelected(AuthRoute.LOGIN_WITH_TV) },
                rightFocusRequester = if (selectedAuthOption == AuthRoute.LOGIN_WITH_TV) tvLoginFirstFieldFocusRequester else null
            )
        }
    }
}

@Composable
fun AuthOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    rightFocusRequester: FocusRequester? = null
) {
    TvButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .let { modifier ->
                if (rightFocusRequester != null) {
                    modifier.focusProperties { right = rightFocusRequester }
                } else {
                    modifier
                }
            },
        colors = TvButtonDefaults.colors(
            containerColor = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.1f),
            contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
            focusedContainerColor = if (isSelected) Color(0xFFA855F7) else Color(0xFFBD9DF1),
            focusedContentColor = Color.White,
            pressedContainerColor = if (isSelected) Color(0xFFA855F7) else Color(0xFFBD9DF1),
            pressedContentColor = Color.White
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 9.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}