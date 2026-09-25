package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.CustomCategory
import com.example.model.CustomMotor
import com.example.ui.components.ErrorDialog
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomMotorScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    categoryId: String,
    id: String?,
    authViewModel: AuthViewModel
) {
    val customCategories by motorViewModel.customCategories.collectAsState()
    val customMotors by motorViewModel.customMotors.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val category = customCategories.find { it.id == categoryId } ?: CustomCategory(id = categoryId, name = "Custom Motor")

    var companyName by remember { mutableStateOf("") }
    var motorName by remember { mutableStateOf("") }
    var hp by remember { mutableStateOf("") }
    var existingPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var photoUrl by remember { mutableStateOf("") }
    var customFieldValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var existingCreatedBy by remember { mutableStateOf("") }
    var existingCreatedByName by remember { mutableStateOf("") }

    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var totalUploads by remember { mutableStateOf(0) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            existingPhotoUrls = emptyList()
        }
    }

    LaunchedEffect(id) {
        if (id != null && id != "new") {
            val motor = customMotors.find { it.id == id }
            if (motor != null) {
                companyName = motor.companyName
                motorName = motor.motorName
                hp = motor.hp
                photoUrl = motor.photoUrl
                existingPhotoUrls = motor.photoUrls.ifEmpty { if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList() }
                customFieldValues = motor.customFields
                existingCreatedBy = motor.createdBy
                existingCreatedByName = motor.createdByName
            }
        }
    }

    if (uiState is UiState.Loading) {
        LoadingDialog("Saving Motor Data...")
    } else if (isUploading) {
        LoadingDialog(if (totalUploads > 1) "Uploading Photos ($uploadProgress/$totalUploads)..." else "Uploading Photo...")
    } else if (uiState is UiState.Success) {
        val currentUser = authViewModel.currentUser.value
        val isPublic = currentUser?.role == "public"
        val successMsg = if (isPublic && id == "new") "Submitted! Pending Admin Approval" else "Saved Successfully"
        SuccessAnimationDialog(
            title = successMsg,
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
                        Text(if (id == "new" || id == null) "Add ${category.name}" else "Edit ${category.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(category.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PHOTO UPLOAD CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayUrls = if (selectedImageUris.isNotEmpty()) selectedImageUris else existingPhotoUrls
                    if (displayUrls.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayUrls.forEach { uriOrUrl ->
                                AsyncImage(
                                    model = uriOrUrl,
                                    contentDescription = "Motor Photo",
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (displayUrls.isEmpty()) "Upload Photos (Max 6)" else "Replace Photos", fontWeight = FontWeight.Bold)
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

            // CORE INFORMATION SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company Name * (Mandatory)") },
                        placeholder = { Text("e.g. Crompton, Usha, Bajaj") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = motorName,
                        onValueChange = { motorName = it },
                        label = { Text("Motor Name / Model") },
                        placeholder = { Text("e.g. Deluxe High Speed, Model 101") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = hp,
                        onValueChange = { hp = it },
                        label = { Text("HP / Power") },
                        placeholder = { Text("e.g. 0.5 HP, 1 HP, 110W") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }
            }

            // DYNAMIC CUSTOM FIELDS SECTION (PART 5)
            if (category.fields.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${category.name} Specifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        category.fields.sortedBy { it.order }.forEach { field ->
                            val currentValue = customFieldValues[field.fieldName] ?: ""
                            val isNumeric = field.fieldType == "number"

                            OutlinedTextField(
                                value = currentValue,
                                onValueChange = { newVal ->
                                    customFieldValues = customFieldValues + (field.fieldName to newVal)
                                },
                                label = {
                                    Text(if (field.isRequired) "${field.fieldName} *" else field.fieldName)
                                },
                                placeholder = {
                                    Text("Enter ${field.fieldName}")
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SUBMIT BUTTON
            Button(
                onClick = {
                    if (companyName.isBlank()) {
                        Toast.makeText(context, "Company Name is required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Check required custom fields
                    val missingRequired = category.fields.filter { it.isRequired && (customFieldValues[it.fieldName]?.isBlank() != false) }
                    if (missingRequired.isNotEmpty()) {
                        Toast.makeText(context, "Please fill required field: ${missingRequired.first().fieldName}", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val user = authViewModel.currentUser.value
                    val isPublic = user?.role == "public"
                    val actualId = if (id == "new") "" else id ?: ""

                    if (isPublic && actualId.isNotEmpty()) {
                        Toast.makeText(context, "Permission denied: Public users cannot edit motor data.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (isPublic && !category.allowPublicSubmissions) {
                        Toast.makeText(context, "Public submissions are not allowed for this category.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isUploading = true
                    val finalStatus = if (isPublic) "pending" else "approved"
                    val finalCreatedBy = if (actualId.isNotEmpty() && !isPublic) {
                        existingCreatedBy.ifEmpty { user?.id ?: "unknown" }
                    } else {
                        user?.id ?: "unknown"
                    }
                    val finalCreatedByName = if (isPublic) {
                        user?.name?.ifBlank { "Public User" } ?: "Public User"
                    } else {
                        if (actualId.isNotEmpty()) existingCreatedByName else ""
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
                            finalPhotoUrls = newUrls
                        }
                        isUploading = false

                        val customMotor = CustomMotor(
                            id = actualId,
                            categoryId = category.id,
                            categoryName = category.name,
                            companyName = companyName.trim(),
                            motorName = motorName.trim(),
                            hp = hp.trim(),
                            photoUrl = finalPhotoUrls.firstOrNull() ?: photoUrl,
                            photoUrls = finalPhotoUrls,
                            customFields = customFieldValues,
                            status = finalStatus,
                            createdBy = finalCreatedBy,
                            createdByName = finalCreatedByName
                        )
                        motorViewModel.saveCustomMotor(customMotor)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isUploading && uiState !is UiState.Loading && companyName.isNotBlank()
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Motor Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
