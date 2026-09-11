import re

with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "r") as f:
    content = f.read()

empty_state = """                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    }"""

content = content.replace('EmptyStateView("users")', empty_state)

with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "w") as f:
    f.write(content)
