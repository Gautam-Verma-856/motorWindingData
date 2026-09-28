package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.viewmodel.AuthState
import com.example.viewmodel.EmailVerificationStatus

import com.example.ui.components.ErrorDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.AuthViewModel

import kotlinx.coroutines.delay

val LoginBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A), // Deep Slate
        Color(0xFF1E1B4B), // Deep Indigo
        Color(0xFF083344)  // Dark Cyan
    )
)

val GlassContainerColor = Color.White.copy(alpha = 0.08f)
val GlassBorderColor = Color.White.copy(alpha = 0.2f)

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    keyboardType: KeyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            autoCorrect = false
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF06B6D4),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(alpha = 0.8f),
            disabledBorderColor = Color.White.copy(alpha = 0.2f),
            cursorColor = Color(0xFF06B6D4),
            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f),
            disabledContainerColor = Color.Black.copy(alpha = 0.15f)
        ),
        trailingIcon = if (isPassword) {
            {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.White.copy(alpha = 0.7f))
                }
            }
        } else null
    )
}

@Composable
fun AuthStatusDialog(
    statusTitle: String,
    statusMessage: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    actionButtonText: String? = null,
    onAction: (() -> Unit)? = null,
    dismissButtonText: String = "Back to Login",
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(iconBgColor, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = statusTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = statusMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                if (actionButtonText != null && onAction != null) {
                    FilledTonalButton(
                        onClick = onAction,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(actionButtonText)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(dismissButtonText)
                }
            }
        }
    }
}

@Composable
fun GlassLoginButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    showSuccess: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "buttonScale")

    Button(
        onClick = onClick,
        enabled = !isLoading && !showSuccess,
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF06B6D4)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showSuccess) {
                Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(32.dp))
            } else if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(text, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        visible = true
        if (currentUser == null) {
            authViewModel.resetState()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val user = (authState as AuthState.Success).user
                if (user.role.equals("admin", ignoreCase = true) && !user.accountStatus.equals("blocked", ignoreCase = true) && !user.accountStatus.equals("suspended", ignoreCase = true)) {
                    showSuccess = true
                    delay(800)
                    navController.navigate("home") {
                        popUpTo("admin_login") { inclusive = true }
                    }
                    showSuccess = false
                } else if (user.role.equals("public", ignoreCase = true)) {
                    authViewModel.logout()
                    Toast.makeText(context, "This is a public account. Please use Public Login.", Toast.LENGTH_LONG).show()
                } else {
                    authViewModel.logout()
                    Toast.makeText(context, "Access Denied: Not an Admin", Toast.LENGTH_SHORT).show()
                }
            }
            // Error is handled by ErrorDialog composable below
            else -> {}
        }
    }

    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        if (errorMsg.contains("This is a public account", ignoreCase = true)) {
            AuthStatusDialog(
                statusTitle = "PUBLIC ACCOUNT",
                statusMessage = "This is a public account. Please use Public Login.",
                icon = Icons.Rounded.Person,
                iconColor = Color(0xFF06B6D4),
                iconBgColor = Color(0xFF06B6D4).copy(alpha = 0.2f),
                actionButtonText = "Go to Public Login",
                onAction = {
                    authViewModel.resetState()
                    navController.navigate("public_login") {
                        popUpTo("admin_login") { inclusive = true }
                    }
                },
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Pending Approval:")) {
            AuthStatusDialog(
                statusTitle = "PENDING APPROVAL",
                statusMessage = "Your account has been created but is waiting for Admin approval.",
                icon = Icons.Rounded.HourglassEmpty,
                iconColor = Color(0xFFF59E0B),
                iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Not Approved:")) {
            AuthStatusDialog(
                statusTitle = "ACCOUNT NOT APPROVED",
                statusMessage = "Your account registration was not approved by Admin.",
                icon = Icons.Rounded.Cancel,
                iconColor = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.errorContainer,
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Suspended:")) {
            AuthStatusDialog(
                statusTitle = "ACCOUNT SUSPENDED",
                statusMessage = "Please contact the administrator.",
                icon = Icons.Rounded.Block,
                iconColor = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.errorContainer,
                onDismiss = { authViewModel.resetState() }
            )
        } else {
            ErrorDialog(
                message = errorMsg,
                onDismiss = { authViewModel.resetState() }
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LoginBackgroundGradient)
    ) {
        // Transparent Top Bar
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(GlassContainerColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(30.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Admin", tint = Color(0xFF06B6D4), modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Admin Login",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in to manage motor winding data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Admin Email / ID",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                GlassLoginButton(
                    text = "Login",
                    onClick = { authViewModel.loginAdmin(email.trim(), password) },
                    isLoading = authState is AuthState.Loading,
                    showSuccess = showSuccess
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicLoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        visible = true
        if (authViewModel.currentUser.value == null) {
            authViewModel.resetState()
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val user = (authState as AuthState.Success).user
                if (user.role.equals("public", ignoreCase = true) && user.emailVerified && user.accountStatus.equals("active", ignoreCase = true)) {
                    showSuccess = true
                    delay(800)
                    navController.navigate("home") {
                        popUpTo("public_login") { inclusive = true }
                    }
                    showSuccess = false
                } else if (user.role.equals("admin", ignoreCase = true)) {
                    authViewModel.logout()
                    Toast.makeText(context, "This is an admin account. Please use Admin Login.", Toast.LENGTH_LONG).show()
                } else {
                    authViewModel.logout()
                }
            }
            // Error is handled by ErrorDialog composable below
            else -> {}
        }
    }

    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        if (errorMsg.contains("This is an admin account", ignoreCase = true)) {
            AuthStatusDialog(
                statusTitle = "ADMIN ACCOUNT",
                statusMessage = "This is an admin account. Please use Admin Login.",
                icon = Icons.Rounded.AdminPanelSettings,
                iconColor = MaterialTheme.colorScheme.primary,
                iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                actionButtonText = "Go to Admin Login",
                onAction = {
                    authViewModel.resetState()
                    navController.navigate("admin_login")
                },
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Email Not Verified:")) {
            AuthStatusDialog(
                statusTitle = "EMAIL NOT VERIFIED",
                statusMessage = "Please verify your email before logging in. We have sent a verification link to your email address.",
                icon = Icons.Rounded.Email,
                iconColor = Color(0xFF06B6D4),
                iconBgColor = Color(0xFF06B6D4).copy(alpha = 0.2f),
                actionButtonText = "Resend Verification Email",
                onAction = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        authViewModel.resendVerificationEmailForLogin(email, password) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Please enter your email and password to resend", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Pending Approval:")) {
            AuthStatusDialog(
                statusTitle = "PENDING APPROVAL",
                statusMessage = "Your email is verified, but your account is waiting for Admin approval.",
                icon = Icons.Rounded.HourglassEmpty,
                iconColor = Color(0xFFF59E0B),
                iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Not Approved:")) {
            AuthStatusDialog(
                statusTitle = "ACCOUNT NOT APPROVED",
                statusMessage = "Your account registration was not approved by Admin.",
                icon = Icons.Rounded.Cancel,
                iconColor = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.errorContainer,
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Suspended:")) {
            AuthStatusDialog(
                statusTitle = "ACCOUNT SUSPENDED",
                statusMessage = "Please contact the administrator.",
                icon = Icons.Rounded.Block,
                iconColor = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.errorContainer,
                onDismiss = { authViewModel.resetState() }
            )
        } else {
            ErrorDialog(
                message = errorMsg,
                onDismiss = { authViewModel.resetState() }
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LoginBackgroundGradient)
    ) {
        // Transparent Top Bar
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                if (navController.previousBackStackEntry != null) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(800)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(GlassContainerColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(30.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = "Public", tint = Color(0xFF3B82F6), modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Public Login",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in to view and submit motor data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                GlassLoginButton(
                    text = "Login",
                    onClick = { authViewModel.loginPublic(email.trim(), password) },
                    isLoading = authState is AuthState.Loading,
                    showSuccess = showSuccess
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { navController.navigate("public_register") }) {
                        Text("Create Account", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { navController.navigate("admin_login") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin Login", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicRegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val registeredEmail by authViewModel.registeredEmail.collectAsState()
    val emailVerificationStatus by authViewModel.emailVerificationStatus.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetEmailVerificationState()
        }
    }

    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        ErrorDialog(
            message = errorMsg,
            onDismiss = { authViewModel.resetState() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LoginBackgroundGradient)
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = {
                    authViewModel.logout()
                    navController.navigateUp()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (registeredEmail != null) {
                // Email Verification Screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(GlassContainerColor)
                        .border(1.dp, GlassBorderColor, RoundedCornerShape(30.dp))
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                if (emailVerificationStatus is EmailVerificationStatus.Verified)
                                    Color(0xFF10B981).copy(alpha = 0.2f)
                                else
                                    Color(0xFF06B6D4).copy(alpha = 0.2f),
                                RoundedCornerShape(38.dp)
                            )
                            .border(
                                1.dp,
                                if (emailVerificationStatus is EmailVerificationStatus.Verified)
                                    Color(0xFF10B981).copy(alpha = 0.4f)
                                else
                                    Color(0xFF06B6D4).copy(alpha = 0.4f),
                                RoundedCornerShape(38.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (emailVerificationStatus is EmailVerificationStatus.Verified)
                                Icons.Rounded.CheckCircle
                            else
                                Icons.Rounded.MarkEmailRead,
                            contentDescription = null,
                            tint = if (emailVerificationStatus is EmailVerificationStatus.Verified)
                                Color(0xFF10B981)
                            else
                                Color(0xFF06B6D4),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (emailVerificationStatus is EmailVerificationStatus.Verified)
                            "Email Verified!"
                        else
                            "Verify Your Email",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (emailVerificationStatus is EmailVerificationStatus.Verified)
                            "Your email has been verified successfully!\nYour account is now pending Admin approval.\nOnce an Admin approves your account, you will be able to log in and access all features."
                        else
                            "Verification email sent. Please verify your email before continuing.\n\nWe sent a verification link to:\n$registeredEmail\n\nPlease open your email app, check your inbox (and spam folder), and click the link.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    when (emailVerificationStatus) {
                        is EmailVerificationStatus.Checking -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF06B6D4))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Checking verification status with Firebase...", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                        is EmailVerificationStatus.NotVerified -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (emailVerificationStatus as EmailVerificationStatus.NotVerified).message,
                                    color = Color(0xFFFBBF24),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is EmailVerificationStatus.Resent -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ Verification email resent! Please check your inbox (and spam folder).",
                                    color = Color(0xFF34D399),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is EmailVerificationStatus.Error -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (emailVerificationStatus as EmailVerificationStatus.Error).message,
                                    color = Color(0xFFFCA5A5),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is EmailVerificationStatus.Verified -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "STATUS: PENDING ADMIN APPROVAL",
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Your email is confirmed. Waiting for administrator review.",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    if (emailVerificationStatus is EmailVerificationStatus.Verified) {
                        GlassLoginButton(
                            text = "Back to Login",
                            onClick = {
                                authViewModel.logout()
                                authViewModel.resetEmailVerificationState()
                                navController.navigateUp()
                            },
                            isLoading = false,
                            showSuccess = true
                        )
                    } else {
                        GlassLoginButton(
                            text = "Check Verification / I Verified",
                            onClick = { authViewModel.checkEmailVerification() },
                            isLoading = emailVerificationStatus is EmailVerificationStatus.Checking,
                            showSuccess = false
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { authViewModel.resendVerificationEmail() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF06B6D4))
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resend Verification Email", color = Color(0xFF06B6D4), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = {
                                authViewModel.logout()
                                authViewModel.resetEmailVerificationState()
                                navController.navigateUp()
                            }
                        ) {
                            Text("Back to Login", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            } else {
                // Public Registration Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(GlassContainerColor)
                        .border(1.dp, GlassBorderColor, RoundedCornerShape(30.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Email verification & Admin approval required",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    GlassTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        icon = Icons.Rounded.Person
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = mobile,
                        onValueChange = { input ->
                            if (input.length <= 13) {
                                mobile = input
                            }
                        },
                        label = "Mobile Number (10 Digits)",
                        icon = Icons.Rounded.Phone,
                        keyboardType = KeyboardType.Phone
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password (min 6 chars)",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
                    )
                    Spacer(modifier = Modifier.height(26.dp))

                    GlassLoginButton(
                        text = "Register",
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                                return@GlassLoginButton
                            }
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                                return@GlassLoginButton
                            }
                            val formattedMobile = authViewModel.formatIndianPhoneNumber(mobile)
                            if (formattedMobile == null) {
                                Toast.makeText(context, "Please enter a valid 10-digit Indian mobile number", Toast.LENGTH_SHORT).show()
                                return@GlassLoginButton
                            }
                            if (password.isBlank() || password.length < 6) {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                return@GlassLoginButton
                            }
                            if (confirmPassword.isBlank()) {
                                Toast.makeText(context, "Please confirm your password", Toast.LENGTH_SHORT).show()
                                return@GlassLoginButton
                            }
                            if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@GlassLoginButton
                            }
                            authViewModel.register(
                                name = name.trim(),
                                email = email.trim(),
                                mobile = formattedMobile,
                                pass = password
                            )
                        },
                        isLoading = authState is AuthState.Loading,
                        showSuccess = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigateUp() }) {
                        Text(
                            text = "Already have an account? Login",
                            color = Color(0xFF06B6D4),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
