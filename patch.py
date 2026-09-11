import re

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    content = f.read()

# 1. Add imports
imports = """
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.ui.components.ErrorDialog
"""
if "com.example.ui.components.LoadingDialog" not in content:
    content = content.replace("import kotlinx.coroutines.launch\n", "import kotlinx.coroutines.launch\n" + imports)

# 2. Remove Toast logic in LaunchedEffect(uiState)
old_launched_effect = """    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            Toast.makeText(context, "Motor data saved successfully.", Toast.LENGTH_SHORT).show()
            motorViewModel.resetUiState()
            navController.navigateUp()
        } else if (uiState is UiState.Error) {
            Toast.makeText(context, (uiState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            motorViewModel.resetUiState()
        }
    }"""
new_launched_effect = """    // State dialogs handled in Compose UI tree"""
content = content.replace(old_launched_effect, new_launched_effect)

# 3. Add Dialogs inside Scaffold
dialogs = """
    if (uiState is UiState.Loading) {
        LoadingDialog("Saving Motor Data...")
    } else if (isUploading) {
        LoadingDialog("Uploading Photo...")
    } else if (uiState is UiState.Success) {
        val currentUser = authViewModel.currentUser.value
        val isPublic = currentUser?.role == "public"
        val title = if (isPublic) "Submitted Successfully!" else "Motor Data Saved Successfully"
        val msg = if (isPublic) "Your motor data has been sent for Admin approval." else ""
        SuccessAnimationDialog(
            title = title,
            message = msg,
            onDismiss = {
                motorViewModel.resetUiState()
                navController.navigateUp()
            }
        )
    } else if (uiState is UiState.Error) {
        ErrorDialog(
            message = (uiState as UiState.Error).message,
            onDismiss = { motorViewModel.resetUiState() }
        )
    }
"""

if "LoadingDialog" not in content.split("Scaffold(")[1]:
    content = content.replace("    Scaffold(", dialogs + "\    Scaffold(")

# 4. Modify Button area to remove CircularProgressIndicator and disable buttons
old_button_area = """            if (uiState is UiState.Loading || isUploading) {
                CircularProgressIndicator()
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { navController.navigateUp() }, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button("""

new_button_area = """            // Button area
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { navController.navigateUp() }, 
                    modifier = Modifier.weight(1f),
                    enabled = uiState !is UiState.Loading && !isUploading
                ) {
                    Text("Cancel")
                }
                Button(
                    enabled = uiState !is UiState.Loading && !isUploading,"""

content = content.replace(old_button_area, new_button_area)

# Also remove the ending braces for the old 'else' block around buttons
old_button_end = """                    ) {
                        Text("Save Motor")
                    }
                }
            }
        }
    }
}"""
new_button_end = """                    ) {
                        Text("Save Motor")
                    }
                }
        }
    }
}"""
content = content.replace(old_button_end, new_button_end)


with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "w") as f:
    f.write(content)
