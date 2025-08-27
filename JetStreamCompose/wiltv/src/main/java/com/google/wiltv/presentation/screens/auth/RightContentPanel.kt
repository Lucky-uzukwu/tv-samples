package com.google.wiltv.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

@Composable
fun RightContentPanel(
    selectedAuthOption: AuthRoute,
    generatedRegistrationCode: String,
    generatedLoginCode: String,
    handleLoginWithTvOnSubmit: (String, String) -> Unit,
    handleLoginWithAccessCodeOnSubmit: (String) -> Unit,
    isTvLoginLoading: Boolean,
    registrationErrorMessage: String?,
    loginWithSmartphoneErrorMessage: String?,
    loginWithTvErrorMessage: String?,
    loginWithAccessCodeErrorMessage: String?,
    tvLoginFirstFieldFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A1A))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        when (selectedAuthOption) {
            AuthRoute.REGISTER -> {
                RegisterAccount(
                    errorMessage = registrationErrorMessage,
                    accessCode = generatedRegistrationCode,
                )
            }

            AuthRoute.LOGIN_WITH_TV -> {
                LoginWithTv(
                    onSubmit = handleLoginWithTvOnSubmit,
                    isLoading = isTvLoginLoading,
                    errorMessage = loginWithTvErrorMessage,
                    firstFieldFocusRequester = tvLoginFirstFieldFocusRequester
                )
            }

            AuthRoute.LOGIN_WITH_ACCESS_CODE -> {
                LoginWithAccessCode(
                    onSubmit = handleLoginWithAccessCodeOnSubmit,
                    isLoading = isTvLoginLoading,
                    errorMessage = loginWithAccessCodeErrorMessage,
                    firstFieldFocusRequester = tvLoginFirstFieldFocusRequester
                )
            }

            AuthRoute.LOGIN_WITH_SMART_PHONE -> {
                LoginWithSmartphone(
                    accessCode = generatedLoginCode,
                    errorMessage = loginWithSmartphoneErrorMessage
                )
            }
        }
    }
}