package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motor Details") },
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
            if (type == "single") {
                val motor = singlePhaseMotors.find { it.id == id }
                if (motor != null) {
                    if (motor.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = motor.photoUrl,
                            contentDescription = "Motor Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text("Basic Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailRow("Company Name", motor.companyName)
                    DetailRow("HP", motor.hp)
                    DetailRow("Capacitor", motor.capacitor)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Running Winding", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailRow("Pitch", motor.runningPitch)
                    DetailRow("Turn", motor.runningTurn)
                    DetailRow("SWG", motor.runningSwg)
                    DetailRow("Weight", motor.runningWeight)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Starting Winding", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailRow("Pitch", motor.startingPitch)
                    DetailRow("Turn", motor.startingTurn)
                    DetailRow("SWG", motor.startingSwg)
                    DetailRow("Weight", motor.startingWeight)
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
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text("Basic Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailRow("Motor/Company Name", motor.name)
                    DetailRow("Slot", motor.slot)
                    DetailRow("HP", motor.hp)

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Winding Data", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    DetailRow("Pitch", motor.pitch)
                    DetailRow("Turn", motor.turn)
                    DetailRow("SWG", motor.swg)
                    DetailRow("Weight", motor.weight)
                } else {
                    Text("Motor not found.")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value, modifier = Modifier.weight(1f))
    }
}
