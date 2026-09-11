with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith("import androidx.compose.ui.Alignment"):
        continue
    if line.startswith("import androidx.compose.ui.draw.clip"):
        continue
    if line.startswith("import androidx.compose.foundation.background"):
        continue
    if line.startswith("import androidx.compose.foundation.shape."):
        continue
    new_lines.append(line)

imports = """
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
"""

content = "".join(new_lines)
content = content.replace("import androidx.compose.foundation.layout.*", imports)

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "w") as f:
    f.write(content)
