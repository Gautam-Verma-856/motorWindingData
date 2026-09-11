import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "fun PublicRegisterScreen" in line:
        start_idx = i
        break

# Find LaunchedEffect inside PublicRegisterScreen
launched_effect_start = -1
for i in range(start_idx, len(lines)):
    if "LaunchedEffect(authState)" in lines[i]:
        launched_effect_start = i
        break

box_start = -1
for i in range(launched_effect_start, len(lines)):
    if "    Box(" in lines[i]:
        box_start = i
        break

# Replace the LaunchedEffect block with an empty comment
for i in range(launched_effect_start, box_start):
    lines[i] = ""

dialogs = """
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

lines[box_start] = dialogs + "\n" + lines[box_start]

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.writelines(lines)
