package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.R
import com.example.model.User
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch

data class DrawerMenuItem(
    val id: String,
    val label: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val isDestructive: Boolean = false
)

data class DrawerSection(
    val title: String,
    val items: List<DrawerMenuItem>
)

fun canShowDashboardMenuItem(user: User?): Boolean {
    if (user == null) return false
    return (user.isAdmin() && !user.isBlockedOrSuspended()) ||
        (user.isPublic() && user.isActive() && user.emailVerified)
}

fun resolveDashboardRoute(user: User?): String {
    if (user == null) return "public_login"
    if (user.isAdmin() && !user.isBlockedOrSuspended()) return "admin_dashboard"
    if (user.isPublic() && user.isActive() && user.emailVerified) return "user_dashboard"
    return "public_login"
}

fun buildDrawerSections(
    user: User?,
    pendingMotorsCount: Int = 0,
    pendingUsersCount: Int = 0,
    unreadNotificationsCount: Int = 0
): List<DrawerSection> {
    if (user == null) return emptyList()

    val dashboardItem = if (canShowDashboardMenuItem(user)) {
        DrawerMenuItem(
            id = "go_to_dashboard",
            label = "Go to Dashboard",
            route = resolveDashboardRoute(user),
            icon = if (user.isAdmin()) Icons.Rounded.AdminPanelSettings else Icons.Rounded.Dashboard
        )
    } else null

    return if (user.isAdmin()) {
        listOf(
            DrawerSection(
                title = "MAIN MENU",
                items = listOfNotNull(dashboardItem) + listOf(
                    DrawerMenuItem(
                        id = "advanced_search",
                        label = "Advanced Search",
                        route = "advanced_search",
                        icon = Icons.AutoMirrored.Rounded.ManageSearch
                    ),
                    DrawerMenuItem(
                        id = "manage_motor_data",
                        label = "Manage Motor Data",
                        route = "motor_list/all",
                        icon = Icons.AutoMirrored.Rounded.List
                    ),
                    DrawerMenuItem(
                        id = "add_single_phase",
                        label = "Add Single Phase Motor",
                        route = "add_edit_motor/single/new",
                        icon = Icons.Rounded.FlashOn
                    ),
                    DrawerMenuItem(
                        id = "add_three_phase",
                        label = "Add Three Phase Motor",
                        route = "add_edit_motor/three/new",
                        icon = Icons.Rounded.Build
                    ),
                    DrawerMenuItem(
                        id = "pending_approvals",
                        label = "Pending Approvals",
                        route = "motor_list/pending",
                        icon = Icons.Rounded.PendingActions,
                        badgeCount = pendingMotorsCount
                    ),
                    DrawerMenuItem(
                        id = "manage_public_users",
                        label = "Manage Public Users",
                        route = "user_management",
                        icon = Icons.Rounded.ManageAccounts,
                        badgeCount = pendingUsersCount
                    ),
                    DrawerMenuItem(
                        id = "notifications",
                        label = "Notifications",
                        route = "notifications",
                        icon = Icons.Rounded.Notifications,
                        badgeCount = unreadNotificationsCount
                    )
                )
            ),
            DrawerSection(
                title = "ADMIN TOOLS",
                items = listOf(
                    DrawerMenuItem(
                        id = "account_settings",
                        label = "Account Settings",
                        route = "account_settings",
                        icon = Icons.Rounded.Settings
                    ),
                    DrawerMenuItem(
                        id = "contact_us",
                        label = "Contact Us",
                        route = "contact",
                        icon = Icons.Rounded.AlternateEmail
                    ),
                    DrawerMenuItem(
                        id = "app_information",
                        label = "App Information",
                        route = "app_info",
                        icon = Icons.Rounded.Info
                    ),
                    DrawerMenuItem(
                        id = "logout",
                        label = "Logout",
                        route = "logout",
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        isDestructive = true
                    )
                )
            )
        )
    } else {
        listOf(
            DrawerSection(
                title = "MAIN MENU",
                items = listOfNotNull(dashboardItem) + listOf(
                    DrawerMenuItem(
                        id = "advanced_search",
                        label = "Advanced Search",
                        route = "advanced_search",
                        icon = Icons.AutoMirrored.Rounded.ManageSearch
                    ),
                    DrawerMenuItem(
                        id = "motor_categories",
                        label = "Motor Categories",
                        route = "home",
                        icon = Icons.Rounded.Category
                    ),
                    DrawerMenuItem(
                        id = "my_submissions",
                        label = "My Submissions",
                        route = "motor_list/my_submissions",
                        icon = Icons.Rounded.History
                    ),
                    DrawerMenuItem(
                        id = "notifications",
                        label = "Notifications",
                        route = "notifications",
                        icon = Icons.Rounded.Notifications,
                        badgeCount = unreadNotificationsCount
                    )
                )
            ),
            DrawerSection(
                title = "ACCOUNT",
                items = listOf(
                    DrawerMenuItem(
                        id = "my_profile",
                        label = "My Profile",
                        route = "profile",
                        icon = Icons.Rounded.Person
                    ),
                    DrawerMenuItem(
                        id = "edit_profile",
                        label = "Edit Profile",
                        route = "edit_profile",
                        icon = Icons.Rounded.Edit
                    ),
                    DrawerMenuItem(
                        id = "change_password",
                        label = "Change Password",
                        route = "public_change_password",
                        icon = Icons.Rounded.LockReset
                    ),
                    DrawerMenuItem(
                        id = "notification_settings",
                        label = "Notification Settings",
                        route = "notification_settings",
                        icon = Icons.Rounded.NotificationsActive
                    ),
                    DrawerMenuItem(
                        id = "contact_us",
                        label = "Contact Us",
                        route = "contact",
                        icon = Icons.Rounded.AlternateEmail
                    ),
                    DrawerMenuItem(
                        id = "app_information",
                        label = "App Information",
                        route = "app_info",
                        icon = Icons.Rounded.Info
                    ),
                    DrawerMenuItem(
                        id = "logout",
                        label = "Logout",
                        route = "logout",
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        isDestructive = true
                    )
                )
            )
        )
    }
}

fun isDrawerItemSelected(itemRoute: String, currentRoute: String?): Boolean {
    if (currentRoute.isNullOrBlank()) return false
    if (itemRoute == "user_management" && currentRoute.startsWith("user_management")) return true
    return currentRoute == itemRoute
}

fun extractUserInitials(name: String, isAdmin: Boolean): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        parts.size == 1 -> parts[0].take(2).uppercase()
        isAdmin -> "AD"
        else -> "US"
    }
}

@Composable
fun UserAvatar(
    name: String,
    photoUrl: String,
    isAdmin: Boolean,
    sizeDp: Int = 52,
    modifier: Modifier = Modifier
) {
    val initials = extractUserInitials(name, isAdmin)
    val gradientColors = if (isAdmin) {
        listOf(MaterialTheme.colorScheme.primary, Color(0xFF6366F1))
    } else {
        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (photoUrl.isNotBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun AppNavigationDrawerContainer(
    drawerState: DrawerState,
    user: User?,
    currentRoute: String?,
    pendingMotorsCount: Int,
    pendingUsersCount: Int,
    unreadNotificationsCount: Int,
    gesturesEnabled: Boolean,
    showLogoutDialog: Boolean,
    onRequestLogoutDialog: () -> Unit,
    onDismissLogoutDialog: () -> Unit,
    onConfirmLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    if (user == null) {
        content()
        return
    }

    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            isAdmin = user.isAdmin(),
            onConfirm = onConfirmLogout,
            onDismiss = onDismissLogoutDialog
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled || drawerState.isOpen,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
        drawerContent = {
            AppNavigationDrawerSheet(
                user = user,
                currentRoute = currentRoute,
                pendingMotorsCount = pendingMotorsCount,
                pendingUsersCount = pendingUsersCount,
                unreadNotificationsCount = unreadNotificationsCount,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                onProfileClick = {
                    scope.launch {
                        drawerState.close()
                        onNavigate("profile")
                    }
                },
                onItemClick = { item ->
                    scope.launch {
                        drawerState.close()
                        if (item.route == "logout") {
                            onRequestLogoutDialog()
                        } else {
                            onNavigate(item.route)
                        }
                    }
                }
            )
        },
        content = content
    )
}

@Composable
fun AppNavigationDrawerSheet(
    user: User,
    currentRoute: String?,
    pendingMotorsCount: Int,
    pendingUsersCount: Int,
    unreadNotificationsCount: Int,
    onCloseDrawer: () -> Unit,
    onProfileClick: () -> Unit,
    onItemClick: (DrawerMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val sections = buildDrawerSections(
        user = user,
        pendingMotorsCount = pendingMotorsCount,
        pendingUsersCount = pendingUsersCount,
        unreadNotificationsCount = unreadNotificationsCount
    )
    val displayName = user.name.trim().ifBlank {
        if (user.isAdmin()) "Administrator" else "Public Member"
    }
    val roleLabel = if (user.isAdmin()) "Admin" else "Public"
    val statusLabel = when (user.accountStatus.trim().lowercase()) {
        "active" -> "Active"
        "pending" -> "Pending"
        "rejected" -> "Rejected"
        "blocked", "suspended" -> "Suspended"
        else -> user.accountStatus.trim().replaceFirstChar { it.uppercase() }
    }
    val statusContainerColor = when (user.accountStatus.trim().lowercase()) {
        "active" -> Color(0xFF10B981).copy(alpha = if (isDark) 0.22f else 0.14f)
        "pending" -> Color(0xFFF59E0B).copy(alpha = if (isDark) 0.22f else 0.14f)
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val statusContentColor = when (user.accountStatus.trim().lowercase()) {
        "active" -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
        "pending" -> if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onErrorContainer
    }

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.86f)
            .widthIn(min = 280.dp, max = 336.dp)
            .testTag("navigation_drawer_sheet"),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerTonalElevation = 4.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Premium App + User Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.35f else 0.55f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Brand Row + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Motor Winding Data Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Motor Winding Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Winding Reference & Tools",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close navigation menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Clickable User Profile Card
                Surface(
                    onClick = onProfileClick,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.55f else 0.55f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("drawer_profile_header")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            name = displayName,
                            photoUrl = user.photoUrl,
                            isAdmin = user.isAdmin(),
                            sizeDp = 50
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (user.isAdmin()) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    }
                                ) {
                                    Text(
                                        text = roleLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.isAdmin()) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                if (statusLabel.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = statusContainerColor
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = statusContentColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Open My Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Scrollable Grouped Menu Sections
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                sections.forEachIndexed { sectionIndex, section ->
                    if (sectionIndex > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        )
                    }

                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    section.items.forEach { item ->
                        val selected = !item.isDestructive && isDrawerItemSelected(item.route, currentRoute)
                        val itemContainerColor = when {
                            item.isDestructive -> Color.Transparent
                            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                            else -> Color.Transparent
                        }
                        val itemContentColor = when {
                            item.isDestructive -> MaterialTheme.colorScheme.error
                            selected -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val iconTint = when {
                            item.isDestructive -> MaterialTheme.colorScheme.error
                            selected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        NavigationDrawerItem(
                            selected = selected,
                            onClick = { onItemClick(item) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected || item.isDestructive) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            badge = {
                                if (item.badgeCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (item.id == "notifications") {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.defaultMinSize(minWidth = 24.dp, minHeight = 22.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = itemContainerColor,
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = itemContentColor,
                                unselectedTextColor = itemContentColor,
                                selectedIconColor = iconTint,
                                unselectedIconColor = iconTint
                            ),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("drawer_item_${item.id}")
                        )
                    }
                }
            }

            // Subtle Footer Version Stamp
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Motor Winding Data",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    isAdmin: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("logout_confirmation_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Confirm Logout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isAdmin) {
                        "Are you sure you want to log out from your Admin account?"
                    } else {
                        "Are you sure you want to log out from your account?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .padding(end = 8.dp)
                            .testTag("cancel_logout_button")
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("confirm_logout_button")
                    ) {
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
