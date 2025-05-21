package com.arsansys.remapartners.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.arsansys.remapartners.ui.screen.chat.ChatDetailScreen
import com.arsansys.remapartners.ui.screen.chat.ChatsListScreen
import com.arsansys.remapartners.ui.screen.productos.HomeScreen
import com.arsansys.remapartners.ui.screen.users.LoginScreen
import com.arsansys.remapartners.ui.screen.productos.ProductoDetailScreen
import com.arsansys.remapartners.ui.screen.users.RegistroScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object ProductDetail : Screen("product_detail/{id}") {
        fun createRoute(id: String) = "product_detail/$id"
    }

    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail")
    data object Registro : Screen("registro")
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
        composable(Screen.Registro.route) {
            RegistroScreen(navController)
        }
        composable(
            route = Screen.ChatList.route
        ) {
            ChatsListScreen(navController = navController)
        }

        composable(
            route = Screen.ChatDetail.route + "?chatId={chatId}&productId={productId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType; defaultValue = "" },
                navArgument("productId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ChatDetailScreen(
                navController = navController,
                chatId = chatId,
                productId = productId
            )
        }
    }
}
