import re

with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "r") as f:
    content = f.read()

# Add a filter status variable and chips
content = content.replace(
    'var searchQuery by remember { mutableStateOf("") }',
    '''var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
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
    }'''
)

# Replace the search query column with search + filter chips
search_column = """            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search users...") },
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
            }"""

new_search_and_filters = """            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
            }"""

content = content.replace(search_column, new_search_and_filters)

# Update the filteredUsers
content = content.replace(
    '''val filteredUsers = usersList.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) 
            }''',
    '''val filteredUsers = usersList.filter { user ->
                val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) || user.email.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (statusFilter) {
                    "Pending" -> user.accountStatus == "pending"
                    "Active" -> user.accountStatus == "active"
                    "Rejected" -> user.accountStatus == "rejected"
                    "Suspended" -> user.accountStatus == "blocked" || user.accountStatus == "suspended"
                    else -> true
                }
                user.role == "public" && matchesSearch && matchesFilter
            }'''
)

# Status color and badging
content = content.replace(
    '''val statusColor = if (user.accountStatus == "active") Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = statusColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            user.accountStatus.uppercase(), 
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall, 
                                            fontWeight = FontWeight.Bold, 
                                            color = statusColor
                                        )
                                    }''',
    '''val statusColor = when (user.accountStatus) {
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
                                    }'''
)

# Update the buttons logic
buttons_old = """                                    if (user.accountStatus == "active") {
                                        OutlinedButton(
                                            onClick = { authViewModel.updateUserStatus(user.id, "blocked") },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Suspend")
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { authViewModel.updateUserStatus(user.id, "active") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                                        ) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Activate")
                                        }
                                    }"""

buttons_new = """                                    when (user.accountStatus) {
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
                                    }"""

content = content.replace(buttons_old, buttons_new)

# Make sure imports are present
if "import androidx.compose.foundation.lazy.items" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.items", "import androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.LazyRow")

with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "w") as f:
    f.write(content)
