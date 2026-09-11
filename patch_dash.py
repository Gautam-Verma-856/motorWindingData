import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreens.kt", "r") as f:
    content = f.read()

if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import android.widget.Toast\nimport androidx.compose.platform.LocalContext\nimport androidx.compose.ui.Modifier")

# Find AdminDashboardScreen
admin_regex = r"fun AdminDashboardScreen\([\s\S]*?\) \{"
match = re.search(admin_regex, content)
if match:
    insert_pos = match.end()
    content = content[:insert_pos] + "\n    val context = LocalContext.current" + content[insert_pos:]

# Find UserDashboardScreen
user_regex = r"fun UserDashboardScreen\([\s\S]*?\) \{"
match = re.search(user_regex, content)
if match:
    insert_pos = match.end()
    content = content[:insert_pos] + "\n    val context = LocalContext.current" + content[insert_pos:]

old_logout = """                                    authViewModel.logout()
                                    navController.navigate("home") { popUpTo(0) }"""
new_logout = """                                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                    authViewModel.logout()
                                    navController.navigate("home") { popUpTo(0) }"""
content = content.replace(old_logout, new_logout)

old_logout2 = """                    authViewModel.logout()
                    navController.navigate("home") { popUpTo(0) }"""
new_logout2 = """                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    authViewModel.logout()
                    navController.navigate("home") { popUpTo(0) }"""
content = content.replace(old_logout2, new_logout2)

with open("app/src/main/java/com/example/ui/screens/DashboardScreens.kt", "w") as f:
    f.write(content)
