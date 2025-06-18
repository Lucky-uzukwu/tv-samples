package com.google.jetstream.presentation.screens.dashboard.navigation.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.NavigationDrawerItemScale
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.dashboard.TopBarTabs


@Composable
fun HomeDrawer(
    content: @Composable () -> Unit,
    navController: NavController = rememberNavController(),
    onScreenSelected: ((screen: Screens) -> Unit)?
) {
    val closeDrawerWidth = 65.dp
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var (selectedTab, setSelectedTab) = remember { mutableStateOf<String?>(Screens.Home()) }

    LaunchedEffect(key1 = Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectedTab = destination.route ?: return@addOnDestinationChangedListener
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = { _ ->
            Column(
                Modifier
                    .background(
                        color = if (drawerState.currentValue == DrawerValue.Open) {
                            Color.Transparent.copy(alpha = 0.7f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .fillMaxHeight()
                    .padding(top = 90.dp, start = 4.dp, end = 4.dp, bottom = 90.dp)
                    .selectableGroup(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(
                    1.dp, alignment = Alignment.CenterVertically
                ),
            ) {
                TopBarTabs.forEachIndexed { index, item ->
                    NavigationRow(
                        item = item,
                        isSelected = selectedTab == item.name,
                        onScreenSelected = {
                            setSelectedTab(item.name)
                            drawerState.setValue(DrawerValue.Closed)
                            onScreenSelected?.invoke(item)
                        })
                }
            }
        }, scrimBrush = Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.surface, Color.Transparent
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = closeDrawerWidth)
        ) {
            content()
        }
    }
}

@Composable
fun NavigationDrawerScope.NavigationRow(
    item: Screens,
    isSelected: Boolean,
    enabled: Boolean = true,
    onScreenSelected: ((screen: Screens) -> Unit)?
) {
    NavigationDrawerItem(
        selected = isSelected,
        enabled = enabled,
        shape = NavigationDrawerItemDefaults.shape(
            shape = RoundedCornerShape(40)
        ),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            ),
            selectedContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        scale = NavigationDrawerItemScale(
            focusedScale = 0.7f,
            pressedScale = 1f,
            scale = 0.7f,
            selectedScale = 0.7f,
            disabledScale = 1f,
            focusedSelectedScale = 0.9f,
            focusedDisabledScale = 1f,
            pressedSelectedScale = 0.8f,
        ),
        onClick = {
            onScreenSelected?.invoke(item)
        }, leadingContent = {
            Icon(
                imageVector = item.tabIcon ?: return@NavigationDrawerItem,
                modifier = Modifier.size(24.dp),
                contentDescription = item.name
            )
        }) {
        Text(
            item.name,
        )
    }
}

@Preview
@Composable
fun HomeDrawerPrev() {
    HomeDrawer(content = {
        Text(text = "Hello World")
    }, onScreenSelected = null)
}
