package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel
import com.example.ui.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val motorViewModel: MotorViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController, currentUser = currentUser, onLogout = { authViewModel.logout() })
        }
        composable("admin_login") {
            AdminLoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("public_login") {
            PublicLoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("public_register") {
            PublicRegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(navController = navController, authViewModel = authViewModel, motorViewModel = motorViewModel)
        }
        composable("user_dashboard") {
            UserDashboardScreen(navController = navController, authViewModel = authViewModel, motorViewModel = motorViewModel)
        }
        composable(
            "motor_list/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "single"
            MotorListScreen(navController = navController, motorViewModel = motorViewModel, type = type, role = currentUser?.role ?: "public")
        }
        composable(
            "motor_detail/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "single"
            val id = backStackEntry.arguments?.getString("id") ?: ""
            MotorDetailScreen(navController = navController, motorViewModel = motorViewModel, type = type, id = id, role = currentUser?.role ?: "public")
        }
        composable(
            "add_edit_motor/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "single"
            val id = backStackEntry.arguments?.getString("id")
            AddEditMotorScreen(navController = navController, motorViewModel = motorViewModel, type = type, id = id)
        }
        composable("user_management") {
            UserManagementScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}
