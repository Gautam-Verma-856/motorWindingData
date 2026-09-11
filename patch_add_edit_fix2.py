with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.foundation.shape.RoundedCornerShape", "RoundedCornerShape")
content = content.replace("androidx.compose.foundation.shape.CircleShape", "CircleShape")
content = content.replace("androidx.compose.foundation.background", "background")
content = content.replace("androidx.compose.ui.Alignment", "Alignment")
content = content.replace("androidx.compose.ui.draw.clip", "clip")
content = content.replace("androidx.compose.ui.graphics.vector.ImageVector", "ImageVector")
content = content.replace("androidx.compose.material.icons.Icons", "Icons")
content = content.replace("androidx.activity.result.", "")
# Note: for ActivityResultContracts, the import is there. Wait, I'll just check if it's there.

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "w") as f:
    f.write(content)
