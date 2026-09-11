package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import com.example.viewmodel.AuthViewModel
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.ui.components.ErrorDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMotorScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    id: String?,
    authViewModel: AuthViewModel
) {
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var photoUrl by remember { mutableStateOf("") }
    // ... rest of state vars ...
