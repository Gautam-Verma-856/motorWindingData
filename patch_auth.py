import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

imports = """
import com.example.ui.components.ErrorDialog
import com.example.ui.components.SuccessAnimationDialog
"""
if "ErrorDialog" not in content:
    content = content.replace("import com.example.viewmodel.AuthViewModel", imports + "\nimport com.example.viewmodel.AuthViewModel")


# Patch AdminLoginScreen
old_admin_auth_state = """    LaunchedEffect(authState) {
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
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }"""
new_admin_auth_state = """    // Dialogs handled outside"""
content = content.replace(old_admin_auth_state, new_admin_auth_state)

admin_dialogs = """
    if (authState is AuthState.Success) {
        val user = (authState as AuthState.Success).user
        if (user.role == "admin") {
            SuccessAnimationDialog(
                title = "Welcome Admin",
                onDismiss = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("home") { inclusive = false }
                    }
                    authViewModel.resetState()
                }
            )
        } else {
            ErrorDialog(
                message = "Access Denied: Not an Admin",
                onDismiss = { authViewModel.logout() }
            )
        }
    }
    
    if (authState is AuthState.Error) {
        ErrorDialog(
            message = (authState as AuthState.Error).message,
            onDismiss = { authViewModel.resetState() }
        )
    }
"""
content = content.replace("    Box(\n        modifier = Modifier\n            .fillMaxSize()", admin_dialogs + "    Box(\n        modifier = Modifier\n            .fillMaxSize()")

# Patch PublicLoginScreen
old_public_auth_state = """    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                showSuccess = true
                delay(800)
                navController.navigate("user_dashboard") {
                    popUpTo("home") { inclusive = false }
                }
                showSuccess = false
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }"""
content = content.replace(old_public_auth_state, new_admin_auth_state)

public_dialogs = """
    if (authState is AuthState.Success) {
        SuccessAnimationDialog(
            title = "Login Successful",
            onDismiss = {
                navController.navigate("user_dashboard") {
                    popUpTo("home") { inclusive = false }
                }
                authViewModel.resetState()
            }
        )
    }
    
    if (authState is AuthState.Error) {
        ErrorDialog(
            message = (authState as AuthState.Error).message,
            onDismiss = { authViewModel.resetState() }
        )
    }
"""
content = content.replace("    Box(\n        modifier = Modifier\n            .fillMaxSize()", public_dialogs + "    Box(\n        modifier = Modifier\n            .fillMaxSize()", 1) # Only replace once for PublicLogin, wait, will match twice if I don't use regex properly. 
# It's better to just write a simple script that matches the specific function bodies.
