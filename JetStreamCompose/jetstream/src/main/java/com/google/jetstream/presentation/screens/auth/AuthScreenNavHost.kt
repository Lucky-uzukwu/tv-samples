package com.google.jetstream.presentation.screens.auth

import AuthScreen
import LoginScreen
import RegisterScreen
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.jetstream.presentation.screens.Screens


@Composable
fun AuthScreenNavHost() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screens.AuthScreen(),
        builder = {
            composable(route = Screens.Login()) { backStackEntry ->
                val viewModel: AuthScreenViewModel =
                    if (navController.previousBackStackEntry != null) hiltViewModel(
                        navController.previousBackStackEntry!!
                    ) else hiltViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onSubmitSuccess = {
                        navController.navigate(Screens.Dashboard())
                    }
                )
            }
            composable(route = Screens.Register()) { backStackEntry ->
                val viewModel: AuthScreenViewModel =
                    if (navController.previousBackStackEntry != null) hiltViewModel(
                        navController.previousBackStackEntry!!
                    ) else hiltViewModel()
                RegisterScreen(
                    viewModel = viewModel,
                    onSubmitSuccess = {
                        navController.navigate(Screens.Dashboard())
                    }
                )
            }
        }

    )
}