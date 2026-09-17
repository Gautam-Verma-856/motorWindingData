with open('app/src/main/java/com/example/ui/screens/MotorDetailScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''                                    modifier = Modifier
                                        .height(250.dp)
                                        .fillMaxWidth(if (displayUrls.size == 1) 1f else 0.85f)
                                        .clip(RoundedCornerShape(24.dp)),''',
'''                                    modifier = Modifier
                                        .height(250.dp)
                                        .width(if (displayUrls.size == 1) 360.dp else 280.dp)
                                        .clip(RoundedCornerShape(24.dp)),''')

with open('app/src/main/java/com/example/ui/screens/MotorDetailScreen.kt', 'w') as f:
    f.write(content)

