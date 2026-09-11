import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

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
    
""" + box_start

content = content.replace(box_start, error_dialog_code)

# Since PublicSignupScreen already had it, we might have duplicated it.
# Let's fix duplicated ErrorDialogs.
double_error_dialog = """    if (authState is AuthState.Error) {
        ErrorDialog(
            message = (authState as AuthState.Error).message,
            onDismiss = { authViewModel.resetState() }
        )
    }
    
    if (authState is AuthState.Error) {"""
    
content = content.replace(double_error_dialog, "    if (authState is AuthState.Error) {")

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
