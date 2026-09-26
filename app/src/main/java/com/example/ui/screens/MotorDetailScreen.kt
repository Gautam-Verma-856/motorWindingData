package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.components.FullScreenImageViewer
import com.example.viewmodel.MotorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorDetailScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    id: String,
    role: String
) {
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()

    var fullScreenPhotos by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenInitialIndex by remember { mutableIntStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id, type) {
        if (role == "admin") {
            motorViewModel.markMotorAsSeen(type, id)
        }
    }

    val motorName: String
    val motorSubtitle: String
    
    if (type == "single") {
        val motor = singlePhaseMotors.find { it.id == id }
        motorName = motor?.companyName ?: "Details"
        val catInfo = if (motor?.category?.isNotEmpty() == true) "${motor.category} • " else ""
        motorSubtitle = "Single Phase • ${catInfo}${motor?.hp ?: "?"} HP"
    } else {
        val motor = threePhaseMotors.find { it.id == id }
        motorName = motor?.name ?: "Details"
        val catInfo = if (motor?.category?.isNotEmpty() == true) "${motor.category} • " else ""
        motorSubtitle = "3 Phase • ${catInfo}${motor?.hp ?: "?"} HP"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(motorName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(motorSubtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (type == "single") {
                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
                    val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()
                    if (displayUrls.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            displayUrls.forEachIndexed { index, url ->
                                Box(
                                    modifier = Modifier
                                        .height(250.dp)
                                        .width(if (displayUrls.size == 1) 360.dp else 280.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            fullScreenPhotos = displayUrls
                                            fullScreenInitialIndex = index
                                        }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Motor Photo ${index + 1}, tap for full screen",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp),
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ZoomIn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (displayUrls.size > 1) "${index + 1}/${displayUrls.size}" else "Full View",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    DetailSection(title = "Basic Information", icon = Icons.Rounded.Info) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            com.example.ui.components.StatusBadge(motor.status)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Company Name", motor.companyName)
                        if (motor.category.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                            DetailRow("Category", motor.category)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("HP", motor.hp)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Capacitor", motor.capacitor)
                    }
                    
                    DetailSection(title = "Running Winding", icon = Icons.Rounded.Speed) {
                        DetailRow("Pitch", motor.runningPitch)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Turn", motor.runningTurn)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("SWG", motor.runningSwg)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Weight", motor.runningWeight)
                    }
                    
                    DetailSection(title = "Starting Winding", icon = Icons.Rounded.Bolt) {
                        DetailRow("Pitch", motor.startingPitch)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Turn", motor.startingTurn)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("SWG", motor.startingSwg)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Weight", motor.startingWeight)
                    }
                    
                    if (motor.createdByName.isNotBlank()) {
                        AddedByCard(name = motor.createdByName)
                    }

                    if (motor.isRejected()) {
                        com.example.ui.components.RejectionReasonCard(
                            rejectionReason = motor.rejectionReason,
                            reviewedByName = motor.reviewedByName,
                            reviewedAt = motor.reviewedAt
                        )
                    }

                    if (role == "admin" && (motor.isPending() || motor.isRejected())) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Admin Review Actions",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { motorViewModel.updateSinglePhaseStatus(motor.id, "approved") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (motor.isRejected()) "Re-Approve" else "Approve Motor", fontWeight = FontWeight.Bold)
                                    }
                                    if (motor.isPending()) {
                                        OutlinedButton(
                                            onClick = { showRejectDialog = true },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Motor not found.")
                }
            } else {
                val motor = threePhaseMotors.find { it.id == id }
                if (motor != null) {
                    val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()
                    if (displayUrls.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            displayUrls.forEachIndexed { index, url ->
                                Box(
                                    modifier = Modifier
                                        .height(250.dp)
                                        .width(if (displayUrls.size == 1) 360.dp else 280.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            fullScreenPhotos = displayUrls
                                            fullScreenInitialIndex = index
                                        }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Motor Photo ${index + 1}, tap for full screen",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp),
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ZoomIn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (displayUrls.size > 1) "${index + 1}/${displayUrls.size}" else "Full View",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    DetailSection(title = "Basic Information", icon = Icons.Rounded.Info) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            com.example.ui.components.StatusBadge(motor.status)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Company Name", motor.name)
                        if (motor.category.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                            DetailRow("Category", motor.category)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Slot", motor.slot)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("HP", motor.hp)
                    }
                    
                    DetailSection(title = "Winding Data", icon = Icons.Rounded.Label) {
                        DetailRow("Pitch", motor.pitch)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Turn", motor.turn)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("SWG", motor.swg)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        DetailRow("Weight", motor.weight)
                    }
                    
                    if (motor.createdByName.isNotBlank()) {
                        AddedByCard(name = motor.createdByName)
                    }

                    if (motor.isRejected()) {
                        com.example.ui.components.RejectionReasonCard(
                            rejectionReason = motor.rejectionReason,
                            reviewedByName = motor.reviewedByName,
                            reviewedAt = motor.reviewedAt
                        )
                    }

                    if (role == "admin" && (motor.isPending() || motor.isRejected())) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Admin Review Actions",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { motorViewModel.updateThreePhaseStatus(motor.id, "approved") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (motor.isRejected()) "Re-Approve" else "Approve Motor", fontWeight = FontWeight.Bold)
                                    }
                                    if (motor.isPending()) {
                                        OutlinedButton(
                                            onClick = { showRejectDialog = true },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reject", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Motor not found.")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (fullScreenPhotos != null) {
        FullScreenImageViewer(
            photos = fullScreenPhotos!!,
            initialIndex = fullScreenInitialIndex,
            onDismiss = { fullScreenPhotos = null }
        )
    }

    if (showRejectDialog) {
        com.example.ui.components.RejectionReasonDialog(
            onConfirm = { reason ->
                if (type == "single") {
                    motorViewModel.updateSinglePhaseStatus(id, "rejected", reason)
                } else {
                    motorViewModel.updateThreePhaseStatus(id, "rejected", reason)
                }
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false }
        )
    }
}

@Composable
fun AddedByCard(name: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Added by: $name",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Public Contributor",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DetailSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
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
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
