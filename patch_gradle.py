with open("app/build.gradle.kts", "r") as f:
    content = f.read()

target = """    versionCode = 3
    versionName = "3.0\""""

replacement = """    val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 3
    val envVersionName = System.getenv("VERSION_NAME") ?: "3.0"
    versionCode = envVersionCode
    versionName = envVersionName"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
    print("Patched app/build.gradle.kts")
else:
    print("Target not found")
