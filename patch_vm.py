import re

with open("app/src/main/java/com/example/viewmodel/MotorViewModel.kt", "r") as f:
    content = f.read()

# Change UiState.Success to data class
content = content.replace("object Success : UiState()", "data class Success(val message: String = \"\") : UiState()")
content = content.replace("UiState.Success", "UiState.Success()")

# Wait, the replace will make data class Success(val message: String = "") : UiState() into data class Success()(val message: String = "") : UiState() if I just blindly replace UiState.Success
