package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.viewmodel.MotorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorListScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    role: String
) {
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        motorViewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (type == "single") "Single Phase Motors" else "3 Phase Motors") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search motors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            if (type == "single") {
                val filtered = singlePhaseMotors.filter {
                    it.companyName.contains(searchQuery, ignoreCase = true) ||
                    it.hp.contains(searchQuery, ignoreCase = true) ||
                    it.capacitor.contains(searchQuery, ignoreCase = true) ||
                    it.runningPitch.contains(searchQuery, ignoreCase = true) ||
                    it.runningTurn.contains(searchQuery, ignoreCase = true) ||
                    it.startingPitch.contains(searchQuery, ignoreCase = true) ||
                    it.startingTurn.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No motor data found.")
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(filtered) { motor ->
                            SinglePhaseMotorCard(motor, role,
                                onView = { navController.navigate("motor_detail/single/${motor.id}") },
                                onEdit = { navController.navigate("add_edit_motor/single/${motor.id}") },
                                onDelete = { motorViewModel.deleteSinglePhase(motor.id) }
                            )
                        }
                    }
                }
            } else {
                val filtered = threePhaseMotors.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.hp.contains(searchQuery, ignoreCase = true) ||
                    it.slot.contains(searchQuery, ignoreCase = true) ||
                    it.pitch.contains(searchQuery, ignoreCase = true) ||
                    it.turn.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No motor data found.")
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(filtered) { motor ->
                            ThreePhaseMotorCard(motor, role,
                                onView = { navController.navigate("motor_detail/three/${motor.id}") },
                                onEdit = { navController.navigate("add_edit_motor/three/${motor.id}") },
                                onDelete = { motorViewModel.deleteThreePhase(motor.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SinglePhaseMotorCard(
    motor: SinglePhaseMotor,
    role: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (motor.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = motor.photoUrl,
                    contentDescription = "Motor Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Text("Company: ${motor.companyName}", style = MaterialTheme.typography.titleMedium)
            Text("HP: ${motor.hp}")
            Text("Capacitor: ${motor.capacitor}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onView, modifier = Modifier.weight(1f)) {
                    Text("View Details")
                }
                if (role == "admin") {
                    OutlinedButton(onClick = onEdit) {
                        Text("Edit")
                    }
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ThreePhaseMotorCard(
    motor: ThreePhaseMotor,
    role: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (motor.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = motor.photoUrl,
                    contentDescription = "Motor Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Text("Name: ${motor.name}", style = MaterialTheme.typography.titleMedium)
            Text("Slot: ${motor.slot}")
            Text("HP: ${motor.hp}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onView, modifier = Modifier.weight(1f)) {
                    Text("View Details")
                }
                if (role == "admin") {
                    OutlinedButton(onClick = onEdit) {
                        Text("Edit")
                    }
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
