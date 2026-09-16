with open('app/src/main/java/com/example/ui/components/AppUpdateChecker.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.material.icons.rounded.CheckCircle" not in content:
    content = content.replace("import androidx.compose.material.icons.rounded.SystemUpdate", "import androidx.compose.material.icons.rounded.SystemUpdate\nimport androidx.compose.material.icons.rounded.CheckCircle")

with open('app/src/main/java/com/example/ui/components/AppUpdateChecker.kt', 'w') as f:
    f.write(content)
