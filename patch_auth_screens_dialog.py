import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

auth_status_dialog = """@Composable
fun AuthStatusDialog(
    statusTitle: String,
    statusMessage: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(iconBgColor, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = statusTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = statusMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("Back to Login")
                }
            }
        }
    }
}
"""

# Insert the dialog at the top, just below GlassTextField
content = content.replace("@Composable\nfun GlassLoginButton", auth_status_dialog + "\n@Composable\nfun GlassLoginButton")

# In PublicLoginScreen, update the error handling
old_error = """    if (authState is AuthState.Error) {
        ErrorDialog(
            message = (authState as AuthState.Error).message,
            onDismiss = { authViewModel.resetState() }
        )
    }"""

new_error = """    if (authState is AuthState.Error) {
        val errorMsg = (authState as AuthState.Error).message
        if (errorMsg.startsWith("Account Pending Approval:")) {
            AuthStatusDialog(
                statusTitle = "PENDING APPROVAL",
                statusMessage = "Your account has been created but is waiting for Admin approval.",
                icon = Icons.Default.HourglassEmpty,
                iconColor = Color(0xFFF59E0B),
                iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.2f),
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Not Approved:")) {
            AuthStatusDialog(
                statusTitle = "ACCOUNT NOT APPROVED",
                statusMessage = "Your account registration was not approved by Admin.",
                icon = Icons.Default.Cancel,
                iconColor = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.errorContainer,
                onDismiss = { authViewModel.resetState() }
            )
        } else if (errorMsg.startsWith("Account Suspended:")) {
            AuthStatusDialog(
                statusTitle = "ACCOUNT SUSPENDED",
                statusMessage = "Please contact the administrator.",
                icon = Icons.Default.Block,
                iconColor = MaterialTheme.colorScheme.error,
                iconBgColor = MaterialTheme.colorScheme.errorContainer,
                onDismiss = { authViewModel.resetState() }
            )
        } else {
            ErrorDialog(
                message = errorMsg,
                onDismiss = { authViewModel.resetState() }
            )
        }
    }"""

content = content.replace(old_error, new_error)

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
