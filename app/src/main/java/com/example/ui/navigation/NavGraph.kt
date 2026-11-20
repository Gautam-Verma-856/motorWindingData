package com.example.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.NotificationCounter
import com.example.ui.components.AppNavigationDrawerContainer
import com.example.ui.screens.AccountSettingsScreen
import com.example.ui.screens.AddEditMotorScreen
import com.example.ui.screens.AdminChangePasswordScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdvancedSearchScreen
import com.example.ui.screens.AppInfoScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MotorDetailScreen
import com.example.ui.screens.MotorListScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PublicChangePasswordScreen
import com.example.ui.screens.PublicLoginScreen
import com.example.ui.screens.PublicRegisterScreen
import com.example.ui.screens.UserDashboardScreen
import com.example.ui.screens.UserDetailScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.theme.LocalIsDarkTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    isDarkTheme: Boolean = LocalIsDarkTheme.current,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val motorViewModel: MotorViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val usersList by authViewModel.usersList.collectAsState()
    val seenPublicKeys by authViewModel.seenPublicNotificationKeys.collectAsState()

    val drawerState = remember { DrawerState(initialValue = DrawerValue.Closed) }
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rawRoute = backStackEntry?.destination?.route
    val resolvedCurrentRoute = remember(backStackEntry, rawRoute) {
        when {
            rawRoute == "motor_list/{type}" -> {
                val argType = backStackEntry?.arguments?.getString("type").orEmpty()
                "motor_list/$argType"
            }
            rawRoute?.startsWith("add_edit_motor/{type}/{id}") == true -> {
                val argType = backStackEntry?.arguments?.getString("type").orEmpty()
                val argId = backStackEntry?.arguments?.getString("id").orEmpty()
                "add_edit_motor/$argType/$argId"
            }
            else -> rawRoute
        }
    }

    val isAdminUser = currentUser?.isAdmin() == true
    val pendingMotorsCount = if (isAdminUser) {
        singlePhaseMotors.count { it.isPending() } + threePhaseMotors.count { it.isPending() }
    } else 0
    val pendingUsersCount = if (isAdminUser) {
        usersList.count { it.isPublic() && it.isPending() }
    } else 0
    val unreadNotificationsCount = when {
        currentUser == null -> 0
        isAdminUser -> NotificationCounter.countAdminUnread(singlePhaseMotors, threePhaseMotors, usersList)
        else -> NotificationCounter.countPublicUnread(
            userId = currentUser?.id.orEmpty(),
            singlePhaseMotors = singlePhaseMotors,
            threePhaseMotors = threePhaseMotors,
            seenKeys = seenPublicKeys
        )
    }

    LaunchedEffect(Unit) {
        if (drawerState.currentValue != DrawerValue.Closed || drawerState.targetValue != DrawerValue.Closed) {
            drawerState.snapTo(DrawerValue.Closed)
        }
    }

    LaunchedEffect(currentUser?.id) {
        if (drawerState.currentValue != DrawerValue.Closed || drawerState.targetValue != DrawerValue.Closed) {
            drawerState.snapTo(DrawerValue.Closed)
        }
        if (currentUser != null) {
            authViewModel.syncNotificationPreferences(context)
            if (currentUser?.isAdmin() == true) {
                authViewModel.fetchUsers()
            }
        }
    }

    // Open navigation drawer only when user explicitly taps the Menu icon (allow swipe-to-close when open)
    val gesturesEnabled = false

    val performLogout: () -> Unit = {
        showLogoutDialog = false
        coroutineScope.launch { drawerState.close() }
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        authViewModel.logout()
        motorViewModel.clearData()
        navController.navigate("public_login") {
            popUpTo(0) { inclusive = true }
        }
    }

    val openDrawer: () -> Unit = {
        if (currentUser != null) {
            coroutineScope.launch { drawerState.open() }
        }
    }

    val navigateFromDrawer: (String) -> Unit = { targetRoute ->
        if (currentUser == null) {
            navController.navigate("public_login") {
                popUpTo(0) { inclusive = true }
            }
        } else {
            val destination = if (targetRoute == "admin_dashboard" || targetRoute == "user_dashboard") {
                com.example.ui.components.resolveDashboardRoute(currentUser)
            } else {
                targetRoute
            }
            if (destination == "public_login") {
                navController.navigate("public_login") {
                    popUpTo(0) { inclusive = true }
                }
            } else {
                if (destination == "advanced_search") {
                    motorViewModel.resetSearchSession()
                }
                if (resolvedCurrentRoute != destination) {
                    navController.navigate(destination) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    AppNavigationDrawerContainer(
        drawerState = drawerState,
        user = currentUser,
        currentRoute = resolvedCurrentRoute,
        pendingMotorsCount = pendingMotorsCount,
        pendingUsersCount = pendingUsersCount,
        unreadNotificationsCount = unreadNotificationsCount,
        gesturesEnabled = gesturesEnabled,
        showLogoutDialog = showLogoutDialog,
        onRequestLogoutDialog = { showLogoutDialog = true },
        onDismissLogoutDialog = { showLogoutDialog = false },
        onConfirmLogout = performLogout,
        onNavigate = navigateFromDrawer
    ) {
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
                        unreadNotificationsCount = unreadNotificationsCount,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onOpenDrawer = openDrawer,
                        onLogout = { showLogoutDialog = true }
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
                if (currentUser == null || !currentUser!!.isAdmin()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    AdminDashboardScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        motorViewModel = motorViewModel,
                        onOpenDrawer = openDrawer,
                        onRequestLogout = { showLogoutDialog = true }
                    )
                }
            }
            composable("user_dashboard") {
                if (currentUser == null || !currentUser!!.isPublic()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(
                            when {
                                currentUser == null -> "public_login"
                                currentUser!!.isAdmin() -> "admin_dashboard"
                                else -> "public_login"
                            }
                        ) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    UserDashboardScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        motorViewModel = motorViewModel,
                        onOpenDrawer = openDrawer,
                        onRequestLogout = { showLogoutDialog = true }
                    )
                }
            }
            composable("admin_change_password") {
                if (currentUser == null || !currentUser!!.isAdmin()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
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
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    PublicChangePasswordScreen(navController = navController, authViewModel = authViewModel)
                }
            }
            composable("advanced_search") {
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    AdvancedSearchScreen(
                        navController = navController,
                        motorViewModel = motorViewModel,
                        role = currentUser?.role ?: "public"
                    )
                }
            }
            composable("profile") {
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    ProfileScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        onRequestLogout = { showLogoutDialog = true }
                    )
                }
            }
            composable("edit_profile") {
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    EditProfileScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        motorViewModel = motorViewModel
                    )
                }
            }
            composable("account_settings") {
                if (currentUser == null || !currentUser!!.isAdmin()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    AccountSettingsScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        onRequestLogout = { showLogoutDialog = true }
                    )
                }
            }
            composable("notifications") {
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    NotificationsScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        motorViewModel = motorViewModel
                    )
                }
            }
            composable("notification_settings") {
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    NotificationSettingsScreen(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
            composable("app_info") {
                if (currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate("public_login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    AppInfoScreen(navController = navController)
                }
            }
            composable(
                "motor_list/{type}",
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "single"
                val adminOnlyListTypes = setOf("pending", "rejected", "all")
                if (currentUser == null || (type in adminOnlyListTypes && !currentUser!!.isAdmin())) {
                    LaunchedEffect(Unit) {
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    val currentUid = currentUser?.id?.ifBlank { FirebaseAuth.getInstance().currentUser?.uid }
                        ?: FirebaseAuth.getInstance().currentUser?.uid
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
                    MotorDetailScreen(
                        navController = navController,
                        motorViewModel = motorViewModel,
                        type = type,
                        id = id,
                        role = currentUser?.role ?: "public"
                    )
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
                if (currentUser == null || !currentUser!!.isAdmin()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    val filter = backStackEntry.arguments?.getString("filter")
                    UserManagementScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        motorViewModel = motorViewModel,
                        initialFilter = filter
                    )
                }
            }
            composable(
                "user_detail/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                if (currentUser == null || !currentUser!!.isAdmin()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(if (currentUser == null) "public_login" else "home") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    val userId = backStackEntry.arguments?.getString("userId").orEmpty()
                    UserDetailScreen(
                        navController = navController,
                        authViewModel = authViewModel,
                        motorViewModel = motorViewModel,
                        userId = userId
                    )
                }
            }
        }
    }
}
