with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    'initialFilter?.capitalize()',
    'initialFilter?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }'
)

with open("app/src/main/java/com/example/ui/screens/UserManagementScreen.kt", "w") as f:
    f.write(content)
