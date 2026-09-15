import sys

with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "r") as f:
    content = f.read()

target = """                Toast.makeText(context, "Update check: current=$currentVersionCode, latest=$latestVersionCode", Toast.LENGTH_LONG).show()

                if (currentVersionCode < config.latestVersionCode) {"""

replacement = """                android.widget.Toast.makeText(context, "Update check: current=$currentVersionCode, latest=$latestVersionCode", android.widget.Toast.LENGTH_LONG).show()

                if (currentVersionCode < config.latestVersionCode) {"""

content = content.replace(target, replacement)

target2 = """            } else {
                android.util.Log.d("AppUpdateChecker", "Update config document does not exist")
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateChecker", "Firebase update check failed", e)
        }"""

replacement2 = """            } else {
                android.util.Log.d("AppUpdateChecker", "Update config document does not exist")
                android.widget.Toast.makeText(context, "Update check: document does not exist", android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateChecker", "Firebase update check failed", e)
            android.widget.Toast.makeText(context, "Update check failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }"""
        
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "w") as f:
    f.write(content)
