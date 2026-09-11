import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

old_toast = """            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_SHORT).show()
                authViewModel.resetState()
            }"""
            
new_toast = """            // Error is handled by ErrorDialog composable below"""

content = content.replace(old_toast, new_toast)

# Add ErrorDialog outside
# Find Box(modifier = Modifier.fillMaxSize().background(brush = LoginBackgroundGradient))
# and insert the ErrorDialog check just before it.

box_start = """    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LoginBackgroundGradient)
    ) {"""
    
error_dialog_code = """    if (authState is AuthState.Error) {
        ErrorDialog(
            message = (authState as AuthState.Error).message,
            onDismiss = { authViewModel.resetState() }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = LoginBackgroundGradient)
    ) {"""

# Replace only if it doesn't already have it
import string
parts = content.split(box_start)
if len(parts) == 4:
    # AdminLogin, PublicLogin, PublicSignup
    # PublicSignup already has it, so the third box_start might already be preceded by it.
    pass

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
