package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.components.UserAvatar
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.ProfileUpdateState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onRequestLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
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
    val displayName = user.name.trim().ifBlank { if (isAdmin) "Administrator" else "Public User" }
    val changePasswordRoute = if (isAdmin) "admin_change_password" else "public_change_password"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
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
                    .testTag("profile_screen_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Summary Card
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
                        UserAvatar(
                            name = displayName,
                            photoUrl = user.photoUrl,
                            isAdmin = isAdmin,
                            sizeDp = 84
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = if (isAdmin) "Admin" else "Public",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF10B981).copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = user.accountStatus.ifBlank { "active" }
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Account Information Card
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Account Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        ProfileInfoRow(
                            icon = Icons.Rounded.Email,
                            label = "Email",
                            value = user.email.ifBlank { "Not configured" }
                        )
                        if (user.mobile.isNotBlank()) {
                            ProfileInfoRow(
                                icon = Icons.Rounded.Phone,
                                label = "Mobile",
                                value = user.mobile
                            )
                        }
                        ProfileInfoRow(
                            icon = Icons.Rounded.VerifiedUser,
                            label = "Verification",
                            value = if (user.emailVerified || isAdmin) "Verified Account" else "Pending Verification"
                        )
                    }
                }

                // Account Quick Actions
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SettingsActionRow(
                            icon = Icons.Rounded.Edit,
                            title = "Edit Profile",
                            subtitle = "Update display name, phone number or photo",
                            modifier = Modifier.testTag("profile_edit_button")
                        ) {
                            navController.navigate("edit_profile")
                        }
                        SettingsActionRow(
                            icon = Icons.Rounded.LockReset,
                            title = "Change Password",
                            subtitle = "Update your account password securely",
                            modifier = Modifier.testTag("profile_change_password_button")
                        ) {
                            navController.navigate(changePasswordRoute)
                        }
                        SettingsActionRow(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Notification Settings",
                            subtitle = "Manage alert preferences on this device",
                            modifier = Modifier.testTag("profile_notification_settings_button")
                        ) {
                            navController.navigate("notification_settings")
                        }
                    }
                }

                OutlinedButton(
                    onClick = onRequestLogout,
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("profile_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    motorViewModel: MotorViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()
    val profileUpdateState by authViewModel.profileUpdateState.collectAsState()

    val user = currentUser
    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate("public_login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    var fullName by remember(user.id) { mutableStateOf(user.name) }
    var mobile by remember(user.id) { mutableStateOf(user.mobile) }
    var photoUrl by remember(user.id) { mutableStateOf(user.photoUrl) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            isUploadingPhoto = true
            scope.launch {
                val uploadRes = motorViewModel.uploadPhoto(context, uri)
                isUploadingPhoto = false
                uploadRes.onSuccess { uploadedUrl ->
                    photoUrl = uploadedUrl
                    Toast.makeText(context, "Profile photo uploaded", Toast.LENGTH_SHORT).show()
                }.onFailure { err ->
                    Toast.makeText(
                        context,
                        err.message ?: "Unable to upload photo right now",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { authViewModel.resetProfileUpdateState() }
    }

    LaunchedEffect(profileUpdateState) {
        if (profileUpdateState is ProfileUpdateState.Success) {
            Toast.makeText(
                context,
                (profileUpdateState as ProfileUpdateState.Success).message,
                Toast.LENGTH_SHORT
            ).show()
            authViewModel.resetProfileUpdateState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UserAvatar(
                    name = fullName.ifBlank { user.name },
                    photoUrl = selectedImageUri?.toString() ?: photoUrl,
                    isAdmin = user.isAdmin(),
                    sizeDp = 96
                )

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isUploadingPhoto,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("edit_profile_photo_button")
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading Photo...")
                    } else {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Profile Photo")
                    }
                }

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_profile_name_input")
                        )

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Number") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_profile_mobile_input")
                        )

                        OutlinedTextField(
                            value = user.email,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (profileUpdateState is ProfileUpdateState.Error) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (profileUpdateState as ProfileUpdateState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        authViewModel.updateUserProfile(
                            name = fullName,
                            mobile = mobile,
                            photoUrl = photoUrl
                        )
                    },
                    enabled = profileUpdateState !is ProfileUpdateState.Loading && !isUploadingPhoto,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("save_profile_button")
                ) {
                    if (profileUpdateState is ProfileUpdateState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Icon(Icons.Rounded.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onRequestLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val user = currentUser

    if (user == null || !user.isAdmin()) {
        LaunchedEffect(Unit) {
            navController.navigate(if (user == null) "public_login" else "home") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings", fontWeight = FontWeight.Bold) },
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
                    .testTag("account_settings_screen"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = user.name.ifBlank { "Administrator" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Administrator Controls & Security",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SettingsActionRow(
                            icon = Icons.Rounded.Person,
                            title = "My Profile",
                            subtitle = "View admin profile information"
                        ) { navController.navigate("profile") }
                        SettingsActionRow(
                            icon = Icons.Rounded.Edit,
                            title = "Edit Profile",
                            subtitle = "Update display name, phone number or photo"
                        ) { navController.navigate("edit_profile") }
                        SettingsActionRow(
                            icon = Icons.Rounded.LockReset,
                            title = "Admin Change Password",
                            subtitle = "Re-authenticate and update admin password"
                        ) { navController.navigate("admin_change_password") }
                        SettingsActionRow(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Notification Settings",
                            subtitle = "Configure submission and user alert toggles"
                        ) { navController.navigate("notification_settings") }
                        SettingsActionRow(
                            icon = Icons.Rounded.AlternateEmail,
                            title = "Contact Us",
                            subtitle = "View official Instagram support details"
                        ) { navController.navigate("contact") }
                        SettingsActionRow(
                            icon = Icons.Rounded.Info,
                            title = "App Information",
                            subtitle = "Version and build details"
                        ) { navController.navigate("app_info") }
                    }
                }

                OutlinedButton(
                    onClick = onRequestLogout,
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
