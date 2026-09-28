package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.*
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
import com.example.model.User
import com.example.ui.components.FullScreenImageViewer
import com.example.viewmodel.MotorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMotorDetailScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    id: String,
    role: String,
    currentUser: User? = null
) {
    val customMotors by motorViewModel.customMotors.collectAsState()
    val customCategories by motorViewModel.customCategories.collectAsState()
    val motor = customMotors.find { it.id == id }
    val category = customCategories.find { it.id.trim().equals(motor?.categoryId?.trim() ?: "", ignoreCase = true) }

    val isAdmin = role.trim().equals("admin", ignoreCase = true)
    val isEmailVerified = currentUser?.emailVerified == true ||
            (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isEmailVerified == true)
    val isAccountActive = currentUser?.accountStatus?.trim()?.equals("active", ignoreCase = true) == true
    val isPublicActiveAndVerified = currentUser != null &&
            currentUser.role.trim().equals("public", ignoreCase = true) &&
            isEmailVerified &&
            isAccountActive
    val canAddData = isAdmin || (isPublicActiveAndVerified && (category?.allowPublicSubmissions != false))

    var fullScreenPhotos by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenInitialIndex by remember { mutableIntStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (isAdmin) {
            motorViewModel.markMotorAsSeen("custom", id)
        }
    }

    fullScreenPhotos?.let { photos ->
        FullScreenImageViewer(
            photos = photos,
            initialIndex = fullScreenInitialIndex,
            onDismiss = { fullScreenPhotos = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(motor?.companyName ?: "Motor Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(
                            "${motor?.categoryName ?: "Custom Category"} • ${if (motor?.hp?.isNotEmpty() == true) "${motor.hp} HP" else "Specs"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (canAddData && motor != null) {
                        IconButton(onClick = {
                            navController.navigate("add_edit_custom_motor/${motor.categoryId}/new")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Motor Data", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (isAdmin && motor != null) {
                        IconButton(onClick = {
                            navController.navigate("add_edit_custom_motor/${motor.categoryId}/${motor.id}")
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
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
        if (motor == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Motor not found", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // PHOTO GALLERY
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
                                    .height(240.dp)
                                    .width(if (displayUrls.size == 1) 340.dp else 260.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        fullScreenPhotos = displayUrls
                                        fullScreenInitialIndex = index
                                    }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Motor Photo ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp),
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ZoomIn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (displayUrls.size > 1) "${index + 1}/${displayUrls.size}" else "Zoom",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // HEADER CARD: Company, Model, Category, HP
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    motor.categoryName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            com.example.ui.components.StatusBadge(motor.status)
                        }

                        Text(
                            motor.companyName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (motor.motorName.isNotEmpty()) {
                            Text(
                                "Model: ${motor.motorName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (motor.hp.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ElectricBolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Power: ${motor.hp}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // DYNAMIC CUSTOM FIELDS SPECIFICATIONS
                if (motor.customFields.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Winding & Motor Specifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            motor.customFields.forEach { (fieldName, fieldValue) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        fieldName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        fieldValue,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                // SUBMISSION INFO (PART 7)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (motor.createdByName.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Added by: ${motor.createdByName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Date: ${sdf.format(Date(motor.createdAt))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                                    onClick = { motorViewModel.updateCustomMotorStatus(motor.id, "approved") },
                                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981)),
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

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showRejectDialog && motor != null) {
        com.example.ui.components.RejectionReasonDialog(
            onConfirm = { reason ->
                motorViewModel.updateCustomMotorStatus(motor.id, "rejected", reason)
                showRejectDialog = false
            },
            onDismiss = { showRejectDialog = false }
        )
    }
}
