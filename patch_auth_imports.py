with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

imports = """
import com.example.ui.components.ErrorDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.AuthViewModel
"""
content = content.replace("import com.example.viewmodel.AuthViewModel", imports)

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
