import re

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    content = f.read()

# We need to preserve the state initialization up to the scaffold.
scaffold_start = "    Scaffold("
parts = content.split(scaffold_start)
header = parts[0]

new_scaffold = """    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (id == "new") "Add Motor Data" else "Edit Motor Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(if (type == "single") "Single Phase Motor" else "Three Phase Motor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // PHOTO SECTION
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp).androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else if (photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Motor Photo",
                            modifier = Modifier.fillMaxWidth().height(200.dp).androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (photoUrl.isEmpty() && selectedImageUri == null) "Upload Photo" else "Replace Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (type == "single") {
                FormSection("Motor Information", androidx.compose.material.icons.Icons.Rounded.Info) {
                    PremiumTextField(value = companyName, onValueChange = { companyName = it }, label = "Company Name")
                    PremiumTextField(value = hp, onValueChange = { hp = it }, label = "HP")
                    PremiumTextField(value = capacitor, onValueChange = { capacitor = it }, label = "Capacitor")
                }
                
                FormSection("Running Winding", androidx.compose.material.icons.Icons.Rounded.Speed) {
                    PremiumTextField(value = runningPitch, onValueChange = { runningPitch = it }, label = "Pitch")
                    PremiumTextField(value = runningTurn, onValueChange = { runningTurn = it }, label = "Turn")
                    PremiumTextField(value = runningSwg, onValueChange = { runningSwg = it }, label = "SWG")
                    PremiumTextField(value = runningWeight, onValueChange = { runningWeight = it }, label = "Weight")
                }
                
                FormSection("Starting Winding", androidx.compose.material.icons.Icons.Rounded.Bolt) {
                    PremiumTextField(value = startingPitch, onValueChange = { startingPitch = it }, label = "Pitch")
                    PremiumTextField(value = startingTurn, onValueChange = { startingTurn = it }, label = "Turn")
                    PremiumTextField(value = startingSwg, onValueChange = { startingSwg = it }, label = "SWG")
                    PremiumTextField(value = startingWeight, onValueChange = { startingWeight = it }, label = "Weight")
                }
            } else {
                FormSection("Motor Information", androidx.compose.material.icons.Icons.Rounded.Info) {
                    PremiumTextField(value = companyName, onValueChange = { companyName = it }, label = "Motor/Company Name")
                    PremiumTextField(value = slot, onValueChange = { slot = it }, label = "Slot")
                    PremiumTextField(value = hp, onValueChange = { hp = it }, label = "HP")
                }
                
                FormSection("Winding Data", androidx.compose.material.icons.Icons.Rounded.Label) {
                    PremiumTextField(value = pitch, onValueChange = { pitch = it }, label = "Pitch")
                    PremiumTextField(value = turn, onValueChange = { turn = it }, label = "Turn")
                    PremiumTextField(value = swg, onValueChange = { swg = it }, label = "SWG")
                    PremiumTextField(value = weight, onValueChange = { weight = it }, label = "Weight")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isUploading = true
                    val user = authViewModel.currentUser.value
                    val isPublic = user?.role == "public"
                    val finalStatus = if (isPublic) "pending" else "approved"
                    val finalCreatedBy = user?.id ?: "unknown"
                    
                    val actualId = if (id == "new") "" else id ?: ""

                    coroutineScope.launch {
                        var finalPhotoUrl = photoUrl
                        if (selectedImageUri != null) {
                            val uploaded = motorViewModel.uploadPhoto(selectedImageUri!!)
                            if (uploaded != null) {
                                finalPhotoUrl = uploaded
                            } else {
                                Toast.makeText(context, "Photo upload failed. Proceeding without photo.", Toast.LENGTH_SHORT).show()
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
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                enabled = !isUploading && uiState !is UiState.Loading
            ) {
                Text("Submit Motor Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
    f.write(header + new_scaffold)
