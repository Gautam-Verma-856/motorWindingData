with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fill = SolidColor(Color.White),\n            {", "fill = SolidColor(Color.White)\n        ) {")
content = content.replace("fill = SolidColor(Color.White),", "fill = SolidColor(Color.White)")

with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "w") as f:
    f.write(content)
