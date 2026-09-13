with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "r") as f:
    content = f.read()

content = content.replace("import com.google.firebase.firestore.ktx.firestore", "import com.google.firebase.firestore.FirebaseFirestore")
content = content.replace("import com.google.firebase.ktx.Firebase\n", "")
content = content.replace("val db = Firebase.firestore", "val db = FirebaseFirestore.getInstance()")

with open("app/src/main/java/com/example/ui/components/AppUpdateChecker.kt", "w") as f:
    f.write(content)
