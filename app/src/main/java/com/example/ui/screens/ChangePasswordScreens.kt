package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.components.ErrorDialog
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ChangePasswordState
import com.example.viewmodel.PasswordResetState
import kotlinx.coroutines.delay

@Composable
fun AdminChangePasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null || !currentUser!!.isAdmin()) {
        LaunchedEffect(Unit) {
            navController.navigate("public_login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    ChangePasswordScreenContent(
        navController = navController,
        authViewModel = authViewModel,
        expectedRole = "admin",
        screenTitle = "Admin Change Password",
        roleLabel = "Administrator Account",
        subtitle = "Re-authenticate with your current password to update your Admin credentials.",
        accentColor = Color(0xFF6366F1),
        headerIcon = Icons.Rounded.AdminPanelSettings,
        userName = currentUser?.name?.ifBlank { "Admin" } ?: "Admin",
        userEmail = currentUser?.email ?: ""
    )
}

@Composable
fun PublicChangePasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null || !currentUser!!.isPublic()) {
        LaunchedEffect(Unit) {
            navController.navigate("public_login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    ChangePasswordScreenContent(
        navController = navController,
        authViewModel = authViewModel,
        expectedRole = "public",
        screenTitle = "Change Password",
        roleLabel = "Public Member Account",
        subtitle = "Re-authenticate with your current password to update your account password.",
        accentColor = Color(0xFF06B6D4),
        headerIcon = Icons.Rounded.VerifiedUser,
        userName = currentUser?.name?.ifBlank { "User" } ?: "User",
        userEmail = currentUser?.email ?: ""
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordScreenContent(
    navController: NavController,
    authViewModel: AuthViewModel,
    expectedRole: String,
    screenTitle: String,
    roleLabel: String,
    subtitle: String,
    accentColor: Color,
    headerIcon: ImageVector,
    userName: String,
    userEmail: String
) {
    val context = LocalContext.current
    val changePasswordState by authViewModel.changePasswordState.collectAsState()
    val passwordResetState by authViewModel.passwordResetState.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var attemptedSubmit by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showForgotConfirmDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.resetChangePasswordState()
        authViewModel.resetPasswordResetState()
        delay(50)
        isVisible = true
    }

    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetChangePasswordState()
            authViewModel.resetPasswordResetState()
        }
    }

    LaunchedEffect(changePasswordState) {
        if (changePasswordState is ChangePasswordState.Success) {
            currentPassword = ""
            newPassword = ""
            confirmPassword = ""
            attemptedSubmit = false
            showSuccessDialog = true
        }
    }

    val isLoading = changePasswordState is ChangePasswordState.Loading ||
        passwordResetState is PasswordResetState.Loading

    if (changePasswordState is ChangePasswordState.Loading) {
        LoadingDialog(message = "Verifying & updating password...")
    } else if (passwordResetState is PasswordResetState.Loading) {
        LoadingDialog(message = "Sending password reset link...")
    }

    if (showSuccessDialog) {
        SuccessAnimationDialog(
            title = "Password Updated!",
            message = "Your password was updated via Firebase Authentication. You remain logged in.",
            onDismiss = {
                showSuccessDialog = false
                authViewModel.resetChangePasswordState()
                navController.navigateUp()
            }
        )
    }

    if (changePasswordState is ChangePasswordState.Error) {
        ErrorDialog(
            title = "Password Update Failed",
            message = (changePasswordState as ChangePasswordState.Error).message,
            onDismiss = { authViewModel.resetChangePasswordState() }
        )
    }

    if (passwordResetState is PasswordResetState.Success) {
        SuccessAnimationDialog(
            title = "Reset Link Sent!",
            message = (passwordResetState as PasswordResetState.Success).message,
            onDismiss = { authViewModel.resetPasswordResetState() }
        )
    }

    if (passwordResetState is PasswordResetState.Error) {
        ErrorDialog(
            title = "Reset Email Failed",
            message = (passwordResetState as PasswordResetState.Error).message,
            onDismiss = { authViewModel.resetPasswordResetState() }
        )
    }

    if (showForgotConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showForgotConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.MarkEmailRead,
                    contentDescription = null,
                    tint = accentColor
                )
            },
            title = { Text("Forgot Current Password?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "We can send a Firebase password reset link to your registered email:\n\n$userEmail",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotConfirmDialog = false
                        authViewModel.sendPasswordResetEmail(userEmail)
                    }
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val currentError = attemptedSubmit && currentPassword.isBlank()
    val newLengthError = (attemptedSubmit || newPassword.isNotEmpty()) && newPassword.length < 6
    val newSameAsCurrentError = (attemptedSubmit || (newPassword.isNotEmpty() && currentPassword.isNotEmpty())) &&
        newPassword.isNotEmpty() && newPassword == currentPassword
    val confirmMismatchError = (attemptedSubmit || confirmPassword.isNotEmpty()) &&
        confirmPassword.isNotEmpty() && newPassword != confirmPassword
    val confirmEmptyError = attemptedSubmit && confirmPassword.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(screenTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Account Security", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.testTag("change_password_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            accentColor.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
                .imePadding()
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { it / 10 }, animationSpec = tween(400))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Account Summary Card
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accentColor.copy(alpha = 0.10f), MaterialTheme.colorScheme.surface)
                                    )
                                )
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(accentColor.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = headerIcon,
                                    contentDescription = roleLabel,
                                    tint = accentColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = roleLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (userEmail.isNotBlank()) {
                                    Text(
                                        text = userEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Password Form Card
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            PasswordFormField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                label = "Current Password *",
                                isVisible = currentPasswordVisible,
                                onToggleVisibility = { currentPasswordVisible = !currentPasswordVisible },
                                isError = currentError,
                                supportingText = if (currentError) "Current password is required" else null,
                                accentColor = accentColor,
                                fieldTestTag = "current_password_input",
                                toggleTestTag = "current_password_visibility_toggle",
                                enabled = !isLoading
                            )

                            PasswordFormField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "New Password *",
                                isVisible = newPasswordVisible,
                                onToggleVisibility = { newPasswordVisible = !newPasswordVisible },
                                isError = newLengthError || newSameAsCurrentError,
                                supportingText = when {
                                    attemptedSubmit && newPassword.isBlank() -> "New password is required"
                                    newSameAsCurrentError -> "New password must differ from current password"
                                    newLengthError -> "Minimum 6 characters required"
                                    newPassword.length >= 6 -> "Password length looks good"
                                    else -> "Use at least 6 characters"
                                },
                                accentColor = accentColor,
                                fieldTestTag = "new_password_input",
                                toggleTestTag = "new_password_visibility_toggle",
                                enabled = !isLoading
                            )

                            PasswordFormField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm New Password *",
                                isVisible = confirmPasswordVisible,
                                onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                                isError = confirmEmptyError || confirmMismatchError,
                                supportingText = when {
                                    confirmEmptyError -> "Please confirm your new password"
                                    confirmMismatchError -> "Passwords do not match"
                                    confirmPassword.isNotEmpty() && newPassword == confirmPassword -> "Passwords match"
                                    else -> null
                                },
                                accentColor = accentColor,
                                fieldTestTag = "confirm_password_input",
                                toggleTestTag = "confirm_password_visibility_toggle",
                                enabled = !isLoading
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val buttonScale by animateFloatAsState(
                                targetValue = if (isPressed) 0.97f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "submitBtnScale"
                            )

                            Button(
                                onClick = {
                                    attemptedSubmit = true
                                    val error = authViewModel.validateChangePasswordInput(
                                        currentPassword,
                                        newPassword,
                                        confirmPassword
                                    )
                                    if (error != null) {
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    } else {
                                        authViewModel.changePassword(
                                            currentPassword = currentPassword,
                                            newPassword = newPassword,
                                            confirmPassword = confirmPassword,
                                            expectedRole = expectedRole
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .scale(buttonScale)
                                    .testTag("update_password_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LockReset,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Update Password",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (userEmail.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { showForgotConfirmDialog = true },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("forgot_password_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Email,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Forgot Password? Send Reset Email",
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Passwords are updated securely via Firebase Authentication and are never stored in the database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PasswordFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    isError: Boolean,
    supportingText: String?,
    accentColor: Color,
    fieldTestTag: String,
    toggleTestTag: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else accentColor
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.testTag(toggleTestTag)
            ) {
                Icon(
                    imageVector = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = if (isVisible) "Hide password" else "Show password"
                )
            }
        },
        isError = isError,
        supportingText = supportingText?.let { msg ->
            {
                Text(
                    text = msg,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF10B981)
                )
            }
        },
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(fieldTestTag)
    )
}
