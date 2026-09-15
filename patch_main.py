with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """                    if (FirebaseApp.getApps(this).isEmpty()) {"""
replacement = """                    val firebaseApps = FirebaseApp.getApps(this)
                    android.util.Log.d("MainActivity", "Firebase apps count: ${firebaseApps.size}")
                    if (firebaseApps.isEmpty()) {
                        android.util.Log.e("MainActivity", "Firebase is not initialized!")
                        android.widget.Toast.makeText(this, "Firebase is NOT initialized!", android.widget.Toast.LENGTH_LONG).show()"""

content = content.replace(target, replacement)

target2 = """                    } else {
                        AppUpdateWrapper {"""
replacement2 = """                    } else {
                        android.util.Log.d("MainActivity", "Firebase is initialized. Rendering AppUpdateWrapper.")
                        android.widget.Toast.makeText(this, "Firebase OK. Starting AppUpdateWrapper.", android.widget.Toast.LENGTH_SHORT).show()
                        AppUpdateWrapper {"""

content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
