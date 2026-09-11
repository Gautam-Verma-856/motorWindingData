package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Logout
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, authViewModel: AuthViewModel, motorViewModel: MotorViewModel) {
    val context = LocalContext.current
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val usersList by authViewModel.usersList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val pendingCount = singlePhaseMotors.count { it.status == "pending" } + threePhaseMotors.count { it.status == "pending" }
    val approvedCount = singlePhaseMotors.count { it.status == "approved" } + threePhaseMotors.count { it.status == "approved" }
    val totalMotorsCount = singlePhaseMotors.size + threePhaseMotors.size
    
    val pendingUsersCount = usersList.count { it.role == "public" && it.accountStatus == "pending" }
    val activeUsersCount = usersList.count { it.role == "public" && it.accountStatus == "active" }
    val totalPublicUsersCount = usersList.count { it.role == "public" }
    
    LaunchedEffect(Unit) {
        motorViewModel.loadData()
        authViewModel.fetchUsers()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Admin Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Motor Winding Data Management", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(currentUser?.name ?: "Admin", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // STATISTICS GRID
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedStatCard("Total Motors", totalMotorsCount, Icons.Rounded.Functions, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    AnimatedStatCard("Pending Motors", pendingCount, Icons.Rounded.HourglassEmpty, Color(0xFFF59E0B), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedStatCard("Public Users", totalPublicUsersCount, Icons.Rounded.People, Color(0xFF06B6D4), Modifier.weight(1f))
                    AnimatedStatCard("Active Users", activeUsersCount, Icons.Rounded.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        onClick = { navController.navigate("user_management?filter=pending") },
                        modifier = Modifier.weight(1f),
                        color = Color.Transparent
                    ) {
                        AnimatedStatCard("Pending Users", pendingUsersCount, Icons.Rounded.PersonAdd, Color(0xFFEF4444), Modifier)
                    }
                }
            }
            
            // QUICK ACTIONS
            AdminSectionTitle("Quick Actions")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    text = "Add 1-Phase",
                    icon = Icons.Rounded.FlashOn,
                    color = Color(0xFF8B5CF6),
                    onClick = { navController.navigate("add_edit_motor/single/new") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "Add 3-Phase",
                    icon = Icons.Rounded.Build,
                    color = Color(0xFF10B981),
                    onClick = { navController.navigate("add_edit_motor/three/new") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumActionCard(
                    title = "Pending Approval",
                    subtitle = "Review public motor submissions",
                    icon = Icons.Rounded.PendingActions,
                    accentColor = if (pendingCount > 0) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                    badgeCount = pendingCount
                ) { navController.navigate("motor_list/pending") }
                
                PremiumActionCard(
                    title = "Approved Motors",
                    subtitle = "Browse verified motor winding data",
                    icon = Icons.Rounded.CheckCircle,
                    accentColor = Color(0xFF10B981),
                    badgeCount = 0
                ) { navController.navigate("motor_list/approved") }
                
                PremiumActionCard(
                    title = "Manage Public Data",
                    subtitle = "Manage registered public accounts",
                    icon = Icons.Rounded.ManageAccounts,
                    accentColor = Color(0xFF06B6D4),
                    badgeCount = pendingUsersCount
                ) { navController.navigate("user_management") }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    authViewModel.logout()
                    navController.navigate("home") { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavController, authViewModel: AuthViewModel, motorViewModel: MotorViewModel) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Welcome", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(currentUser?.name ?: "User", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        authViewModel.logout()
                        navController.navigate("home") { popUpTo(0) }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AdminSectionTitle("Main Actions")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumActionCard(
                    title = "Approved Motors",
                    subtitle = "Browse verified motor winding data",
                    icon = Icons.AutoMirrored.Rounded.List,
                    accentColor = MaterialTheme.colorScheme.primary,
                    badgeCount = 0
                ) { navController.navigate("motor_list/approved") }
                
                PremiumActionCard(
                    title = "My Submissions",
                    subtitle = "Track approval status of your data",
                    icon = Icons.Rounded.History,
                    accentColor = Color(0xFF8B5CF6),
                    badgeCount = 0
                ) { navController.navigate("motor_list/my_submissions") }
            }
            
            AdminSectionTitle("Submit Data")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButton(
                    text = "Add 1-Phase",
                    icon = Icons.Rounded.FlashOn,
                    color = Color(0xFFF59E0B),
                    onClick = { navController.navigate("add_edit_motor/single/new") },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "Add 3-Phase",
                    icon = Icons.Rounded.Build,
                    color = Color(0xFF10B981),
                    onClick = { navController.navigate("add_edit_motor/three/new") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AnimatedStatCard(title: String, targetValue: Int, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }
    
    val animatedValue by animateIntAsState(
        targetValue = if (startAnimation) targetValue else 0,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "count"
    )
    
    ElevatedCard(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Box(
                    modifier = Modifier.size(24.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
            }
            Text(animatedValue.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AdminSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    )
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale).height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumActionCard(title: String, subtitle: String, icon: ImageVector, accentColor: Color = MaterialTheme.colorScheme.primary, badgeCount: Int = 0, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "scale")
    
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (badgeCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = accentColor,
                            modifier = Modifier.defaultMinSize(minWidth = 20.dp).height(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                                Text(badgeCount.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
