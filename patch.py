import re

with open('app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt', 'r') as f:
    content = f.read()

# Imports
if 'import androidx.compose.foundation.horizontalScroll' not in content:
    content = content.replace('import androidx.compose.foundation.verticalScroll', 'import androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.horizontalScroll')

# photoUrl -> existingPhotoUrls
content = content.replace('var photoUrl by remember { mutableStateOf("") }', 
'''var existingPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var photoUrl by remember { mutableStateOf("") }''')

# selectedImageUri -> selectedImageUris
content = content.replace(
'''    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }''',
'''    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var totalUploads by remember { mutableStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            existingPhotoUrls = emptyList() // clear existing when picking new
        }
    }''')

# LaunchedEffect Single
content = content.replace(
'''                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
                    photoUrl = motor.photoUrl''',
'''                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
                    existingPhotoUrls = motor.photoUrls.ifEmpty { if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList() }
                    photoUrl = motor.photoUrl''')

# LaunchedEffect ThreePhase
content = content.replace(
'''                val motor = threePhaseMotors.find { it.id == id }
                if (motor != null) {
                    photoUrl = motor.photoUrl''',
'''                val motor = threePhaseMotors.find { it.id == id }
                if (motor != null) {
                    existingPhotoUrls = motor.photoUrls.ifEmpty { if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList() }
                    photoUrl = motor.photoUrl''')

# LoadingDialog
content = content.replace(
'''    } else if (isUploading) {
        LoadingDialog("Uploading Photo...")
    } else if (uiState is UiState.Success) {''',
'''    } else if (isUploading) {
        LoadingDialog(if (totalUploads > 1) "Uploading Photos ($uploadProgress/$totalUploads)..." else "Uploading Photo...")
    } else if (uiState is UiState.Success) {''')

# Photo Section UI
content = content.replace(
'''                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Motor Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (photoUrl.isEmpty() && selectedImageUri == null) "Upload Photo" else "Replace Photo", fontWeight = FontWeight.Bold)
                    }''',
'''                    val displayUrls = if (selectedImageUris.isNotEmpty()) selectedImageUris else existingPhotoUrls
                    if (displayUrls.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            displayUrls.forEach { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Motor Photo",
                                    modifier = Modifier.size(200.dp).clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (displayUrls.isEmpty()) "Upload Photos (Max 6)" else "Replace Photos", fontWeight = FontWeight.Bold)
                    }''')


# Upload Logic
content = content.replace(
'''                        var finalPhotoUrl = photoUrl
                        if (selectedImageUri != null) {
                            val uploadResult = motorViewModel.uploadPhoto(context, selectedImageUri!!)
                            if (uploadResult.isSuccess) {
                                finalPhotoUrl = uploadResult.getOrNull() ?: ""
                            } else {
                                isUploading = false
                                val errorMsg = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                                Toast.makeText(context, "Photo upload failed: $errorMsg", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                        }
                        isUploading = false
                        
                        if (type == "single") {
                            val motor = SinglePhaseMotor(
                                id = actualId,
                                photoUrl = finalPhotoUrl,
                                companyName = companyName,
                                hp = hp,
                                capacitor = capacitor,
                                runningPitch = runningPitch,
                                runningTurn = runningTurn,
                                runningSwg = runningSwg,
                                runningWeight = runningWeight,
                                startingPitch = startingPitch,
                                startingTurn = startingTurn,
                                startingSwg = startingSwg,
                                startingWeight = startingWeight,
                                status = finalStatus,
                                createdBy = finalCreatedBy
                            )
                            motorViewModel.saveSinglePhase(motor)
                        } else {
                            val motor = ThreePhaseMotor(
                                id = actualId,
                                photoUrl = finalPhotoUrl,
                                name = companyName,
                                slot = slot,
                                hp = hp,
                                pitch = pitch,
                                turn = turn,
                                swg = swg,
                                weight = weight,
                                status = finalStatus,
                                createdBy = finalCreatedBy
                            )
                            motorViewModel.saveThreePhase(motor)
                        }''',
'''                        var finalPhotoUrls = existingPhotoUrls
                        if (selectedImageUris.isNotEmpty()) {
                            val newUrls = mutableListOf<String>()
                            totalUploads = selectedImageUris.size
                            uploadProgress = 0
                            for (uri in selectedImageUris) {
                                val uploadResult = motorViewModel.uploadPhoto(context, uri)
                                if (uploadResult.isSuccess) {
                                    newUrls.add(uploadResult.getOrNull() ?: "")
                                    uploadProgress++
                                } else {
                                    isUploading = false
                                    val errorMsg = uploadResult.exceptionOrNull()?.message ?: "Unknown error"
                                    Toast.makeText(context, "Photo upload failed: $errorMsg", Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                            }
                            finalPhotoUrls = newUrls
                        }
                        isUploading = false
                        
                        if (type == "single") {
                            val motor = SinglePhaseMotor(
                                id = actualId,
                                photoUrl = finalPhotoUrls.firstOrNull() ?: photoUrl,
                                photoUrls = finalPhotoUrls,
                                companyName = companyName,
                                hp = hp,
                                capacitor = capacitor,
                                runningPitch = runningPitch,
                                runningTurn = runningTurn,
                                runningSwg = runningSwg,
                                runningWeight = runningWeight,
                                startingPitch = startingPitch,
                                startingTurn = startingTurn,
                                startingSwg = startingSwg,
                                startingWeight = startingWeight,
                                status = finalStatus,
                                createdBy = finalCreatedBy
                            )
                            motorViewModel.saveSinglePhase(motor)
                        } else {
                            val motor = ThreePhaseMotor(
                                id = actualId,
                                photoUrl = finalPhotoUrls.firstOrNull() ?: photoUrl,
                                photoUrls = finalPhotoUrls,
                                name = companyName,
                                slot = slot,
                                hp = hp,
                                pitch = pitch,
                                turn = turn,
                                swg = swg,
                                weight = weight,
                                status = finalStatus,
                                createdBy = finalCreatedBy
                            )
                            motorViewModel.saveThreePhase(motor)
                        }''')

with open('app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt', 'w') as f:
    f.write(content)

