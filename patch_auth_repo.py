with open("app/src/main/java/com/example/repository/AuthRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    'suspend fun registerPublicUser(name: String, email: String, pass: String): Result<User>',
    'suspend fun registerPublicUser(name: String, email: String, mobile: String, pass: String): Result<User>'
)
content = content.replace(
    'id = uid,\n                name = name,\n                email = email,\n                role = "public",\n                accountStatus = "active"',
    'id = uid,\n                name = name,\n                email = email,\n                mobile = mobile,\n                role = "public",\n                accountStatus = "pending"'
)
with open("app/src/main/java/com/example/repository/AuthRepository.kt", "w") as f:
    f.write(content)
