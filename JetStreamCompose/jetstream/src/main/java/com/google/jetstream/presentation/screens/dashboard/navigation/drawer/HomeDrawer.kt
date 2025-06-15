package com.google.jetstream.presentation.screens.dashboard.navigation.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import com.google.jetstream.presentation.screens.Screens
import com.google.jetstream.presentation.screens.dashboard.TopBarTabs


@Composable
fun HomeDrawer(
    content: @Composable () -> Unit,
    selectedTab: String? = null,
    onScreenSelected: ((screen: Screens) -> Unit)?
) {
    val closeDrawerWidth = 80.dp
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = { _ ->
            Column(
                Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxHeight()
                    .padding(10.dp)
                    .selectableGroup(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(
                    8.dp, alignment = Alignment.CenterVertically
                ),
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                TopBarTabs.forEachIndexed { index, item ->
                    NavigationRow(
                        item = item,
                        isSelected = selectedTab == item.name,
                        onScreenSelected = {
                            drawerState.setValue(DrawerValue.Closed)
                            onScreenSelected?.invoke(item)
                        })
                }
                Spacer(modifier = Modifier.weight(1f))
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
        selected = isSelected, enabled = enabled,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            ),
            selectedContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = {
            onScreenSelected?.invoke(item)
        }, leadingContent = {
            Icon(
                imageVector = item.tabIcon ?: return@NavigationDrawerItem,
                contentDescription = item.name
            )
        }) {
        Text(item.name)
    }
}

@Preview
@Composable
fun HomeDrawerPrev() {
    HomeDrawer(content = {
        Text(text = "Hello World")
    }, onScreenSelected = null)
}
