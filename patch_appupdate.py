import sys

with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "r") as f:
    content = f.read()

# Make sure com.google.firebase.FirebaseApp is imported. If not, add it.
if "import com.google.firebase.FirebaseApp" not in content:
    content = content.replace("import com.google.firebase.firestore.FirebaseFirestore", "import com.google.firebase.FirebaseApp\nimport com.google.firebase.firestore.FirebaseFirestore")

target = """    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("app_config").document("update").get().await()
            if (snapshot.exists()) {
                val latestVersionCode = snapshot.get("latestVersionCode")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val latestVersionName = snapshot.getString("latestVersionName") ?: snapshot.get("latestVersionName")?.toString() ?: ""
                val minimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val updateMessage = snapshot.getString("updateMessage") ?: snapshot.get("updateMessage")?.toString() ?: "A new version of the app is available."
                val updateUrl = snapshot.getString("updateUrl") ?: snapshot.get("updateUrl")?.toString() ?: ""
                val forceUpdate = snapshot.get("forceUpdate")?.toString()?.toBoolean() ?: false

                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                android.util.Log.d("AppUpdateChecker", "currentVersionCode: $currentVersionCode, latestVersionCode: $latestVersionCode, minimumSupportedVersionCode: $minimumSupportedVersionCode, forceUpdate: $forceUpdate, updateUrl: $updateUrl")

                if (currentVersionCode < config.latestVersionCode) {
                    updateConfig = config
                    showDialog = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateChecker", "Error checking for updates", e)
        }
    }"""

replacement = """    LaunchedEffect(Unit) {
        android.util.Log.d("AppUpdateChecker", "Starting update check")
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("app_config").document("update").get().await()
            
            val projectId = com.google.firebase.FirebaseApp.getInstance().options.projectId
            android.util.Log.d("AppUpdateChecker", "snapshot.exists(): ${snapshot.exists()}")
            android.util.Log.d("AppUpdateChecker", "Firebase project ID: $projectId")
            android.util.Log.d("AppUpdateChecker", "currentVersionCode: $currentVersionCode")
            
            if (snapshot.exists()) {
                val rawLatestVersionCode = snapshot.get("latestVersionCode")
                val rawLatestVersionName = snapshot.get("latestVersionName")
                val rawMinimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")
                val rawUpdateMessage = snapshot.get("updateMessage")
                val rawUpdateUrl = snapshot.get("updateUrl")
                val rawForceUpdate = snapshot.get("forceUpdate")
                
                android.util.Log.d("AppUpdateChecker", "latestVersionCode value: $rawLatestVersionCode, type: ${rawLatestVersionCode?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "latestVersionName value: $rawLatestVersionName, type: ${rawLatestVersionName?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "minimumSupportedVersionCode value: $rawMinimumSupportedVersionCode, type: ${rawMinimumSupportedVersionCode?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "updateMessage value: $rawUpdateMessage, type: ${rawUpdateMessage?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "updateUrl value: $rawUpdateUrl, type: ${rawUpdateUrl?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "forceUpdate value: $rawForceUpdate, type: ${rawForceUpdate?.javaClass?.name}")

                val latestVersionCode = rawLatestVersionCode?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val latestVersionName = rawLatestVersionName?.toString() ?: ""
                val minimumSupportedVersionCode = rawMinimumSupportedVersionCode?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val updateMessage = rawUpdateMessage?.toString() ?: "A new version of the app is available."
                val updateUrl = rawUpdateUrl?.toString() ?: ""
                val forceUpdate = rawForceUpdate?.toString()?.toBooleanStrictOrNull() ?: rawForceUpdate?.toString()?.toBoolean() ?: false

                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                Toast.makeText(context, "Update check: current=$currentVersionCode, latest=$latestVersionCode", Toast.LENGTH_LONG).show()

                if (currentVersionCode < config.latestVersionCode) {
                    updateConfig = config
                    showDialog = true
                }
            } else {
                android.util.Log.d("AppUpdateChecker", "Update config document does not exist")
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateChecker", "Firebase update check failed", e)
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "w") as f:
        f.write(content)
    print("Successfully patched.")
else:
    print("Target not found. Check exact string match.")
    sys.exit(1)
