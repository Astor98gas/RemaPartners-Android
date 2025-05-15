package com.arsansys.remapartners.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.arsansys.remapartners.ui.screen.productos.HomeScreen
import com.arsansys.remapartners.ui.screen.LoginScreen
import com.arsansys.remapartners.ui.screen.productos.ProductoDetailScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object ProductDetail : Screen("product_detail/{id}") {
        fun createRoute(id: String) = "product_detail/$id"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            if (id != null) {
                ProductoDetailScreen(navController, id)
            }
        }

    }
}
