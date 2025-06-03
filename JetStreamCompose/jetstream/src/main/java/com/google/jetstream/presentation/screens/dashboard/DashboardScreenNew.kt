package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Text
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.theme.AppTheme

@Composable
fun DashboardScreenNew(
    isComingBackFromDifferentScreen: Boolean,
    resetIsComingBackFromDifferentScreen: () -> Unit = {},
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val navController = rememberNavController()

    var currentDestination: String? by remember { mutableStateOf(null) }
    val contentFocusRequester = remember { FocusRequester() }

    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    BackPressHandledArea(
        onBackPressed = {}
    ) {
        val tabs = listOf("Home", "Movies", "Series", "Live", "Settings")
        var selectedTab by remember { mutableStateOf(0) }

        // Background with Netflix-like gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1F1F1F), Color(0xFF000000))
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Sidebar
                DashboardSideBar(
                    selectedTabIndex = selectedTab,
                    onTabSelected = { screen ->
                        selectedTab = TopBarTabs.indexOf(screen)
                        // Navigate to the corresponding screen
                        navController.navigate(screen()) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    contentFocusRequester = contentFocusRequester
                )

                Spacer(Modifier.width(16.dp))

                // Body content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(contentFocusRequester)
                        .focusable()
                        .background(Color.Black)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = 48.dp)
                    ) {
                        composable(Screens.Home()) { BodyContent("Home Content") }
                        composable(Screens.Movies()) { BodyContent("Movies Content") }
                        composable(Screens.Shows()) { BodyContent("Series Content") }
                        composable(Screens.Categories()) { BodyContent("Categories Content") }
                        composable(Screens.Search()) { BodyContent("Search Content") }
                        composable(Screens.Profile()) { BodyContent("Profile Content") }
                    }
                }
            }
        }
    }
}

@Composable
fun BodyContent(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun BackPressHandledArea(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) =
    Box(
        modifier = Modifier
            .onPreviewKeyEvent {
                if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                    onBackPressed()
                    true
                } else {
                    false
                }
            }
            .then(modifier),
        content = content
    )

@Composable
private fun Body(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) =
    Loading(modifier = modifier)

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun DashboardScreenNewPreview() {
    DashboardScreenNew(
        isComingBackFromDifferentScreen = false,
    )
}