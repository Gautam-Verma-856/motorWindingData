package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

    val motorName: String
    val motorSubtitle: String
    
    if (type == "single") {
        val motor = singlePhaseMotors.find { it.id == id }
        motorName = motor?.companyName ?: "Details"
        motorSubtitle = "Single Phase • ${motor?.hp ?: "?"} HP"
    } else {
        val motor = threePhaseMotors.find { it.id == id }
        motorName = motor?.name ?: "Details"
        motorSubtitle = "3 Phase • ${motor?.hp ?: "?"} HP"
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
                    if (motor.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = motor.photoUrl,
                            contentDescription = "Motor Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    DetailSection(title = "Basic Information", icon = Icons.Rounded.Info) {
                        DetailRow("Company Name", motor.companyName)
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
                    
                } else {
                    Text("Motor not found.")
                }
            } else {
                val motor = threePhaseMotors.find { it.id == id }
                if (motor != null) {
                    if (motor.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = motor.photoUrl,
                            contentDescription = "Motor Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    DetailSection(title = "Basic Information", icon = Icons.Rounded.Info) {
                        DetailRow("Company Name", motor.name)
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
                    
                } else {
                    Text("Motor not found.")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
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
