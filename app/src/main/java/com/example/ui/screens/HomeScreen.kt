package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.model.User
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, currentUser: User?, onLogout: () -> Unit) {
    var headerVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }
    var loginVisible by remember { mutableStateOf(false) }
    var contactVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        headerVisible = true
        delay(100)
        cardsVisible = true
        delay(100)
        loginVisible = true
        delay(100)
        contactVisible = true
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -50 })
            ) {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Logo", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Motor Winding Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        var menuExpanded by remember { mutableStateOf(false) }
                        
                        if (currentUser == null) {
                            // SMALL, PREMIUM ADMIN LOGIN BUTTON AT TOP-RIGHT
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

                            Surface(
                                onClick = { navController.navigate("admin_login") },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), // Subtle tonal style
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .scale(scale)
                                    .padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock, 
                                        contentDescription = "Admin Login", 
                                        modifier = Modifier.size(16.dp), 
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Admin Login", 
                                        style = MaterialTheme.typography.labelMedium, 
                                        fontWeight = FontWeight.Bold, 
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = onLogout) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                        
                        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface).width(220.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Home", fontWeight = FontWeight.Bold) },
                                    onClick = { menuExpanded = false },
                                    leadingIcon = { Icon(Icons.Rounded.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Single Phase Motor") },
                                    onClick = { menuExpanded = false; navController.navigate("motor_list/single") },
                                    leadingIcon = { Icon(Icons.Rounded.FlashOn, contentDescription = null, tint = Color(0xFFF59E0B)) }
                                )
                                DropdownMenuItem(
                                    text = { Text("3 Phase Motor") },
                                    onClick = { menuExpanded = false; navController.navigate("motor_list/three") },
                                    leadingIcon = { Icon(Icons.Rounded.Build, contentDescription = null, tint = Color(0xFF10B981)) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Contact Details") },
                                    onClick = { menuExpanded = false; navController.navigate("contact") },
                                    leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color(0xFF25D366)) }
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                if (currentUser == null) {
                                    DropdownMenuItem(
                                        text = { Text("Public Login") },
                                        onClick = { menuExpanded = false; navController.navigate("public_login") },
                                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF06B6D4)) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Admin Login") },
                                        onClick = { menuExpanded = false; navController.navigate("admin_login") },
                                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("Dashboard") },
                                        onClick = { 
                                            menuExpanded = false
                                            if (currentUser.role == "admin") navController.navigate("admin_dashboard")
                                            else navController.navigate("user_dashboard") 
                                        },
                                        leadingIcon = { Icon(Icons.Rounded.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // MOTOR CATEGORIES SECTION (MAIN FOCUS)
            AnimatedVisibility(
                visible = cardsVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Motor Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    PremiumActionCard(
                        title = "Single Phase Motor",
                        subtitle = "View Single Phase Motor Winding Data",
                        icon = Icons.Rounded.FlashOn,
                        iconTint = Color(0xFFF59E0B),
                        onClick = { navController.navigate("motor_list/single") }
                    )

                    PremiumActionCard(
                        title = "3 Phase Motor",
                        subtitle = "View 3 Phase Motor Winding Data",
                        icon = Icons.Rounded.Build,
                        iconTint = Color(0xFF10B981),
                        onClick = { navController.navigate("motor_list/three") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PUBLIC LOGIN SECTION (COMPACT PILL BUTTON)
            AnimatedVisibility(
                visible = loginVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentUser == null) {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

                        FilledTonalButton(
                            onClick = { navController.navigate("public_login") },
                            shape = RoundedCornerShape(50),
                            interactionSource = interactionSource,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .scale(scale)
                                .height(48.dp)
                                .widthIn(min = 200.dp),
                            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Public Login", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        TextButton(onClick = { navController.navigate("public_register") }) {
                            Text("Don't have an account? Register", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

                        FilledTonalButton(
                            onClick = {
                                if (currentUser.role == "admin") navController.navigate("admin_dashboard")
                                else navController.navigate("user_dashboard")
                            },
                            shape = RoundedCornerShape(50),
                            interactionSource = interactionSource,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .scale(scale)
                                .height(48.dp)
                                .widthIn(min = 200.dp),
                            elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Rounded.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // FOOTER BRANDING
            AnimatedVisibility(
                visible = contactVisible,
                enter = fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 32.dp)
                ) {
                    Divider(
                        modifier = Modifier
                            .width(48.dp)
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Text(
                        "Motor Winding Data",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "scale")

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
