with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.graphics.vector.PathBuilder", "import androidx.compose.ui.graphics.vector.PathBuilder\nimport androidx.compose.ui.graphics.vector.path\nimport androidx.compose.ui.unit.sp")
content = content.replace("letterSpacing = 1.2.dp", "letterSpacing = 1.2.sp")
content = content.replace("pathBuilder = {", "{")
content = content.replace("            }", "            }") # No change, just in case

with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "w") as f:
    f.write(content)
