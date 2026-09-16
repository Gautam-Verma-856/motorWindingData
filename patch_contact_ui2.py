with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "r") as f:
    content = f.read()

content = content.replace("    ).apply {", "    ).apply {")
content = content.replace("        }\n    }\n\nval InstagramIcon", "        }\n    }.build()\n\nval InstagramIcon")
content = content.replace("        }\n    }\n\n@OptIn", "        }\n    }.build()\n\n@OptIn")

with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "w") as f:
    f.write(content)

print("Fixed")
