package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.PublicUserOverviewStats
import com.example.model.User
import com.example.model.UserAdminAction
import com.example.model.UserManagementEngine
import com.example.model.UserStatusFilter
import com.example.model.UserSubmissionStats
import com.example.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatUserTimestamp(timestamp: Long, includeTime: Boolean = false): String {
    if (timestamp <= 0L) return "Not available"
    val pattern = if (includeTime) "dd MMM yyyy, hh:mm a" else "dd MMM yyyy"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}

@Composable
fun UserOverviewSummaryGrid(
    stats: PublicUserOverviewStats,
    selectedFilter: UserStatusFilter,
    onSelectFilter: (UserStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewMetricCard(
                title = "Total Public Users",
                count = stats.totalPublicUsers,
                icon = Icons.Rounded.Group,
                accentColor = MaterialTheme.colorScheme.primary,
                isSelected = selectedFilter == UserStatusFilter.ALL,
                onClick = { onSelectFilter(UserStatusFilter.ALL) },
                testTag = "overview_card_total",
                modifier = Modifier.weight(1f)
            )
            OverviewMetricCard(
                title = "Active Users",
                count = stats.activeUsers,
                icon = Icons.Rounded.VerifiedUser,
                accentColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                isSelected = selectedFilter == UserStatusFilter.ACTIVE,
                onClick = { onSelectFilter(UserStatusFilter.ACTIVE) },
                testTag = "overview_card_active",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewMetricCard(
                title = "Pending Approval",
                count = stats.pendingUsers,
                icon = Icons.Rounded.HourglassTop,
                accentColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                isSelected = selectedFilter == UserStatusFilter.PENDING,
                onClick = { onSelectFilter(UserStatusFilter.PENDING) },
                testTag = "overview_card_pending",
                modifier = Modifier.weight(1f)
            )
            OverviewMetricCard(
                title = "Suspended Users",
                count = stats.suspendedUsers,
                icon = Icons.Rounded.PauseCircle,
                accentColor = MaterialTheme.colorScheme.error,
                isSelected = selectedFilter == UserStatusFilter.SUSPENDED,
                onClick = { onSelectFilter(UserStatusFilter.SUSPENDED) },
                testTag = "overview_card_suspended",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 76.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            accentColor.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                accentColor.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        ),
        tonalElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun UserStatusChip(
    accountStatus: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val normalized = accountStatus.trim().lowercase()
    val (label, containerColor, contentColor) = when (normalized) {
        "active" -> Triple(
            "ACTIVE",
            Color(0xFF10B981).copy(alpha = if (isDark) 0.22f else 0.14f),
            if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)
        )
        "pending" -> Triple(
            "PENDING",
            Color(0xFFF59E0B).copy(alpha = if (isDark) 0.22f else 0.14f),
            if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
        )
        "blocked", "suspended" -> Triple(
            "SUSPENDED",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        "rejected" -> Triple(
            "REJECTED",
            MaterialTheme.colorScheme.error.copy(alpha = if (isDark) 0.24f else 0.12f),
            MaterialTheme.colorScheme.error
        )
        else -> Triple(
            accountStatus.trim().uppercase().ifBlank { "UNKNOWN" },
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.28f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PremiumPublicUserCard(
    user: User,
    submissionStats: UserSubmissionStats,
    onViewDetails: () -> Unit,
    onActionClick: (UserAdminAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val displayName = user.name.trim().ifBlank { "Unnamed User" }
    val allowedActions = remember(user.accountStatus) {
        UserManagementEngine.allowedActionsForStatus(user.accountStatus)
    }
    val regDate = remember(user.createdAt) { formatUserTimestamp(user.createdAt) }

    ElevatedCard(
        onClick = onViewDetails,
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    name = displayName,
                    photoUrl = user.photoUrl,
                    isAdmin = false,
                    sizeDp = 50
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (user.isUnseen()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = user.email.ifBlank { "No email" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                UserStatusChip(accountStatus = user.accountStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(10.dp))

            // Compact info rows
            if (user.mobile.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = "Mobile Number",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.mobile,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Registration Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Joined $regDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ElectricBolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (submissionStats.totalSubmissions == 1) {
                                "1 Motor"
                            } else {
                                "${submissionStats.totalSubmissions} Motors"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status-appropriate Admin Actions + View Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                allowedActions.forEach { action ->
                    when (action) {
                        UserAdminAction.APPROVE -> {
                            Button(
                                onClick = { onActionClick(action) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("user_action_approve_${user.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF059669) else Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        UserAdminAction.REJECT -> {
                            OutlinedButton(
                                onClick = { onActionClick(action) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("user_action_reject_${user.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reject", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        UserAdminAction.SUSPEND -> {
                            OutlinedButton(
                                onClick = { onActionClick(action) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("user_action_suspend_${user.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Suspend", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        UserAdminAction.ACTIVATE -> {
                            Button(
                                onClick = { onActionClick(action) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("user_action_activate_${user.id}"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF059669) else Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Activate", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                FilledTonalButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("user_view_profile_${user.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.PersonOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View Profile", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun UserStatusActionDialog(
    user: User,
    action: UserAdminAction,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember(user.id, action) { mutableStateOf("") }
    val requiresReasonField = action == UserAdminAction.REJECT || action == UserAdminAction.SUSPEND
    val isDestructive = requiresReasonField
    val displayName = user.name.trim().ifBlank { user.email }

    val (title, message, confirmLabel) = when (action) {
        UserAdminAction.APPROVE -> Triple(
            "Approve User?",
            "Approve $displayName so they can log in and submit motor winding data.",
            "Approve User"
        )
        UserAdminAction.REJECT -> Triple(
            "Reject User?",
            "Reject $displayName's registration request. You can optionally include a reason below.",
            "Reject User"
        )
        UserAdminAction.ACTIVATE -> Triple(
            "Activate User?",
            "Reactivate $displayName's public account and restore access.",
            "Activate User"
        )
        UserAdminAction.SUSPEND -> Triple(
            "Suspend User?",
            "Suspend $displayName's account to restrict login access. You can optionally add a reason below.",
            "Suspend User"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("user_action_dialog"),
        icon = {
            Icon(
                imageVector = if (isDestructive) Icons.Rounded.Block else Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (requiresReasonField) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("Enter optional reason...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_action_reason_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.trim()) },
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("user_action_confirm_button")
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("user_action_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UserLoadingSkeletonList(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "userSkeletonShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("users_loading_skeleton"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(shimmerColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(shimmerColor)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.75f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(shimmerColor)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(shimmerColor)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(shimmerColor)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(shimmerColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserErrorStateCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("users_error_state"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Unable to Load Public Users",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("users_retry_button")
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
fun UserEmptyStateCard(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("users_empty_state"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (hasFilters) "No matching users found" else "No public users yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (hasFilters) {
                    "Try a different name, email, mobile, user ID, or clear the active filters."
                } else {
                    "Registered public accounts will appear here once users sign up."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasFilters) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("empty_clear_filters_button")
                ) {
                    Text("Clear Filters")
                }
            }
        }
    }
}
