import re

with open("app/src/main/java/com/example/ui/screens/MotorListScreen.kt", "r") as f:
    content = f.read()

# Add state observation
content = content.replace("val uiState by motorViewModel.uiState.collectAsState()", 
                          "val uiState by motorViewModel.uiState.collectAsState()\n    val isLoadingData by motorViewModel.isLoadingData.collectAsState()")

# Find the "No motor data found." and replace it with a loading check
old_empty_1 = """                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No motor data found.")
                    }
                } else {"""
new_empty_1 = """                if (isLoadingData) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No motor data found.")
                    }
                } else {"""

content = content.replace(old_empty_1, new_empty_1)

with open("app/src/main/java/com/example/ui/screens/MotorListScreen.kt", "w") as f:
    f.write(content)
