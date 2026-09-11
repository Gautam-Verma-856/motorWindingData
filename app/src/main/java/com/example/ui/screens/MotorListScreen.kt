package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.viewmodel.MotorViewModel
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.ui.components.ErrorDialog
import com.example.ui.components.ConfirmationDialog
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorListScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    role: String,
    currentUserId: String? = null
) {
    val uiState by motorViewModel.uiState.collectAsState()
    val isLoadingData by motorViewModel.isLoadingData.collectAsState()
    var motorToDelete by remember { mutableStateOf<String?>(null) }
    var deleteType by remember { mutableStateOf<String?>(null) }
    var motorToReject by remember { mutableStateOf<String?>(null) }
    var rejectType by remember { mutableStateOf<String?>(null) }

    if (uiState is UiState.Loading) {
        LoadingDialog("Processing...")
    } else if (uiState is UiState.Success) {
        val action = (uiState as UiState.Success).message
        val title = when (action) {
            "delete" -> "Deleted Successfully"
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
                if (deleteType == "single") motorViewModel.deleteSinglePhase(motorToDelete!!)
                else motorViewModel.deleteThreePhase(motorToDelete!!)
                motorToDelete = null
                deleteType = null
            },
            onDismiss = {
                motorToDelete = null
                deleteType = null
            }
        )
    }

    if (motorToReject != null) {
        ConfirmationDialog(
            title = "Reject Submission?",
            message = "Are you sure you want to reject this motor data?",
            confirmText = "Reject",
            isDestructive = true,
            onConfirm = {
                if (rejectType == "single") motorViewModel.updateSinglePhaseStatus(motorToReject!!, "rejected")
                else motorViewModel.updateThreePhaseStatus(motorToReject!!, "rejected")
                motorToReject = null
                rejectType = null
            },
            onDismiss = {
                motorToReject = null
                rejectType = null
            }
        )
    }

    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    
    // Default filter for generic "approved" views
    var selectedFilter by remember { mutableStateOf(if (type == "approved" || type == "pending" || type == "my_submissions") "All" else if (type == "single") "Single Phase" else "Three Phase") }

    LaunchedEffect(Unit) {
        motorViewModel.loadData()
    }

    val screenTitle = when (type) {
        "pending" -> "Pending Approval"
        "approved" -> "Approved Motor Data"
        "my_submissions" -> "My Submissions"
        "single" -> "Single Phase Motors"
        "three" -> "3 Phase Motors"
        else -> "Motor Data"
    }
    
    val screenSubtitle = when (type) {
        "pending" -> "Review motor data submitted by public users"
        "approved" -> "Browse verified motor winding data"
        "my_submissions" -> "Track your submitted motor data"
        else -> "Manage motor specifications"
    }

    val filters = listOf("All", "Single Phase", "Three Phase")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(screenTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(screenSubtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // SEARCH & FILTERS
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search motors...") },
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (type == "approved" || type == "pending" || type == "my_submissions") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // LIST
            val filteredSingle = singlePhaseMotors.filter { motor ->
                val matchesType = type == "single" || type == "approved" && motor.status == "approved" || type == "pending" && motor.status == "pending" || type == "my_submissions" && motor.createdBy == currentUserId
                val matchesSearch = motor.companyName.contains(searchQuery, ignoreCase = true) || motor.hp.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Single Phase"
                matchesType && matchesSearch && matchesFilterChip
            }

            val filteredThree = threePhaseMotors.filter { motor ->
                val matchesType = type == "three" || type == "approved" && motor.status == "approved" || type == "pending" && motor.status == "pending" || type == "my_submissions" && motor.createdBy == currentUserId
                val matchesSearch = motor.name.contains(searchQuery, ignoreCase = true) || motor.hp.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Three Phase"
                matchesType && matchesSearch && matchesFilterChip
            }

            if (isLoadingData) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredSingle.isEmpty() && filteredThree.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(type)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredSingle) { motor ->
                        PremiumSinglePhaseMotorCard(
                            motor = motor,
                            role = role,
                            viewType = type,
                            onView = { navController.navigate("motor_detail/single/${motor.id}") },
                            onEdit = { navController.navigate("add_edit_motor/single/${motor.id}") },
                            onDelete = { motorToDelete = motor.id; deleteType = "single" },
                            onApprove = { motorViewModel.updateSinglePhaseStatus(motor.id, "approved") },
                            onReject = { motorToReject = motor.id; rejectType = "single" }
                        )
                    }
                    items(filteredThree) { motor ->
                        PremiumThreePhaseMotorCard(
                            motor = motor,
                            role = role,
                            viewType = type,
                            onView = { navController.navigate("motor_detail/three/${motor.id}") },
                            onEdit = { navController.navigate("add_edit_motor/three/${motor.id}") },
                            onDelete = { motorToDelete = motor.id; deleteType = "three" },
                            onApprove = { motorViewModel.updateThreePhaseStatus(motor.id, "approved") },
                            onReject = { motorToReject = motor.id; rejectType = "three" }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(type: String) {
    val title = when(type) {
        "pending" -> "All caught up!"
        "my_submissions" -> "No submissions yet"
        else -> "No motors found"
    }
    val subtitle = when(type) {
        "pending" -> "There are no pending submissions to review."
        "my_submissions" -> "Submit motor winding data to see it here."
        else -> "Try adjusting your search or filters."
    }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (type == "pending") Icons.Rounded.CheckCircle else Icons.Rounded.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PremiumSinglePhaseMotorCard(
    motor: SinglePhaseMotor,
    role: String,
    viewType: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (motor.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = motor.photoUrl,
                    contentDescription = "Motor Photo",
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(motor.companyName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Single Phase • ${motor.hp} HP", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (viewType == "my_submissions" || viewType == "pending" || role == "admin") {
                        StatusBadge(motor.status)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip("Cap", motor.capacitor)
                    InfoChip("Run SWG", motor.runningSwg)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onView, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        Text("Details")
                    }
                    if (role == "admin" && (viewType == "single" || viewType == "approved")) {
                        OutlinedButton(onClick = onEdit) { Text("Edit") }
                        IconButton(onClick = onDelete, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Close, contentDescription = "Delete")
                        }
                    }
                }
                
                if (role == "admin" && viewType == "pending") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Approve")
                        }
                        Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumThreePhaseMotorCard(
    motor: ThreePhaseMotor,
    role: String,
    viewType: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (motor.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = motor.photoUrl,
                    contentDescription = "Motor Photo",
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(motor.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("3 Phase • ${motor.hp} HP", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (viewType == "my_submissions" || viewType == "pending" || role == "admin") {
                        StatusBadge(motor.status)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip("Slots", motor.slot)
                    InfoChip("SWG", motor.swg)
                    InfoChip("Turns", motor.turn)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onView, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        Text("Details")
                    }
                    if (role == "admin" && (viewType == "three" || viewType == "approved")) {
                        OutlinedButton(onClick = onEdit) { Text("Edit") }
                        IconButton(onClick = onDelete, colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Close, contentDescription = "Delete")
                        }
                    }
                }
                
                if (role == "admin" && viewType == "pending") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onApprove, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Approve")
                        }
                        Button(onClick = onReject, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status.lowercase()) {
        "approved" -> Color(0xFF10B981)
        "pending" -> Color(0xFFF59E0B)
        "rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val icon = when(status.lowercase()) {
        "approved" -> Icons.Rounded.CheckCircle
        "pending" -> Icons.Rounded.HourglassEmpty
        "rejected" -> Icons.Rounded.Block
        else -> Icons.Rounded.Info
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(status.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
