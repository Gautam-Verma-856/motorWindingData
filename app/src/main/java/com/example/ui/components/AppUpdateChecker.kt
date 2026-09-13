package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UpdateConfig(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val minimumSupportedVersionCode: Int,
    val updateMessage: String,
    val updateUrl: String,
    val forceUpdate: Boolean
)

@Composable
fun AppUpdateWrapper(content: @Composable () -> Unit) {
    var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentVersionCode = BuildConfig.VERSION_CODE

    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("app_config").document("update").get().await()
            if (snapshot.exists()) {
                val latestVersionCode = snapshot.get("latestVersionCode")?.toString()?.toIntOrNull() ?: 0
                val latestVersionName = snapshot.getString("latestVersionName") ?: ""
                val minimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")?.toString()?.toIntOrNull() ?: 0
                val updateMessage = snapshot.getString("updateMessage") ?: "A new version of the app is available."
                val updateUrl = snapshot.getString("updateUrl") ?: ""
                val forceUpdate = snapshot.get("forceUpdate")?.toString()?.toBoolean() ?: false

                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                if (currentVersionCode < config.latestVersionCode) {
                    updateConfig = config
                    showDialog = true
                }
            }
        } catch (e: Exception) {
            // Silently ignore errors (e.g., no network) to avoid breaking the app
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        if (showDialog && updateConfig != null) {
            val config = updateConfig!!
            val isMandatory = currentVersionCode < config.minimumSupportedVersionCode || config.forceUpdate
            
            AppUpdateDialog(
                config = config,
                isMandatory = isMandatory,
                onDismiss = { if (!isMandatory) showDialog = false },
                onUpdate = {
                    if (config.updateUrl.isNotBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(config.updateUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open update link", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Update URL is not available", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun AppUpdateDialog(
    config: UpdateConfig,
    isMandatory: Boolean,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory,
            dismissOnClickOutside = !isMandatory
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = "Update Available",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isMandatory) "Mandatory Update" else "Update Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = config.updateMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Version ${config.latestVersionName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onUpdate,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Update Now", fontWeight = FontWeight.Bold)
                }
                
                if (!isMandatory) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Later", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
