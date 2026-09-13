with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "r") as f:
    content = f.read()

content = content.replace("            }\n        )\n    }.build()", "            }\n    }.build()")

with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "w") as f:
    f.write(content)
