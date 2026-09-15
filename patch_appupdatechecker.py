with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "r") as f:
    content = f.read()

import re

target1 = """                val latestVersionCode = snapshot.get("latestVersionCode")?.toString()?.toIntOrNull() ?: 0
                val latestVersionName = snapshot.getString("latestVersionName") ?: ""
                val minimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")?.toString()?.toIntOrNull() ?: 0
                val updateMessage = snapshot.getString("updateMessage") ?: "A new version of the app is available."
                val updateUrl = snapshot.getString("updateUrl") ?: ""
                val forceUpdate = snapshot.get("forceUpdate")?.toString()?.toBoolean() ?: false"""

replacement1 = """                val latestVersionCode = snapshot.get("latestVersionCode")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val latestVersionName = snapshot.getString("latestVersionName") ?: snapshot.get("latestVersionName")?.toString() ?: ""
                val minimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val updateMessage = snapshot.getString("updateMessage") ?: snapshot.get("updateMessage")?.toString() ?: "A new version of the app is available."
                val updateUrl = snapshot.getString("updateUrl") ?: snapshot.get("updateUrl")?.toString() ?: ""
                val forceUpdate = snapshot.get("forceUpdate")?.toString()?.toBoolean() ?: false"""
                
content = content.replace(target1, replacement1)

target2 = """                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                if (currentVersionCode < config.latestVersionCode) {"""

replacement2 = """                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                android.util.Log.d("AppUpdateChecker", "currentVersionCode: $currentVersionCode, latestVersionCode: $latestVersionCode, minimumSupportedVersionCode: $minimumSupportedVersionCode, forceUpdate: $forceUpdate, updateUrl: $updateUrl")

                if (currentVersionCode < config.latestVersionCode) {"""

content = content.replace(target2, replacement2)

target3 = """        } catch (e: Exception) {
            // Silently ignore errors (e.g., no network) to avoid breaking the app
        }"""

replacement3 = """        } catch (e: Exception) {
            android.util.Log.e("AppUpdateChecker", "Error checking for updates", e)
        }"""
        
content = content.replace(target3, replacement3)

with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "w") as f:
    f.write(content)
