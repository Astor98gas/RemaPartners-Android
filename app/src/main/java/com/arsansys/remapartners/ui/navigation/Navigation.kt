package com.arsansys.remapartners.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.arsansys.remapartners.ui.screen.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
    }
}
