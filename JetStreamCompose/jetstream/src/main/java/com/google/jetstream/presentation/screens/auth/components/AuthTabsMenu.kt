package com.google.jetstream.presentation.screens.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.presentation.screens.auth.AuthRoute
import com.google.jetstream.presentation.screens.auth.LoginWithAccessCode
import com.google.jetstream.presentation.screens.auth.LoginWithSmartphone
import com.google.jetstream.presentation.screens.auth.LoginWithTv
import com.google.jetstream.presentation.screens.auth.Register
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AuthTabsMenu(
    pagerState: PagerState,
    tabs: List<AuthRoute>,
    coroutineScope: CoroutineScope
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            // Custom vertical tab
            VerticalTab(
                text = title.displayName,
                isSelected = index == pagerState.currentPage,
                onClick = {
                    // Update pager state on tab click
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@Composable
fun AuthContentPager(
    pagerState: PagerState,
    tabs: List<AuthRoute>,
    handleLoginWithTvOnSubmit: (emailAddress: String, password: String) -> Unit,
    isLoginWithTvLoading: Boolean = false,
    isLoginWithTvError: Boolean = false,
    errorMessage: String? = null
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { page ->
        // Content for each tab
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val currentPage = tabs[page]
            when (currentPage) {
                AuthRoute.LOGIN_WITH_TV -> LoginWithTv(
                    onSubmit = handleLoginWithTvOnSubmit,
                    isLoading = isLoginWithTvLoading,
                    isError = isLoginWithTvError,
                    errorMessage = errorMessage
                )

                AuthRoute.LOGIN_WITH_ACCESS_CODE -> LoginWithAccessCode(
                    onSubmit = { _, _ -> }
                )

                AuthRoute.LOGIN_WITH_SMART_PHONE -> LoginWithSmartphone(
                    onSubmit = { _, _ -> }
                )

                AuthRoute.REGISTER -> Register(
                    onSubmit = { _, _ -> }
                )
            }
        }
    }
}

@Composable
fun VerticalTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


