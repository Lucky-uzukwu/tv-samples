package com.google.wiltv.presentation.screens.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.google.wiltv.presentation.screens.Screens
import com.google.wiltv.presentation.utils.handleDPadKeyEvents


val TopBarTabs = Screens.entries.toList().filter { it.isTabItem }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DashboardSideBar(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    screens: List<Screens> = TopBarTabs,
    onTabSelected: (screen: Screens) -> Unit,
    contentFocusRequester: FocusRequester, // Pass this from your main dashboard
) {
    var isFocused by remember { mutableStateOf(false) }

    val tabFocusRequesters = remember {
        screens.associateWith { FocusRequester() }
    }

    val animatedWidth by animateDpAsState(
        targetValue = if (isFocused) 200.dp else 60.dp,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )

    LazyColumn(
        modifier = modifier
            .width(animatedWidth)
            .fillMaxHeight()
            .background(Color.Black)
            .onFocusChanged { focusState ->
                isFocused = focusState.hasFocus
                // Restore focus to the selected tab when sidebar gains focus
                if (focusState.hasFocus && selectedTabIndex in screens.indices) {
                    tabFocusRequesters[screens[selectedTabIndex]]?.requestFocus()
                }
            }
            .focusGroup()
            .padding(vertical = 16.dp),
    ) {
        itemsIndexed(screens) { index, screen ->
            SideBarTab(
                screen = screen,
                isSelected = index == selectedTabIndex,
                focusRequester = tabFocusRequesters[screen] ?: FocusRequester(),
                showText = isFocused,
                onClick = { onTabSelected(screen) },
                onActivate = {
                    // When user activates a tab, move focus to content
                    contentFocusRequester.requestFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

@Composable
fun SideBarTab(
    screen: Screens,
    isSelected: Boolean,
    showText: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTabFocused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isTabFocused = focusState.isFocused
            }
            .focusable()
            .handleDPadKeyEvents(
                onEnter = {
                    onClick()
                    onActivate() // Move focus to content area
                },
                onRight = {
                    onActivate() // Move focus to content area
                },
            )
            .background(
                if (isTabFocused) Color(0xFF555555) else Color.Transparent
            )
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        screen.tabIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected || isTabFocused) Color.White else Color(0xFFCCCCCC)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = screen.name,
                color = if (isSelected || isTabFocused) Color.White else Color(0xFFCCCCCC),
                fontSize = 20.sp,
                modifier = Modifier.animateContentSize()
            )
        }
    }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            focusRequester.requestFocus()
        }
    }
}