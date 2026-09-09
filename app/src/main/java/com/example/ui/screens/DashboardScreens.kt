package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, authViewModel: AuthViewModel, motorViewModel: MotorViewModel) {
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val usersList by authViewModel.usersList.collectAsState()

    LaunchedEffect(Unit) {
        motorViewModel.loadData()
        authViewModel.fetchUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard("Total Single Phase", singlePhaseMotors.size.toString(), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                StatCard("Total 3 Phase", threePhaseMotors.size.toString(), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatCard("Total Motors", (singlePhaseMotors.size + threePhaseMotors.size).toString(), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                StatCard("Total Users", usersList.size.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("add_edit_motor/single/new") }, modifier = Modifier.fillMaxWidth()) {
                Text("Add Single Phase Motor")
            }
            Button(onClick = { navController.navigate("add_edit_motor/three/new") }, modifier = Modifier.fillMaxWidth()) {
                Text("Add 3 Phase Motor")
            }
            Button(onClick = { navController.navigate("motor_list/single") }, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Single Phase Motors")
            }
            Button(onClick = { navController.navigate("motor_list/three") }, modifier = Modifier.fillMaxWidth()) {
                Text("Manage 3 Phase Motors")
            }
            Button(onClick = { navController.navigate("user_management") }, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Public Users")
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(100.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavController, authViewModel: AuthViewModel, motorViewModel: MotorViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Public Dashboard") },
                actions = {
                    TextButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("motor_list/single") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("View Single Phase Motors", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("motor_list/three") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("View 3 Phase Motors", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
