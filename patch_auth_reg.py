import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

old_reg_auth_state = """    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("user_dashboard") {
                    popUpTo("home") { inclusive = false }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }"""
    
new_state = """    // State managed below"""
content = content.replace(old_reg_auth_state, new_state)

reg_dialogs = """
    if (authState is AuthState.Success) {
        SuccessAnimationDialog(
            title = "Registration Successful",
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

content = content.replace("    Box(\n        modifier = Modifier\n            .fillMaxSize()", reg_dialogs + "    Box(\n        modifier = Modifier\n            .fillMaxSize()")
# This might fail or replace incorrectly since there are multiple Box modifiers.
# We should target a more specific replacement.
