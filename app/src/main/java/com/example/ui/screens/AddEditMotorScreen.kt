package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import com.example.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.ui.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMotorScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    id: String?,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("public_login") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var existingPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var photoUrl by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var hp by remember { mutableStateOf("") }
    var capacitor by remember { mutableStateOf("") }
    var runningPitch by remember { mutableStateOf("") }
    var runningTurn by remember { mutableStateOf("") }
    var runningSwg by remember { mutableStateOf("") }
    var runningWeight by remember { mutableStateOf("") }
    var startingPitch by remember { mutableStateOf("") }
    var startingTurn by remember { mutableStateOf("") }
    var startingSwg by remember { mutableStateOf("") }
    var startingWeight by remember { mutableStateOf("") }

    var slot by remember { mutableStateOf("") }
    var pitch by remember { mutableStateOf("") }
    var turn by remember { mutableStateOf("") }
    var swg by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingCreatedBy by remember { mutableStateOf("") }
    var existingCreatedByName by remember { mutableStateOf("") }
    var existingStatus by remember { mutableStateOf("approved") }
    var category by remember { mutableStateOf(if (type == "single") "Self Priming (Tullu Pump)" else "Monoblock") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var totalUploads by remember { mutableStateOf(0) }

    val totalPhotos = existingPhotoUrls.size + selectedImageUris.size

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remainingSlots = 6 - (existingPhotoUrls.size + selectedImageUris.size)
            if (remainingSlots > 0) {
                selectedImageUris = selectedImageUris + uris.take(remainingSlots)
            } else {
                Toast.makeText(context, "Maximum 6 photos allowed. Remove existing photos first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(id) {
        if (id != null && id != "new") {
            if (type == "single") {
                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
                    existingPhotoUrls = motor.photoUrls.ifEmpty { if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList() }
                    existingCreatedBy = motor.createdBy
                    existingCreatedByName = motor.createdByName
                    existingStatus = motor.status
                    if (motor.category.isNotEmpty()) {
                        category = motor.category
                    }
                    photoUrl = motor.photoUrl
                    companyName = motor.companyName
                    hp = motor.hp
                    capacitor = motor.capacitor
                    runningPitch = motor.runningPitch
                    runningTurn = motor.runningTurn
                    runningSwg = motor.runningSwg
                    runningWeight = motor.runningWeight
                    startingPitch = motor.startingPitch
                    startingTurn = motor.startingTurn
                    startingSwg = motor.startingSwg
                    startingWeight = motor.startingWeight
                }
            } else {
                val motor = threePhaseMotors.find { it.id == id }
                if (motor != null) {
                    existingPhotoUrls = motor.photoUrls.ifEmpty { if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList() }
                    existingCreatedBy = motor.createdBy
                    existingCreatedByName = motor.createdByName
                    existingStatus = motor.status
                    if (motor.category.isNotEmpty()) {
                        category = motor.category
                    }
                    photoUrl = motor.photoUrl
                    companyName = motor.name
                    slot = motor.slot
                    hp = motor.hp
                    pitch = motor.pitch
                    turn = motor.turn
                    swg = motor.swg
                    weight = motor.weight
                }
            }
        }
    }

    // State dialogs handled in Compose UI tree


    if (uiState is UiState.Loading) {
        LoadingDialog("Saving Motor Data...")
    } else if (isUploading) {
        LoadingDialog(if (totalUploads > 1) "Uploading Photos ($uploadProgress/$totalUploads)..." else "Uploading Photo...")
    } else if (uiState is UiState.Success) {
        val currentUser = authViewModel.currentUser.value
        val isPublic = currentUser?.role == "public"
        val title = if (isPublic) "Submitted Successfully!" else "Motor Data Saved Successfully"
        val msg = if (isPublic) "Your motor data has been sent for Admin approval." else ""
        SuccessAnimationDialog(
            title = title,
            message = msg,
            onDismiss = {
                motorViewModel.resetUiState()
                navController.navigateUp()
            }
        )
    } else if (uiState is UiState.Error) {
        ErrorDialog(
            message = (uiState as UiState.Error).message,
            onDismiss = { motorViewModel.resetUiState() }
        )
    }
    Scaffold(
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (totalPhotos > 0) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            existingPhotoUrls.forEach { url ->
                                Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp))) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Motor Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { existingPhotoUrls = existingPhotoUrls.filter { it != url } },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            selectedImageUris.forEach { uri ->
                                Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp))) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Motor Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImageUris = selectedImageUris.filter { it != uri } },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
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
                        onClick = {
                            val remaining = 6 - totalPhotos
                            if (remaining > 0) {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                Toast.makeText(context, "Maximum 6 photos allowed. Remove a photo to add another.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = totalPhotos < 6,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (totalPhotos == 0) "Add Photos (Max 6)" else "Add Photos ($totalPhotos/6)", fontWeight = FontWeight.Bold)
                    }

                    val safeDiagnostic = remember { motorViewModel.getSafeDiagnostic() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when (safeDiagnostic.status) {
                                        com.example.repository.MotorRepository.SafeKeyStatus.KEY_PRESENT -> Color(0xFF2E7D32)
                                        com.example.repository.MotorRepository.SafeKeyStatus.KEY_LITERAL_PLACEHOLDER -> Color(0xFFED6C02)
                                        com.example.repository.MotorRepository.SafeKeyStatus.KEY_EMPTY -> Color(0xFFD32F2F)
                                    },
                                    CircleShape
                                )
                        )
                        Text(
                            text = "ImgBB Status: ${safeDiagnostic.status} (length: ${safeDiagnostic.length})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (type == "single") {
                FormSection("Motor Category & Info", Icons.Rounded.Category) {
                    Text("Select Motor Category *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val singleCategories = listOf("Self Priming (Tullu Pump)", "Submersible Motor", "Pandubi", "General / Other Motors")
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        singleCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontWeight = if (category == cat) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    PremiumTextField(value = companyName, onValueChange = { companyName = it }, label = "Company Name")
                    PremiumTextField(value = hp, onValueChange = { hp = it }, label = "HP")
                    PremiumTextField(value = capacitor, onValueChange = { capacitor = it }, label = "Capacitor")
                }
                
                FormSection("Running Winding", Icons.Rounded.Speed) {
                    PremiumTextField(value = runningPitch, onValueChange = { runningPitch = it }, label = "Pitch")
                    PremiumTextField(value = runningTurn, onValueChange = { runningTurn = it }, label = "Turn")
                    PremiumTextField(value = runningSwg, onValueChange = { runningSwg = it }, label = "SWG")
                    PremiumTextField(value = runningWeight, onValueChange = { runningWeight = it }, label = "Weight")
                }
                
                FormSection("Starting Winding", Icons.Rounded.Bolt) {
                    PremiumTextField(value = startingPitch, onValueChange = { startingPitch = it }, label = "Pitch")
                    PremiumTextField(value = startingTurn, onValueChange = { startingTurn = it }, label = "Turn")
                    PremiumTextField(value = startingSwg, onValueChange = { startingSwg = it }, label = "SWG")
                    PremiumTextField(value = startingWeight, onValueChange = { startingWeight = it }, label = "Weight")
                }
            } else {
                FormSection("Motor Category & Info", Icons.Rounded.Category) {
                    Text("Select Motor Category *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val threeCategories = listOf("Monoblock", "Submersible", "Pandubi", "General / Other Motors")
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        threeCategories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontWeight = if (category == cat) FontWeight.Bold else FontWeight.Normal) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    PremiumTextField(value = companyName, onValueChange = { companyName = it }, label = "Motor/Company Name")
                    PremiumTextField(value = slot, onValueChange = { slot = it }, label = "Slot")
                    PremiumTextField(value = hp, onValueChange = { hp = it }, label = "HP")
                }
                
                FormSection("Winding Data", Icons.Rounded.Label) {
                    PremiumTextField(value = pitch, onValueChange = { pitch = it }, label = "Pitch")
                    PremiumTextField(value = turn, onValueChange = { turn = it }, label = "Turn")
                    PremiumTextField(value = swg, onValueChange = { swg = it }, label = "SWG")
                    PremiumTextField(value = weight, onValueChange = { weight = it }, label = "Weight")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (companyName.isBlank()) {
                        Toast.makeText(context, if (type == "single") "Please enter Company Name" else "Please enter Motor/Company Name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val user = authViewModel.currentUser.value
                    val isPublic = user?.role == "public"
                    val actualId = if (id == "new") "" else id ?: ""
                    
                    if (isPublic && actualId.isNotEmpty()) {
                        Toast.makeText(context, "Permission denied: Public users cannot edit motor data.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true
                    val authUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val currentUid = user?.id?.ifBlank { authUid } ?: authUid
                    val currentUserName = user?.name?.ifBlank { "Public User" } ?: "Public User"

                    val finalStatus = if (isPublic) "pending" else if (actualId.isNotEmpty()) existingStatus.ifBlank { "approved" } else "approved"
                    val finalCreatedBy = if (isPublic) {
                        currentUid
                    } else {
                        if (actualId.isNotEmpty()) existingCreatedBy.ifEmpty { currentUid } else currentUid
                    }
                    val finalCreatedByName = if (isPublic) {
                        currentUserName
                    } else {
                        if (actualId.isNotEmpty()) existingCreatedByName else currentUserName
                    }

                    coroutineScope.launch {
                        var finalPhotoUrls = existingPhotoUrls
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
                            finalPhotoUrls = existingPhotoUrls + newUrls
                        }
                        isUploading = false
                        
                        if (type == "single") {
                            val motor = SinglePhaseMotor(
                                id = actualId,
                                category = category,
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
                                createdBy = finalCreatedBy,
                                createdByName = finalCreatedByName
                            )
                            motorViewModel.saveSinglePhase(motor)
                        } else {
                            val motor = ThreePhaseMotor(
                                id = actualId,
                                category = category,
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
                                createdBy = finalCreatedBy,
                                createdByName = finalCreatedByName
                            )
                            motorViewModel.saveThreePhase(motor)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isUploading && uiState !is UiState.Loading
            ) {
                Text("Submit Motor Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
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
