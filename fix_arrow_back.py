import re

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

content = re.sub(r'Icons\.(Default|Filled|Rounded)\.ArrowBack', 'Icons.AutoMirrored.Filled.ArrowBack', content)
content = content.replace("Icons.AutoMirrored.Filled.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")
# make sure the import is there
if "import androidx.compose.material.icons.automirrored.filled.ArrowBack" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack")

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
