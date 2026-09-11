import re

with open("app/src/main/java/com/example/ui/screens/MotorListScreen.kt", "r") as f:
    content = f.read()

imports = """
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.ui.components.ErrorDialog
import com.example.ui.components.ConfirmationDialog
import com.example.viewmodel.UiState
"""
content = content.replace("import com.example.viewmodel.MotorViewModel", "import com.example.viewmodel.MotorViewModel\n" + imports)

# We need to add state observation to MotorListScreen
# Look for MotorListScreen definition
screen_def_regex = r"fun MotorListScreen\([\s\S]*?\) \{"
match = re.search(screen_def_regex, content)
if match:
    insert_pos = match.end()
    state_code = """
    val uiState by motorViewModel.uiState.collectAsState()
    var motorToDelete by remember { mutableStateOf<String?>(null) }
    var deleteType by remember { mutableStateOf<String?>(null) }
    
    if (uiState is UiState.Loading) {
        LoadingDialog("Processing...")
    } else if (uiState is UiState.Success) {
        val action = (uiState as UiState.Success).message
        val title = when (action) {
            "delete" -> "Deleted Successfully"
            "approve" -> "Approved Successfully"
            "reject" -> "Submission Rejected"
            else -> "Success"
        }
        SuccessAnimationDialog(
            title = title,
            onDismiss = { motorViewModel.resetUiState() }
        )
    } else if (uiState is UiState.Error) {
        ErrorDialog(
            message = (uiState as UiState.Error).message,
            onDismiss = { motorViewModel.resetUiState() }
        )
    }
    
    if (motorToDelete != null && deleteType != null) {
        ConfirmationDialog(
            title = "Delete Motor Data?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                if (deleteType == "single") {
                    motorViewModel.deleteSinglePhase(motorToDelete!!)
                } else {
                    motorViewModel.deleteThreePhase(motorToDelete!!)
                }
                motorToDelete = null
                deleteType = null
            },
            onDismiss = {
                motorToDelete = null
                deleteType = null
            }
        )
    }
"""
    content = content[:insert_pos] + state_code + content[insert_pos:]

# Replace onDelete calls inside the LazyColumns
content = content.replace("onDelete = { motorViewModel.deleteSinglePhase(motor.id) }", 
                          "onDelete = { motorToDelete = motor.id; deleteType = \"single\" }")
content = content.replace("onDelete = { motorViewModel.deleteThreePhase(motor.id) }", 
                          "onDelete = { motorToDelete = motor.id; deleteType = \"three\" }")

with open("app/src/main/java/com/example/ui/screens/MotorListScreen.kt", "w") as f:
    f.write(content)
