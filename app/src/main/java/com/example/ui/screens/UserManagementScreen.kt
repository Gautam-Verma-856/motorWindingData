package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.viewmodel.AuthViewModel
import com.example.ui.components.ConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController, authViewModel: AuthViewModel, initialFilter: String? = null) {
    val usersList by authViewModel.usersList.collectAsState()
    val isFetchingUsers by authViewModel.isFetchingUsers.collectAsState()
    
    var userToDelete by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(initialFilter?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() } ?: "All") }
    var userToReject by remember { mutableStateOf<String?>(null) }
    
    if (userToReject != null) {
        ConfirmationDialog(
            title = "Reject Account?",
            message = "Are you sure you want to reject this account?",
            confirmText = "Reject",
            isDestructive = true,
            onConfirm = {
                authViewModel.updateUserStatus(userToReject!!, "rejected")
                userToReject = null
            },
            onDismiss = { userToReject = null }
        )
    }
    
    LaunchedEffect(Unit) {
        authViewModel.fetchUsers()
    }
    
    if (userToDelete != null) {
        ConfirmationDialog(
            title = "Delete User?",
            message = "This action cannot be undone and will remove the user permanently.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                authViewModel.deleteUser(userToDelete!!)
                userToDelete = null
            },
            onDismiss = { userToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Manage Public Users", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Manage registered public accounts", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or email...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("All", "Pending", "Active", "Rejected", "Suspended")
                    items(filters) { filter ->
                        FilterChip(
                            selected = statusFilter == filter,
                            onClick = { statusFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }
            
            val filteredUsers = usersList.filter { user ->
                val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (statusFilter) {
                    "Pending" -> user.accountStatus == "pending"
                    "Active" -> user.accountStatus == "active"
                    "Rejected" -> user.accountStatus == "rejected"
                    "Suspended" -> user.accountStatus == "blocked" || user.accountStatus == "suspended"
                    else -> true
                }
                user.role == "public" && matchesSearch && matchesFilter
            }
            
            if (isFetchingUsers) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredUsers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No users found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Try adjusting your search query.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredUsers) { user ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    
                                    val statusColor = when (user.accountStatus) {
                                        "active" -> Color(0xFF10B981)
                                        "pending" -> Color(0xFFF59E0B)
                                        "rejected" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                    val statusText = if (user.accountStatus == "pending") "PENDING APPROVAL" else user.accountStatus.uppercase()
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            statusText, 
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall, 
                                            fontWeight = FontWeight.Bold, 
                                            color = statusColor
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    when (user.accountStatus) {
                                        "pending" -> {
                                            OutlinedButton(
                                                onClick = { authViewModel.updateUserStatus(user.id, "active") },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                                            ) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Approve")
                                            }
                                            OutlinedButton(
                                                onClick = { userToReject = user.id },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Reject")
                                            }
                                        }
                                        "active" -> {
                                            OutlinedButton(
                                                onClick = { authViewModel.updateUserStatus(user.id, "blocked") },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Suspend")
                                            }
                                        }
                                        else -> {
                                            OutlinedButton(
                                                onClick = { authViewModel.updateUserStatus(user.id, "active") },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                                            ) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Activate")
                                            }
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { userToDelete = user.id },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete User")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
