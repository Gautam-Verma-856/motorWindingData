import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreens.kt", "r") as f:
    content = f.read()

# Add pendingUsersCount and activeUsersCount
content = content.replace(
    'val totalMotorsCount = singlePhaseMotors.size + threePhaseMotors.size',
    '''val totalMotorsCount = singlePhaseMotors.size + threePhaseMotors.size
    
    val pendingUsersCount = usersList.count { it.role == "public" && it.accountStatus == "pending" }
    val activeUsersCount = usersList.count { it.role == "public" && it.accountStatus == "active" }
    val totalPublicUsersCount = usersList.count { it.role == "public" }'''
)

# Update the statistics grid
old_grid = """            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedStatCard("Total Motors", totalMotorsCount, Icons.Rounded.Functions, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    AnimatedStatCard("Pending Approval", pendingCount, Icons.Rounded.HourglassEmpty, Color(0xFFF59E0B), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedStatCard("Approved", approvedCount, Icons.Rounded.CheckCircle, Color(0xFF10B981), Modifier.weight(1f))
                    AnimatedStatCard("Public Users", usersList.size, Icons.Rounded.People, Color(0xFF06B6D4), Modifier.weight(1f))
                }
            }"""

new_grid = """            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            }"""

content = content.replace(old_grid, new_grid)

# Also update the Manage Public Data card badge count
content = content.replace(
    '''PremiumActionCard(
                    title = "Manage Public Data",
                    subtitle = "Manage registered public accounts",
                    icon = Icons.Rounded.ManageAccounts,
                    accentColor = Color(0xFF06B6D4),
                    badgeCount = 0
                ) { navController.navigate("user_management") }''',
    '''PremiumActionCard(
                    title = "Manage Public Data",
                    subtitle = "Manage registered public accounts",
                    icon = Icons.Rounded.ManageAccounts,
                    accentColor = Color(0xFF06B6D4),
                    badgeCount = pendingUsersCount
                ) { navController.navigate("user_management") }'''
)

with open("app/src/main/java/com/example/ui/screens/DashboardScreens.kt", "w") as f:
    f.write(content)
