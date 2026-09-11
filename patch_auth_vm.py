with open("app/src/main/java/com/example/viewmodel/AuthViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val res = repo.registerPublicUser(name, email, pass)',
    'val res = repo.registerPublicUser(name, email, mobile, pass)'
)
with open("app/src/main/java/com/example/viewmodel/AuthViewModel.kt", "w") as f:
    f.write(content)
