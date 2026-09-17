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
    }

    LaunchedEffect(id) {
        if (id != null && id != "new") {
            if (type == "single") {
                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
                    existingPhotoUrls = motor.photoUrls.ifEmpty { if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList() }
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
                    val displayUrls = if (selectedImageUris.isNotEmpty()) selectedImageUris else existingPhotoUrls
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
                    }
                }
            }

            if (type == "single") {
                FormSection("Motor Information", Icons.Rounded.Info) {
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
                FormSection("Motor Information", Icons.Rounded.Info) {
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
                    isUploading = true
                    val user = authViewModel.currentUser.value
                    val isPublic = user?.role == "public"
                    val finalStatus = if (isPublic) "pending" else "approved"
                    val finalCreatedBy = user?.id ?: "unknown"
                    
                    val actualId = if (id == "new") "" else id ?: ""

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
