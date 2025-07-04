package com.google.jetstream.presentation.screens.profile

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.google.jetstream.R
import com.google.jetstream.presentation.common.Error
import com.google.jetstream.presentation.common.Loading
import com.google.jetstream.presentation.screens.dashboard.rememberChildPadding
import com.google.jetstream.presentation.theme.ComposeTvTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileScreen(
    @FloatRange(from = 0.0, to = 1.0)
    sidebarWidthFraction: Float = 0.32f,
    viewModel: ProfileScreenViewModel = hiltViewModel(),
    logOutOnClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is ProfileScreenUiState.Ready -> {
            if (s.user == null) return

            val childPadding = rememberChildPadding()
            val profileNavController = rememberNavController()

            val backStack by profileNavController.currentBackStackEntryAsState()
            val currentDestination =
                remember(backStack?.destination?.route) { backStack?.destination?.route }
            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current
            var isLeftColumnFocused by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp, top = 48.dp)
                    .padding(horizontal = childPadding.start, vertical = childPadding.top)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(fraction = sidebarWidthFraction)
                        .verticalScroll(rememberScrollState())
                        .fillMaxHeight()
                        .onFocusChanged {
                            isLeftColumnFocused = it.hasFocus
                        }
                        .focusRestorer()
                        .focusGroup(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileScreens.entries.forEachIndexed { index, profileScreen ->
                        // TODO: make this dense list item
                        key(index) {
                            ListItem(
                                trailingContent = {
                                    Icon(
                                        profileScreen.icon,
                                        modifier = Modifier
                                            .padding(vertical = 2.dp)
                                            .padding(start = 4.dp)
                                            .size(20.dp),
                                        contentDescription = stringResource(
                                            id = R.string.profile_screen_listItem_icon_content_description,
                                            profileScreen.tabTitle
                                        )
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        text = profileScreen.tabTitle,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                selected = currentDestination == profileScreen.name,
                                onClick = { focusManager.moveFocus(FocusDirection.Right) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (index == 0) Modifier.focusRequester(focusRequester)
                                        else Modifier
                                    )
                                    .onFocusChanged {
                                        if (it.isFocused && currentDestination != profileScreen.name) {
                                            profileNavController.navigate(profileScreen()) {
                                                currentDestination?.let { nnCurrentDestination ->
                                                    popUpTo(nnCurrentDestination) {
                                                        inclusive = true
                                                    }
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                scale = ListItemDefaults.scale(focusedScale = 1f),
                                colors = ListItemDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.inverseSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.inverseSurface
                                        .copy(alpha = 0.4f),
                                    selectedContentColor = MaterialTheme.colorScheme.surface,
                                ),
                                shape = ListItemDefaults.shape(shape = MaterialTheme.shapes.extraSmall)
                            )
                        }
                    }
                }

                var selectedLanguageIndex by rememberSaveable { mutableIntStateOf(0) }
                var isSubtitlesChecked by rememberSaveable { mutableStateOf(true) }
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .onPreviewKeyEvent {
                            if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                                // Using 'while' because AccountsScreen has a grid that has multiple items
                                // in a row for which we would need to press D-Pad Left multiple times
                                while (!isLeftColumnFocused) {
                                    focusManager.moveFocus(FocusDirection.Left)
                                }
                                return@onPreviewKeyEvent true
                            }
                            false
                        },
                    navController = profileNavController,
                    startDestination = ProfileScreens.Accounts(),
                    builder = {
                        composable(ProfileScreens.Accounts()) {
                            AccountsSection(
                                userEmailAddress = s.user.email,
                                logOutOnClick = logOutOnClick
                            )
                        }
//                        composable(ProfileScreens.About()) {
//                            AboutSection()
//                        }
                        composable(ProfileScreens.Subtitles()) {
                            SubtitlesSection(
                                isSubtitlesChecked = isSubtitlesChecked,
                                onSubtitleCheckChange = { isSubtitlesChecked = it }
                            )
                        }
                        composable(ProfileScreens.Language()) {
                            LanguageSection(
                                selectedIndex = selectedLanguageIndex,
                                onSelectedIndexChange = { selectedLanguageIndex = it }
                            )
                        }
                        composable(ProfileScreens.SearchHistory()) {
                            SearchHistorySection()
                        }
                        composable(ProfileScreens.HelpAndSupport()) {
                            HelpAndSupportSection()
                        }
                    }
                )
            }
        }

        is ProfileScreenUiState.Loading -> Loading(modifier = Modifier.fillMaxSize())
        is ProfileScreenUiState.Error -> Error(modifier = Modifier.fillMaxSize())
    }

}

@Preview(device = Devices.TV_1080p)
@Composable
fun ProfileScreenPreview() {
    ComposeTvTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            ProfileScreen(
                logOutOnClick = {}
            )
        }
    }
}
