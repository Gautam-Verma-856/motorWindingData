with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.navigation.NavGraph", "import com.example.ui.navigation.NavGraph\nimport com.example.ui.components.AppUpdateWrapper")

content = content.replace(
"""                    } else {
                        NavGraph()
                    }""",
"""                    } else {
                        AppUpdateWrapper {
                            NavGraph()
                        }
                    }"""
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
