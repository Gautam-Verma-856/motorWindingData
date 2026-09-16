with open("app/build.gradle.kts", "r") as f:
    content = f.read()

target = """    val envVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 3
    val envVersionName = System.getenv("VERSION_NAME") ?: "3.0"
    versionCode = envVersionCode
    versionName = envVersionName"""

replacement = """    val githubRunNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
    val calcVersionCode = if (githubRunNumber != null) githubRunNumber + 3 else 3
    val calcVersionName = "${calcVersionCode}.0"

    versionCode = calcVersionCode
    versionName = calcVersionName"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/build.gradle.kts", "w") as f:
        f.write(content)
    print("Patched app/build.gradle.kts successfully")
else:
    print("Target not found")
