package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMotorScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    id: String?
) {
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(id) {
        if (id != null && id != "new") {
            if (type == "single") {
                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
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

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            Toast.makeText(context, "Motor data saved successfully.", Toast.LENGTH_SHORT).show()
            motorViewModel.resetUiState()
            navController.navigateUp()
        } else if (uiState is UiState.Error) {
            Toast.makeText(context, (uiState as UiState.Error).message, Toast.LENGTH_SHORT).show()
            motorViewModel.resetUiState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == "new") "Add Motor Data" else "Edit Motor Data") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { 
                photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text(if (photoUrl.isEmpty() && selectedImageUri == null) "Upload Motor Photo" else "Replace Photo")
            }
            
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Photo",
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            } else if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Motor Photo",
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentScale = ContentScale.Crop
                )
            }

            if (type == "single") {
                OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hp, onValueChange = { hp = it }, label = { Text("HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = capacitor, onValueChange = { capacitor = it }, label = { Text("Capacitor") }, modifier = Modifier.fillMaxWidth())
                
                Text("Running Winding", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = runningPitch, onValueChange = { runningPitch = it }, label = { Text("Pitch") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = runningTurn, onValueChange = { runningTurn = it }, label = { Text("Turn") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = runningSwg, onValueChange = { runningSwg = it }, label = { Text("SWG") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = runningWeight, onValueChange = { runningWeight = it }, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth())
                
                Text("Starting Winding", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = startingPitch, onValueChange = { startingPitch = it }, label = { Text("Pitch") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startingTurn, onValueChange = { startingTurn = it }, label = { Text("Turn") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startingSwg, onValueChange = { startingSwg = it }, label = { Text("SWG") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = startingWeight, onValueChange = { startingWeight = it }, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth())
            } else {
                OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Motor/Company Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = slot, onValueChange = { slot = it }, label = { Text("Slot") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = hp, onValueChange = { hp = it }, label = { Text("HP") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pitch, onValueChange = { pitch = it }, label = { Text("Pitch") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = turn, onValueChange = { turn = it }, label = { Text("Turn") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = swg, onValueChange = { swg = it }, label = { Text("SWG") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is UiState.Loading || isUploading) {
                CircularProgressIndicator()
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { navController.navigateUp() }, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (companyName.isBlank() || hp.isBlank()) {
                                Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            coroutineScope.launch {
                                isUploading = true
                                var finalPhotoUrl = photoUrl
                                if (selectedImageUri != null) {
                                    val uploaded = motorViewModel.uploadPhoto(selectedImageUri!!)
                                    if (uploaded != null) {
                                        finalPhotoUrl = uploaded
                                    } else {
                                        Toast.makeText(context, "Photo upload failed", Toast.LENGTH_SHORT).show()
                                        isUploading = false
                                        return@launch
                                    }
                                }
                                isUploading = false
                                
                                val actualId = if (id == "new") "" else id ?: ""
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
                                        startingWeight = startingWeight
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
                                        weight = weight
                                    )
                                    motorViewModel.saveThreePhase(motor)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Motor")
                    }
                }
            }
        }
    }
}
