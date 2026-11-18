package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val isSessionChecking by authViewModel.isSessionChecking.collectAsState()

            LaunchedEffect(isSessionChecking, currentUser) {
                if (!isSessionChecking) {
                    val user = currentUser
                    if (user != null) {
                        val role = user.role.trim().lowercase()
                        val status = user.accountStatus.trim().lowercase()
                        if (role == "admin" && status != "blocked" && status != "suspended") {
                            motorViewModel.loadData(isAdmin = true, userId = user.id)
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else if (role == "public" && user.emailVerified && status == "active") {
                            motorViewModel.loadData(isAdmin = false, userId = user.id)
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            authViewModel.logout()
                            motorViewModel.clearData()
                            navController.navigate("public_login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else {
                        motorViewModel.clearData()
                        navController.navigate("public_login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Motor Winding Data",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        composable("home") {
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                HomeScreen(
                    navController = navController,
                    currentUser = currentUser,
                    motorViewModel = motorViewModel,
                    onLogout = {
                        authViewModel.logout()
                        motorViewModel.clearData()
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable("contact") {
            ContactScreen(navController = navController)
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
            if (currentUser == null || !currentUser!!.role.equals("admin", ignoreCase = true)) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                AdminDashboardScreen(navController = navController, authViewModel = authViewModel, motorViewModel = motorViewModel)
            }
        }
        composable("user_dashboard") {
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                UserDashboardScreen(navController = navController, authViewModel = authViewModel, motorViewModel = motorViewModel)
            }
        }
        composable("admin_change_password") {
            if (currentUser == null || !currentUser!!.isAdmin()) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                AdminChangePasswordScreen(navController = navController, authViewModel = authViewModel)
            }
        }
        composable("public_change_password") {
            if (currentUser == null || !currentUser!!.isPublic()) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                PublicChangePasswordScreen(navController = navController, authViewModel = authViewModel)
            }
        }
        composable(
            "motor_list/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                val type = backStackEntry.arguments?.getString("type") ?: "single"
                val currentUid = currentUser?.id?.ifBlank { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid } ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                MotorListScreen(
                    navController = navController, 
                    motorViewModel = motorViewModel, 
                    type = type, 
                    role = currentUser?.role ?: "public",
                    currentUserId = currentUid
                )
            }
        }
        composable(
            "motor_detail/{type}/{id}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                val type = backStackEntry.arguments?.getString("type") ?: "single"
                val id = backStackEntry.arguments?.getString("id") ?: ""
                MotorDetailScreen(navController = navController, motorViewModel = motorViewModel, type = type, id = id, role = currentUser?.role ?: "public")
            }
        }
        composable(
            "add_edit_motor/{type}/{id}?category={category}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType },
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                val type = backStackEntry.arguments?.getString("type") ?: "single"
                val id = backStackEntry.arguments?.getString("id")
                val category = backStackEntry.arguments?.getString("category")
                AddEditMotorScreen(
                    navController = navController,
                    motorViewModel = motorViewModel,
                    type = type,
                    id = id,
                    authViewModel = authViewModel,
                    initialCategory = category
                )
            }
        }
        composable(
            "user_management?filter={filter}",
            arguments = listOf(navArgument("filter") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            if (currentUser == null || !currentUser!!.role.equals("admin", ignoreCase = true)) {
                LaunchedEffect(Unit) {
                    navController.navigate("public_login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            } else {
                val filter = backStackEntry.arguments?.getString("filter")
                UserManagementScreen(navController = navController, authViewModel = authViewModel, motorViewModel = motorViewModel, initialFilter = filter)
            }
        }
    }
}
