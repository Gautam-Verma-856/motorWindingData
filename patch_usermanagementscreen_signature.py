with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'fun UserManagementScreen(navController: NavController, authViewModel: AuthViewModel) {',
    'fun UserManagementScreen(navController: NavController, authViewModel: AuthViewModel, initialFilter: String? = null) {'
)

content = content.replace(
    'var statusFilter by remember { mutableStateOf("All") }',
    'var statusFilter by remember { mutableStateOf(initialFilter?.capitalize() ?: "All") }'
)

with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "w") as f:
    f.write(content)
