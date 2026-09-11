with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.rounded.*")

content = content.replace("Icons.Default.HourglassEmpty", "Icons.Rounded.HourglassEmpty")
content = content.replace("Icons.Default.Cancel", "Icons.Rounded.Cancel")
content = content.replace("Icons.Default.Block", "Icons.Rounded.Block")
content = content.replace("Icons.Default.Phone", "Icons.Rounded.Phone")
content = content.replace("Icons.Default.Person", "Icons.Rounded.Person")

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
