with open("app/src/main/java/com/example/model/User.kt", "r") as f:
    content = f.read()
content = content.replace('val email: String = "",', 'val email: String = "",\n    val mobile: String = "",')
with open("app/src/main/java/com/example/model/User.kt", "w") as f:
    f.write(content)
