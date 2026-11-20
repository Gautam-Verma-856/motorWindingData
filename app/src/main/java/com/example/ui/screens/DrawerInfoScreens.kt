package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.BuildConfig
import com.example.R
import com.example.model.NotificationCounter
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel

private sealed class AppNotificationItem {
    data class AdminPendingMotor(
        val motorType: String,
        val motorId: String,
        val title: String,
        val subtitle: String,
        val unseen: Boolean
    ) : AppNotificationItem()

    data class AdminPendingUser(
        val userId: String,
        val name: String,
        val email: String,
        val unseen: Boolean
    ) : AppNotificationItem()

    data class PublicReviewUpdate(
        val key: String,
        val motorType: String,
        val motorId: String,
        val title: String,
        val status: String,
        val rejectionReason: String,
        val unread: Boolean
    ) : AppNotificationItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    motorViewModel: MotorViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val usersList by authViewModel.usersList.collectAsState()
    val seenPublicKeys by authViewModel.seenPublicNotificationKeys.collectAsState()

    val user = currentUser
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("public_login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val isAdmin = user.isAdmin()

    LaunchedEffect(user.id, isAdmin) {
        authViewModel.syncNotificationPreferences(context)
        motorViewModel.loadData(isAdmin = isAdmin, userId = user.id)
        if (isAdmin) {
            authViewModel.fetchUsers()
        }
    }

    val notificationItems: List<AppNotificationItem> = if (isAdmin) {
        val pendingSingle = singlePhaseMotors.filter { it.isPending() }.map {
            AppNotificationItem.AdminPendingMotor(
                motorType = "single",
                motorId = it.id,
                title = "Pending Motor: ${it.companyName.ifBlank { "Single Phase Motor" }}",
                subtitle = "Single Phase • ${it.hp.ifBlank { "N/A" }}",
                unseen = it.isUnseen()
            )
        }
        val pendingThree = threePhaseMotors.filter { it.isPending() }.map {
            AppNotificationItem.AdminPendingMotor(
                motorType = "three",
                motorId = it.id,
                title = "Pending Motor: ${it.name.ifBlank { "3 Phase Motor" }}",
                subtitle = "Three Phase • ${it.hp.ifBlank { "N/A" }}",
                unseen = it.isUnseen()
            )
        }
        val pendingUsers = usersList.filter { it.isPublic() && it.isPending() }.map {
            AppNotificationItem.AdminPendingUser(
                userId = it.id,
                name = it.name.ifBlank { "New Public User" },
                email = it.email,
                unseen = it.isUnseen()
            )
        }
        pendingSingle + pendingThree + pendingUsers
    } else {
        val singleUpdates = singlePhaseMotors
            .filter { it.createdBy == user.id && (it.isExplicitlyApproved() || it.isRejected()) }
            .map {
                val key = NotificationCounter.publicNotificationKey("single", it.id, it.status)
                AppNotificationItem.PublicReviewUpdate(
                    key = key,
                    motorType = "single",
                    motorId = it.id,
                    title = it.companyName.ifBlank { "Single Phase Motor" },
                    status = it.status,
                    rejectionReason = it.rejectionReason,
                    unread = !seenPublicKeys.contains(key)
                )
            }
        val threeUpdates = threePhaseMotors
            .filter { it.createdBy == user.id && (it.isExplicitlyApproved() || it.isRejected()) }
            .map {
                val key = NotificationCounter.publicNotificationKey("three", it.id, it.status)
                AppNotificationItem.PublicReviewUpdate(
                    key = key,
                    motorType = "three",
                    motorId = it.id,
                    title = it.name.ifBlank { "Three Phase Motor" },
                    status = it.status,
                    rejectionReason = it.rejectionReason,
                    unread = !seenPublicKeys.contains(key)
                )
            }
        singleUpdates + threeUpdates
    }

    val hasUnread = notificationItems.any {
        when (it) {
            is AppNotificationItem.AdminPendingMotor -> it.unseen
            is AppNotificationItem.AdminPendingUser -> it.unseen
            is AppNotificationItem.PublicReviewUpdate -> it.unread
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (hasUnread) {
                        TextButton(
                            onClick = {
                                if (isAdmin) {
                                    notificationItems.forEach { item ->
                                        when (item) {
                                            is AppNotificationItem.AdminPendingMotor -> {
                                                if (item.unseen) {
                                                    motorViewModel.markMotorAsSeen(item.motorType, item.motorId)
                                                }
                                            }
                                            is AppNotificationItem.AdminPendingUser -> {
                                                if (item.unseen) {
                                                    authViewModel.markUserAsSeen(item.userId)
                                                }
                                            }
                                            else -> Unit
                                        }
                                    }
                                } else {
                                    val keys = notificationItems
                                        .filterIsInstance<AppNotificationItem.PublicReviewUpdate>()
                                        .map { it.key }
                                    authViewModel.markAllPublicNotificationsSeen(context, keys)
                                }
                            },
                            modifier = Modifier.testTag("notifications_mark_all_read")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Read")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (notificationItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .padding(32.dp)
                        .testTag("notifications_empty_state"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(48.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All caught up!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isAdmin) {
                            "No pending motor submissions or account approval requests."
                        } else {
                            "You have no new submission review updates right now."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(16.dp)
                        .testTag("notifications_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notificationItems) { item ->
                        when (item) {
                            is AppNotificationItem.AdminPendingMotor -> {
                                NotificationCard(
                                    icon = Icons.Rounded.PendingActions,
                                    accentColor = Color(0xFFF59E0B),
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    unread = item.unseen,
                                    onClick = {
                                        motorViewModel.markMotorAsSeen(item.motorType, item.motorId)
                                        navController.navigate("motor_detail/${item.motorType}/${item.motorId}")
                                    }
                                )
                            }
                            is AppNotificationItem.AdminPendingUser -> {
                                NotificationCard(
                                    icon = Icons.Rounded.PersonAdd,
                                    accentColor = Color(0xFF06B6D4),
                                    title = "Pending User: ${item.name}",
                                    subtitle = item.email,
                                    unread = item.unseen,
                                    onClick = {
                                        authViewModel.markUserAsSeen(item.userId)
                                        navController.navigate("user_management?filter=pending")
                                    }
                                )
                            }
                            is AppNotificationItem.PublicReviewUpdate -> {
                                val approved = item.status.trim().equals("approved", ignoreCase = true)
                                NotificationCard(
                                    icon = if (approved) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    accentColor = if (approved) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                    title = if (approved) {
                                        "Submission Approved: ${item.title}"
                                    } else {
                                        "Submission Rejected: ${item.title}"
                                    },
                                    subtitle = if (approved) {
                                        "Your ${if (item.motorType == "single") "Single Phase" else "Three Phase"} submission is now live."
                                    } else {
                                        item.rejectionReason.ifBlank { "Please review details and submit corrections." }
                                    },
                                    unread = item.unread,
                                    onClick = {
                                        authViewModel.markPublicNotificationSeen(context, item.key)
                                        navController.navigate("motor_detail/${item.motorType}/${item.motorId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    title: String,
    subtitle: String,
    unread: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (unread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val settings by authViewModel.notificationSettings.collectAsState()
    val isAdmin = currentUser?.isAdmin() == true

    LaunchedEffect(Unit) {
        authViewModel.syncNotificationPreferences(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("notification_settings_screen"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NotificationToggleRow(
                            title = if (isAdmin) "Pending Motor Alerts" else "Submission Review Alerts",
                            subtitle = if (isAdmin) {
                                "Highlight new motor winding submissions awaiting approval"
                            } else {
                                "Show alerts when your submitted motor data is approved or rejected"
                            },
                            checked = settings.submissionAlertsEnabled,
                            onCheckedChange = { enabled ->
                                authViewModel.updateNotificationSettings(
                                    context,
                                    settings.copy(submissionAlertsEnabled = enabled)
                                )
                            }
                        )

                        if (isAdmin) {
                            NotificationToggleRow(
                                title = "User Registration Alerts",
                                subtitle = "Show alerts for newly registered public accounts awaiting review",
                                checked = settings.userApprovalAlertsEnabled,
                                onCheckedChange = { enabled ->
                                    authViewModel.updateNotificationSettings(
                                        context,
                                        settings.copy(userApprovalAlertsEnabled = enabled)
                                    )
                                }
                            )
                        }

                        NotificationToggleRow(
                            title = "App Update Alerts",
                            subtitle = "Check for newer Motor Winding Data releases on launch",
                            checked = settings.appUpdateAlertsEnabled,
                            onCheckedChange = { enabled ->
                                authViewModel.updateNotificationSettings(
                                    context,
                                    settings.copy(appUpdateAlertsEnabled = enabled)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Information", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("app_info_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.size(84.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Motor Winding Data Icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(22.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Motor Winding Data",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Version ${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "A complete field-ready winding reference for Single Phase and Three Phase motors with verified coil, SWG, slot, RPM, Amps, and connection records.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Release Highlights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "• Single Phase & Three Phase winding specifications\n" +
                                "• Advanced multi-filter search across HP, RPM, Amps, Slot, SWG & Connection\n" +
                                "• Community motor submissions with admin verification workflow\n" +
                                "• Role-based dashboards and optional in-app updates",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("contact") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Rounded.AlternateEmail, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Contact Us")
                    }

                    Button(
                        onClick = { navController.popBackStack() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Rounded.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Done")
                    }
                }
            }
        }
    }
}
