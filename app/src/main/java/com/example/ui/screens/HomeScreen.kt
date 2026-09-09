package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, currentUser: User?, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Motor Winding Data", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (currentUser != null) {
                        TextButton(onClick = onLogout) {
                            Text("Logout")
                        }
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
            Text("Motor Categories", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("motor_list/single") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Single Phase Motor", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("motor_list/three") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("3 Phase Motor", style = MaterialTheme.typography.titleMedium)
            }

            if (currentUser == null) {
                Spacer(modifier = Modifier.height(48.dp))
                Text("Login Options", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { navController.navigate("admin_login") },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Admin Login", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.navigate("public_login") },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Public Login", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { 
                        if (currentUser.role == "admin") {
                            navController.navigate("admin_dashboard")
                        } else {
                            navController.navigate("user_dashboard")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Go to Dashboard", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
