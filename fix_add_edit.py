import re

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "r") as f:
    content = f.read()

# I will recreate the imports manually.
parts = content.split("@OptIn(ExperimentalMaterial3Api::class)")
body = "@OptIn(ExperimentalMaterial3Api::class)" + parts[1]

imports = """package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import com.example.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.ui.components.ErrorDialog

"""

with open("app/src/main/java/com/example/ui/screens/AddEditMotorScreen.kt", "w") as f:
    f.write(imports + body)
