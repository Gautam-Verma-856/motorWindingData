with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "r") as f:
    content = f.read()

content = content.replace("Icons.Default.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")
content = content.replace("Icons.Filled.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")

with open("app/src/main/java/com/example/ui/screens/AuthScreens.kt", "w") as f:
    f.write(content)
