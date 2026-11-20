package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.model.UserAdminAction
import com.example.model.UserManagementEngine
import com.example.ui.components.UserAvatar
import com.example.ui.components.UserStatusActionDialog
import com.example.ui.components.UserStatusChip
import com.example.ui.components.formatUserTimestamp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    motorViewModel: MotorViewModel,
    userId: String
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val usersList by authViewModel.usersList.collectAsState()
    val userActionMessage by authViewModel.userActionMessage.collectAsState()
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()

    // Strict Admin-only role guard
    if (currentUser == null || !currentUser!!.isAdmin()) {
        LaunchedEffect(currentUser) {
            navController.navigate(if (currentUser == null) "public_login" else "home") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val targetUser = remember(usersList, userId) {
        usersList.find { it.id == userId }
    }

    var pendingAction by remember { mutableStateOf<UserAdminAction?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = LocalIsDarkTheme.current

    LaunchedEffect(userId) {
        if (usersList.isEmpty()) {
            authViewModel.fetchUsers()
        }
        motorViewModel.loadData(isAdmin = true, userId = currentUser?.id)
    }

    LaunchedEffect(userActionMessage) {
        val message = userActionMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            authViewModel.clearUserActionMessage()
        }
    }

    if (pendingAction != null && targetUser != null) {
        UserStatusActionDialog(
            user = targetUser,
            action = pendingAction!!,
            onConfirm = { reason ->
                val targetStatus = when (pendingAction!!) {
                    UserAdminAction.APPROVE, UserAdminAction.ACTIVATE -> "active"
                    UserAdminAction.REJECT -> "rejected"
                    UserAdminAction.SUSPEND -> "blocked"
                }
                authViewModel.updateUserStatus(targetUser.id, targetStatus, reason)
                pendingAction = null
            },
            onDismiss = { pendingAction = null }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "User Profile Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = targetUser?.name?.ifBlank { targetUser.email } ?: "Public Account",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("user_detail_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            if (targetUser == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "User not found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { navController.navigateUp() }) {
                        Text("Go Back")
                    }
                }
            } else {
                val submissionStats = remember(targetUser, singlePhaseMotors, threePhaseMotors) {
                    UserManagementEngine.computeUserSubmissions(
                        user = targetUser,
                        singlePhaseMotors = singlePhaseMotors,
                        threePhaseMotors = threePhaseMotors
                    )
                }
                val userSingleMotors = remember(targetUser, singlePhaseMotors) {
                    singlePhaseMotors.filter {
                        UserManagementEngine.motorMatchesUser(it.createdBy, it.createdByName, targetUser)
                    }
                }
                val userThreeMotors = remember(targetUser, threePhaseMotors) {
                    threePhaseMotors.filter {
                        UserManagementEngine.motorMatchesUser(it.createdBy, it.createdByName, targetUser)
                    }
                }
                val allowedActions = remember(targetUser.accountStatus) {
                    UserManagementEngine.allowedActionsForStatus(targetUser.accountStatus)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                        .testTag("user_detail_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Profile Header Card
                    item(key = "header") {
                        ElevatedCard(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UserAvatar(
                                    name = targetUser.name.ifBlank { "Public User" },
                                    photoUrl = targetUser.photoUrl,
                                    isAdmin = false,
                                    sizeDp = 76
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = targetUser.name.ifBlank { "Unnamed User" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = targetUser.email.ifBlank { "No email" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserStatusChip(accountStatus = targetUser.accountStatus)
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "Public User",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Profile & Verification Details
                    item(key = "profile_info") {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Account & Verification Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                DetailInfoRow(
                                    icon = Icons.Rounded.Badge,
                                    label = "User ID",
                                    value = targetUser.id.ifBlank { "Unavailable" }
                                )
                                DetailInfoRow(
                                    icon = Icons.Rounded.Email,
                                    label = "Email Address",
                                    value = targetUser.email.ifBlank { "Not provided" }
                                )
                                DetailInfoRow(
                                    icon = Icons.Rounded.Phone,
                                    label = "Mobile Number",
                                    value = targetUser.mobile.ifBlank { "Not provided" }
                                )
                                DetailInfoRow(
                                    icon = Icons.Rounded.CalendarToday,
                                    label = "Account Created",
                                    value = formatUserTimestamp(targetUser.createdAt, includeTime = true)
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                DetailInfoRow(
                                    icon = Icons.Rounded.MarkEmailRead,
                                    label = "Email Verification",
                                    value = if (targetUser.emailVerified) "Verified" else "Not Verified",
                                    valueColor = if (targetUser.emailVerified) {
                                        if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
                                    } else {
                                        if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                                    }
                                )
                                DetailInfoRow(
                                    icon = Icons.Rounded.PhoneAndroid,
                                    label = "Phone Verification",
                                    value = if (targetUser.phoneVerified) "Verified" else "Not Verified",
                                    valueColor = if (targetUser.phoneVerified) {
                                        if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    // 3. Submission Statistics
                    item(key = "submission_stats") {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_detail_submission_stats")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Motor Submission Statistics",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SubmissionStatTile(
                                        label = "Total",
                                        value = submissionStats.totalSubmissions,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SubmissionStatTile(
                                        label = "Approved",
                                        value = submissionStats.approvedCount,
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                        modifier = Modifier.weight(1f)
                                    )
                                    SubmissionStatTile(
                                        label = "Pending",
                                        value = submissionStats.pendingCount,
                                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                        modifier = Modifier.weight(1f)
                                    )
                                    SubmissionStatTile(
                                        label = "Rejected",
                                        value = submissionStats.rejectedCount,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(
                                    text = "Breakdown: ${submissionStats.singlePhaseCount} Single Phase • ${submissionStats.threePhaseCount} Three Phase",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4. Profile & Status Update History
                    item(key = "update_history") {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_detail_history_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Profile & Status History",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                DetailInfoRow(
                                    icon = Icons.Rounded.VerifiedUser,
                                    label = "Status Updated",
                                    value = if (targetUser.statusUpdatedAt > 0L) {
                                        formatUserTimestamp(targetUser.statusUpdatedAt, includeTime = true)
                                    } else {
                                        "No status changes recorded"
                                    }
                                )
                                DetailInfoRow(
                                    icon = Icons.Rounded.CalendarToday,
                                    label = "Last Profile Update",
                                    value = if (targetUser.updatedAt > 0L) {
                                        formatUserTimestamp(targetUser.updatedAt, includeTime = true)
                                    } else {
                                        "No profile edits recorded"
                                    }
                                )
                                if (targetUser.statusReason.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Admin Status Note / Reason",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = targetUser.statusReason,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Status-Appropriate Admin Actions
                    item(key = "admin_actions") {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Admin Actions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    allowedActions.forEach { action ->
                                        when (action) {
                                            UserAdminAction.APPROVE -> {
                                                Button(
                                                    onClick = { pendingAction = action },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .defaultMinSize(minHeight = 48.dp)
                                                        .testTag("detail_action_approve"),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isDark) Color(0xFF059669) else Color(0xFF10B981),
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Approve User")
                                                }
                                            }
                                            UserAdminAction.REJECT -> {
                                                OutlinedButton(
                                                    onClick = { pendingAction = action },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .defaultMinSize(minHeight = 48.dp)
                                                        .testTag("detail_action_reject"),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Reject User")
                                                }
                                            }
                                            UserAdminAction.SUSPEND -> {
                                                OutlinedButton(
                                                    onClick = { pendingAction = action },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .defaultMinSize(minHeight = 48.dp)
                                                        .testTag("detail_action_suspend"),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Icon(Icons.Rounded.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Suspend User")
                                                }
                                            }
                                            UserAdminAction.ACTIVATE -> {
                                                Button(
                                                    onClick = { pendingAction = action },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .defaultMinSize(minHeight = 48.dp)
                                                        .testTag("detail_action_activate"),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isDark) Color(0xFF059669) else Color(0xFF10B981),
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(14.dp)
                                                ) {
                                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Activate User")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Submitted Motors Preview List (if any)
                    if (userSingleMotors.isNotEmpty() || userThreeMotors.isNotEmpty()) {
                        item(key = "motors_header") {
                            Text(
                                text = "Submitted Motors (${userSingleMotors.size + userThreeMotors.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        items(userSingleMotors, key = { "sp_${it.id}" }) { motor ->
                            SubmittedMotorSummaryRow(
                                title = motor.companyName.ifBlank { motor.category.ifBlank { "Single Phase Motor" } },
                                subtitle = "Single Phase • ${motor.hp.ifBlank { "HP N/A" }}",
                                status = motor.status,
                                onClick = { navController.navigate("motor_detail/single/${motor.id}") }
                            )
                        }
                        items(userThreeMotors, key = { "tp_${it.id}" }) { motor ->
                            SubmittedMotorSummaryRow(
                                title = motor.name.ifBlank { motor.category.ifBlank { "Three Phase Motor" } },
                                subtitle = "Three Phase • ${motor.hp.ifBlank { "HP N/A" }}",
                                status = motor.status,
                                onClick = { navController.navigate("motor_detail/three/${motor.id}") }
                            )
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

@Composable
private fun SubmissionStatTile(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubmittedMotorSummaryRow(
    title: String,
    subtitle: String,
    status: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.ElectricBolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = status.trim().uppercase().ifBlank { "APPROVED" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
