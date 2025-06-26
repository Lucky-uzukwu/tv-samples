package com.google.jetstream.presentation.screens.dashboard.navigation.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var (selectedTab, setSelectedTab) = remember { mutableStateOf<String?>(Screens.Home()) }

    LaunchedEffect(key1 = Unit) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            selectedTab = destination.route ?: return@addOnDestinationChangedListener
        }
    }


    ModalNavigationDrawer(
        scrimBrush = Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.background,
                Color.Transparent
            )
        ),
        drawerState = drawerState, drawerContent = { _ ->
            Column(
                Modifier
                    .fillMaxHeight()
                    .selectableGroup(),
                verticalArrangement = Arrangement.Center
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
        }
    ) {
        Box(
            modifier = Modifier
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
    val lineThickness = 2.dp
    val lineColor = MaterialTheme.colorScheme.onSurface

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        NavigationDrawerItem(
            selected = isSelected,
            enabled = enabled,
            colors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = Color.Transparent, // No background for selected item
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = NavigationDrawerItemDefaults.shape(
                shape = RoundedCornerShape(40)
            ),
            scale = NavigationDrawerItemScale(
                focusedScale = 0.8f,
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
                text = item.name,
                style = if (isSelected) MaterialTheme.typography.labelLarge
                else MaterialTheme.typography.labelMedium,
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp) // Adjust width as needed
                    .height(lineThickness)
                    .background(lineColor)
            )
        }
    }
}

@Preview
@Composable
fun HomeDrawerPrev() {
    HomeDrawer(content = {
        Text(text = "Hello World")
    }, onScreenSelected = null)
}
