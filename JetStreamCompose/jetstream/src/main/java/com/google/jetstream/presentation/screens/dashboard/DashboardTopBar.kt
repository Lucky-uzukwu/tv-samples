package com.google.jetstream.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import com.google.jetstream.data.util.StringConstants
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.theme.JetStreamCardShape
import com.google.jetstream.presentation.theme.primaryLight
import com.google.jetstream.presentation.utils.handleDPadKeyEvents
import com.google.jetstream.presentation.utils.occupyScreenSize

val TopBarTabs = Screens.entries.toList().filter { it.isTabItem }

// +1 for ProfileTab
val TopBarFocusRequesters = List(size = TopBarTabs.size + 1) { FocusRequester() }

private const val PROFILE_SCREEN_INDEX = 5

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DashboardTopBar(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    screens: List<Screens> = TopBarTabs,
    clearFilmSelection: () -> Unit,
    focusRequesters: List<FocusRequester> = remember { TopBarFocusRequesters },
    onScreenSelection: (screen: Screens) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .wrapContentHeight()
    ) {
        // Overlay your top bar content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .focusRestorer(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                modifier = Modifier
                    .size(32.dp)
                    .focusRequester(focusRequesters[0])
                    .semantics {
                        contentDescription =
                            StringConstants.Composable.ContentDescription.UserAvatar
                    },
                selected = selectedTabIndex == PROFILE_SCREEN_INDEX,
                onClick = {
                    onScreenSelection(Screens.Profile)
                }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                var isTabRowFocused by remember { mutableStateOf(false) }
                Spacer(modifier = Modifier.width(20.dp))
                TabRow(
                    modifier = Modifier.onFocusChanged {
                        clearFilmSelection()
                        isTabRowFocused = it.isFocused || it.hasFocus
                    },
                    selectedTabIndex = selectedTabIndex,
                    indicator = { tabPositions, _ ->
                        if (selectedTabIndex >= 0) {
                            DashboardTopBarItemIndicator(
                                currentTabPosition = tabPositions[selectedTabIndex],
                                anyTabFocused = isTabRowFocused,
                                shape = JetStreamCardShape
                            )
                        }
                    },
                    separator = { Spacer(modifier = Modifier) }
                ) {
                    screens.forEachIndexed { index, screen ->
                        key(index) {
                            Tab(
                                onClick = { focusManager.moveFocus(FocusDirection.Down) },
                                modifier = Modifier
                                    .height(32.dp)
                                    .focusRequester(focusRequesters[index + 1]),
                                selected = index == selectedTabIndex,
                                onFocus = { onScreenSelection(screen) },
                                colors = TabDefaults.pillIndicatorTabColors(
                                    focusedContentColor = primaryLight,
                                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                            ) {
                                if (screen.tabIcon != null) {
                                    Icon(
                                        imageVector = screen.tabIcon,
                                        modifier = Modifier.padding(4.dp),
                                        contentDescription = StringConstants.Composable.ContentDescription.DashboardSearchButton,
                                        tint = Color.White
                                    )
                                } else {
                                    Text(
                                        modifier = Modifier
                                            .occupyScreenSize()
                                            .padding(horizontal = 16.dp),
                                        text = screen(),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
