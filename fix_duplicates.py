import re

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    content = f.read()

parts = content.split("@Composable\nfun FormSection")
body = parts[0]

correct_suffix = """@Composable
fun FormSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true
    )
}
"""

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "w") as f:
    f.write(body + correct_suffix)
