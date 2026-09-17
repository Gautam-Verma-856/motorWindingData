with open('app/src/main/java/com/example/ui/screens/MotorDetailScreen.kt', 'r') as f:
    content = f.read()

if 'import androidx.compose.foundation.horizontalScroll' not in content:
    content = content.replace('import androidx.compose.foundation.verticalScroll', 'import androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.horizontalScroll')

single_target = '''                    if (motor.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = motor.photoUrl,
                            contentDescription = "Motor Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }'''
                    
single_replace = '''                    val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()
                    if (displayUrls.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayUrls.forEach { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Motor Photo",
                                    modifier = Modifier
                                        .height(250.dp)
                                        .fillMaxWidth(if (displayUrls.size == 1) 1f else 0.85f)
                                        .clip(RoundedCornerShape(24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }'''

content = content.replace(single_target, single_replace)

with open('app/src/main/java/com/example/ui/screens/MotorDetailScreen.kt', 'w') as f:
    f.write(content)

