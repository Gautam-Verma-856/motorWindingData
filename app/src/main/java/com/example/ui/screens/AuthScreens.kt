package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
    onPasswordVisibilityChange: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
            autoCorrect = false
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF06B6D4),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF06B6D4),
            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
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
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Back to Login")
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
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val user = (authState as AuthState.Success).user
                if (user.role == "admin") {
                    showSuccess = true
                    delay(800)
                    navController.navigate("admin_dashboard") {
                        popUpTo("home") { inclusive = false }
                    }
                    showSuccess = false
                } else {
                    Toast.makeText(context, "Access Denied: Not an Admin", Toast.LENGTH_SHORT).show()
                    authViewModel.logout()
                }
            }
            // Error is handled by ErrorDialog composable below
            else -> {}
        }
    }

    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        if (errorMsg.startsWith("Account Pending Approval:")) {
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
                    onClick = { authViewModel.login(email.trim(), password) },
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
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                showSuccess = true
                delay(800)
                navController.navigate("user_dashboard") {
                    popUpTo("home") { inclusive = false }
                }
                showSuccess = false
            }
            // Error is handled by ErrorDialog composable below
            else -> {}
        }
    }

    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        if (errorMsg.startsWith("Account Pending Approval:")) {
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
                    onClick = { authViewModel.login(email.trim(), password) },
                    isLoading = authState is AuthState.Loading,
                    showSuccess = showSuccess
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = { navController.navigate("public_register") }) {
                    Text("Create Account", color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
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
    val context = LocalContext.current

    if (authState is AuthState.Success) {
        SuccessAnimationDialog(
            title = "Registration Submitted",
            message = "Your account has been created successfully.\nYour account is waiting for Admin approval.\n\nStatus: PENDING ADMIN APPROVAL",
            onDismiss = {
                navController.navigateUp()
                authViewModel.logout()
            }
        )
    }

    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        if (errorMsg.startsWith("Account Pending Approval:")) {
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(GlassContainerColor)
                    .border(1.dp, GlassBorderColor, RoundedCornerShape(30.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))

                GlassTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Rounded.Person
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = "Mobile Number",
                    icon = Icons.Rounded.Phone
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
                Spacer(modifier = Modifier.height(16.dp))
                GlassTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                GlassLoginButton(
                    text = "Create Account",
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (mobile.isBlank() || !android.util.Patterns.PHONE.matcher(mobile).matches()) {
                            Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
                            return@GlassLoginButton
                        }
                        if (password.isBlank()) {
                            Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
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
                        authViewModel.register(name.trim(), email.trim(), mobile.trim(), password)
                    },
                    isLoading = authState is AuthState.Loading,
                    showSuccess = authState is AuthState.Success
                )
            }
        }
    }
}
