package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.tv.material3.Text

@Composable
fun LoginWithSmartphone(
    onSubmit: (String, String) -> Unit,
) {

    Box {
        Column {
            Text("Login with smart phone")
        }
    }

}