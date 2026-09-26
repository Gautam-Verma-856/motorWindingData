package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.CustomCategory
import com.example.model.CustomMotor
import com.example.model.User
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ErrorDialog
import com.example.ui.components.FullScreenImageViewer
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMotorListScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    categoryId: String,
    currentUser: User?
) {
    val customCategories by motorViewModel.customCategories.collectAsState()
    val customMotors by motorViewModel.customMotors.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()

    val category = customCategories.find { it.id == categoryId } ?: CustomCategory(id = categoryId, name = "Custom Motor")
    val role = currentUser?.role ?: "public"

    var selectedCompany by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var motorToDelete by remember { mutableStateOf<String?>(null) }
    var motorToReject by remember { mutableStateOf<String?>(null) }
    var fullScreenPhotos by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenInitialIndex by remember { mutableIntStateOf(0) }

    val authUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val targetUserId = currentUser?.id?.ifBlank { authUid } ?: authUid

    LaunchedEffect(categoryId, role, targetUserId) {
        motorViewModel.loadData(isAdmin = (role == "admin"), userId = targetUserId)
    }

    if (uiState is UiState.Loading) {
        LoadingDialog("Processing...")
    } else if (uiState is UiState.Success) {
        val action = (uiState as UiState.Success).message
        val title = when (action) {
            "delete_custom_motor" -> "Deleted Successfully"
            "approve" -> "Approved Successfully"
            "reject" -> "Submission Rejected"
            else -> "Success"
        }
        SuccessAnimationDialog(
            title = title,
            onDismiss = { motorViewModel.resetUiState() }
        )
    } else if (uiState is UiState.Error) {
        ErrorDialog(
            message = (uiState as UiState.Error).message,
            onDismiss = { motorViewModel.resetUiState() }
        )
    }

    if (motorToDelete != null) {
        ConfirmationDialog(
            title = "Delete Motor Data?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                motorViewModel.deleteCustomMotor(motorToDelete!!)
                motorToDelete = null
            },
            onDismiss = { motorToDelete = null }
        )
    }

    if (motorToReject != null) {
        com.example.ui.components.RejectionReasonDialog(
            onConfirm = { reason ->
                motorViewModel.updateCustomMotorStatus(motorToReject!!, "rejected", reason)
                motorToReject = null
            },
            onDismiss = { motorToReject = null }
        )
    }

    // Full screen image viewer
    fullScreenPhotos?.let { photos ->
        FullScreenImageViewer(
            photos = photos,
            initialIndex = fullScreenInitialIndex,
            onDismiss = { fullScreenPhotos = null }
        )
    }

    // Filter motors belonging to this category
    // In public browsing (role != "admin"), only explicitly approved records are included
    val categoryMotors = remember(customMotors, categoryId, role) {
        if (role == "admin") {
            customMotors.filter { it.categoryId == categoryId }
        } else {
            customMotors.filter { it.categoryId == categoryId && it.isExplicitlyApproved() }
        }
    }

    // Dynamic list of unique companies sorted alphabetically
    val companies = remember(categoryMotors) {
        val unique = categoryMotors
            .map { it.companyName.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
        listOf("All") + unique
    }

    // Filtered by company and search query
    val displayedMotors = categoryMotors.filter { motor ->
        val matchesCompany = selectedCompany == "All" || motor.companyName.equals(selectedCompany, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                motor.companyName.contains(searchQuery, ignoreCase = true) ||
                motor.motorName.contains(searchQuery, ignoreCase = true) ||
                motor.hp.contains(searchQuery, ignoreCase = true) ||
                motor.customFields.values.any { it.contains(searchQuery, ignoreCase = true) }

        val isAuthorizedToSee = if (role == "admin") {
            true
        } else {
            motor.isExplicitlyApproved()
        }

        matchesCompany && matchesSearch && isAuthorizedToSee
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(category.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Company Wise Winding Data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        motorViewModel.loadData(isAdmin = (role == "admin"), userId = targetUserId)
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Data")
                    }
                    if (role == "admin") {
                        IconButton(onClick = { navController.navigate("manage_custom_categories") }) {
                            Icon(Icons.Rounded.Tune, contentDescription = "Configure Category", tint = MaterialTheme.colorScheme.primary)
                        }
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
        ) {
            // SEARCH BOX
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by company, HP, model...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                // COMPANY FILTER CHIPS (PART 6)
                if (companies.size > 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Filter by Company:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        companies.forEach { company ->
                            val count = if (company == "All") {
                                categoryMotors.size
                            } else {
                                categoryMotors.count { it.companyName.equals(company, ignoreCase = true) }
                            }
                            FilterChip(
                                selected = selectedCompany == company,
                                onClick = { selectedCompany = company },
                                label = { Text("$company ($count)") },
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            // LIST OF MOTORS
            if (displayedMotors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (searchQuery.isNotEmpty() || selectedCompany != "All") "No matching motor data" else "No motor data in this category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            if (searchQuery.isNotEmpty() || selectedCompany != "All") "Try clearing filters or changing search query" else "No motor winding data currently available for ${category.name}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedMotors, key = { it.id }) { motor ->
                        CustomMotorCard(
                            motor = motor,
                            category = category,
                            role = role,
                            currentUserId = currentUser?.id,
                            onView = { navController.navigate("custom_motor_detail/${motor.id}") },
                            onEdit = { navController.navigate("add_edit_custom_motor/${category.id}/${motor.id}") },
                            onDelete = { motorToDelete = motor.id },
                            onApprove = { motorViewModel.updateCustomMotorStatus(motor.id, "approved") },
                            onReject = { motorToReject = motor.id },
                            onPhotoClick = { urls, idx ->
                                fullScreenPhotos = urls
                                fullScreenInitialIndex = idx
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomMotorCard(
    motor: CustomMotor,
    category: CustomCategory,
    role: String,
    currentUserId: String?,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPhotoClick: (List<String>, Int) -> Unit
) {
    val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()
    val canEdit = role == "admin"
    val canDelete = role == "admin"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // HEADER: Company & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            motor.companyName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (motor.motorName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            motor.motorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // STATUS BADGE
                com.example.ui.components.StatusBadge(motor.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PHOTO & CORE SPECS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(displayUrls, 0) }
                    ) {
                        AsyncImage(
                            model = displayUrls.first(),
                            contentDescription = "Motor Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (displayUrls.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    "+${displayUrls.size - 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    if (motor.hp.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HP: ${motor.hp}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    // Display up to 3 custom fields in the card preview
                    val previewFields = motor.customFields.entries.take(3)
                    if (previewFields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            previewFields.forEach { (k, v) ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        "$k: $v",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CREATED BY INFO (PART 7)
            if (motor.createdByName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Added by: ${motor.createdByName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (motor.createdAt > 0L) {
                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(motor.createdAt))
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submitted: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (motor.isRejected()) {
                Spacer(modifier = Modifier.height(8.dp))
                com.example.ui.components.RejectionReasonCard(
                    rejectionReason = motor.rejectionReason,
                    reviewedByName = motor.reviewedByName,
                    reviewedAt = motor.reviewedAt
                )
            }

            // ACTION BUTTONS
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Admin Approve/Reject buttons if pending
                if (role == "admin" && motor.isPending()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onApprove,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else if (role == "admin" && motor.isRejected()) {
                    FilledTonalButton(
                        onClick = onApprove,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Approve", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (canEdit) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "View", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
