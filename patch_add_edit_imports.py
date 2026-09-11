import re

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    content = f.read()

# Fix Unresolved reference 'FontWeight'
if "import androidx.compose.ui.text.font.FontWeight" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.text.font.FontWeight")

# Fix Icons.Filled.ArrowBack to Icons.AutoMirrored.Filled.ArrowBack
if "import androidx.compose.material.icons.automirrored.filled.ArrowBack" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.ArrowBack", "import androidx.compose.material.icons.automirrored.filled.ArrowBack")
    content = content.replace("Icons.Filled.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")

# Fix androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack
content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")

# Fix rounded icons
if "import androidx.compose.material.icons.rounded.*" not in content:
    content = content.replace("import androidx.compose.material.icons.automirrored.filled.ArrowBack", "import androidx.compose.material.icons.automirrored.filled.ArrowBack\nimport androidx.compose.material.icons.rounded.*")

content = content.replace("androidx.compose.material.icons.Icons.Rounded.PhotoCamera", "Icons.Rounded.PhotoCamera")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.CloudUpload", "Icons.Rounded.CloudUpload")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.Info", "Icons.Rounded.Info")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.Speed", "Icons.Rounded.Speed")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.Bolt", "Icons.Rounded.Bolt")
content = content.replace("androidx.compose.material.icons.Icons.Rounded.Label", "Icons.Rounded.Label")

# Fix androidx.compose.ui.draw.clip
if "import androidx.compose.ui.draw.clip" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip")

content = content.replace("androidx.compose.ui.draw.clip", "clip")

# Fix androidx.compose.foundation.background
if "import androidx.compose.foundation.background" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.background")

content = content.replace("androidx.compose.foundation.background", "background")

# Fix androidx.compose.foundation.shape.RoundedCornerShape
if "import androidx.compose.foundation.shape.RoundedCornerShape" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.shape.RoundedCornerShape")

content = content.replace("androidx.compose.foundation.shape.RoundedCornerShape", "RoundedCornerShape")

# Fix androidx.compose.foundation.shape.CircleShape
if "import androidx.compose.foundation.shape.CircleShape" not in content:
    content = content.replace("import androidx.compose.foundation.layout.*", "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.shape.CircleShape")
    
content = content.replace("androidx.compose.foundation.shape.CircleShape", "CircleShape")

# Fix androidx.compose.ui.Alignment
if "import androidx.compose.ui.Alignment" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.Alignment")

content = content.replace("androidx.compose.ui.Alignment", "Alignment")

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "w") as f:
    f.write(content)
