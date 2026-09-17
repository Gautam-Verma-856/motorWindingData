package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.utils.ApkDownloader
import com.example.utils.DownloadState
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.CheckCircle
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
import com.google.firebase.FirebaseApp
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
        android.util.Log.d("AppUpdateChecker", "Starting update check")
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("app_config").document("update").get().await()
            
            val projectId = com.google.firebase.FirebaseApp.getInstance().options.projectId
            android.util.Log.d("AppUpdateChecker", "snapshot.exists(): ${snapshot.exists()}")
            android.util.Log.d("AppUpdateChecker", "Firebase project ID: $projectId")
            android.util.Log.d("AppUpdateChecker", "currentVersionCode: $currentVersionCode")
            
            if (snapshot.exists()) {
                val rawLatestVersionCode = snapshot.get("latestVersionCode")
                val rawLatestVersionName = snapshot.get("latestVersionName")
                val rawMinimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")
                val rawUpdateMessage = snapshot.get("updateMessage")
                val rawUpdateUrl = snapshot.get("updateUrl")
                val rawForceUpdate = snapshot.get("forceUpdate")
                
                android.util.Log.d("AppUpdateChecker", "latestVersionCode value: $rawLatestVersionCode, type: ${rawLatestVersionCode?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "latestVersionName value: $rawLatestVersionName, type: ${rawLatestVersionName?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "minimumSupportedVersionCode value: $rawMinimumSupportedVersionCode, type: ${rawMinimumSupportedVersionCode?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "updateMessage value: $rawUpdateMessage, type: ${rawUpdateMessage?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "updateUrl value: $rawUpdateUrl, type: ${rawUpdateUrl?.javaClass?.name}")
                android.util.Log.d("AppUpdateChecker", "forceUpdate value: $rawForceUpdate, type: ${rawForceUpdate?.javaClass?.name}")

                val latestVersionCode = rawLatestVersionCode?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val latestVersionName = rawLatestVersionName?.toString() ?: ""
                val minimumSupportedVersionCode = rawMinimumSupportedVersionCode?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val updateMessage = rawUpdateMessage?.toString() ?: "A new version of the app is available."
                val updateUrl = rawUpdateUrl?.toString() ?: ""
                val forceUpdate = rawForceUpdate?.toString()?.toBooleanStrictOrNull() ?: rawForceUpdate?.toString()?.toBoolean() ?: false

                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                android.widget.Toast.makeText(context, "Update check: current=$currentVersionCode, latest=$latestVersionCode", android.widget.Toast.LENGTH_LONG).show()

                if (currentVersionCode < config.latestVersionCode) {
                    updateConfig = config
                    showDialog = true
                }
            } else {
                android.util.Log.d("AppUpdateChecker", "Update config document does not exist")
                android.widget.Toast.makeText(context, "Update check: document does not exist", android.widget.Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("AppUpdateChecker", "Firebase update check failed", e)
            android.widget.Toast.makeText(context, "Update check failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    val apkDownloader = remember { ApkDownloader(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        if (showDialog && updateConfig != null) {
            val config = updateConfig!!
            // Update is always optional, ignoring minimumSupportedVersionCode and forceUpdate
            val isMandatory = false
            
            AppUpdateDialog(
                config = config,
                isMandatory = isMandatory,
                downloadState = downloadState,
                onDismiss = { if (!isMandatory && downloadState !is DownloadState.Downloading) showDialog = false },
                onUpdate = {
                    if (config.updateUrl.isNotBlank()) {
                        coroutineScope.launch {
                            apkDownloader.downloadApk(config.updateUrl, "motor_winding_update_${config.latestVersionName}.apk")
                                .collect { state ->
                                    downloadState = state
                                    if (state is DownloadState.Success) {
                                        apkDownloader.installApk(state.file)
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(context, "Update URL is not available", Toast.LENGTH_SHORT).show()
                    }
                },
                onInstall = {
                    if (downloadState is DownloadState.Success) {
                        apkDownloader.installApk((downloadState as DownloadState.Success).file)
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
    downloadState: DownloadState,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    onInstall: () -> Unit
) {
    val isDownloading = downloadState is DownloadState.Downloading || downloadState is DownloadState.Progress
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory && !isDownloading,
            dismissOnClickOutside = !isMandatory && !isDownloading
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
                    if (downloadState is DownloadState.Success) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Update Ready",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.SystemUpdate,
                            contentDescription = "Update Available",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = when (downloadState) {
                        is DownloadState.Downloading, is DownloadState.Progress -> "Downloading Update..."
                        is DownloadState.Success -> "Ready to Install"
                        is DownloadState.Error -> "Download Failed"
                        else -> if (isMandatory) "Mandatory Update" else "Update Available"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (downloadState is DownloadState.Idle || downloadState is DownloadState.Error) {
                    Text(
                        text = if (downloadState is DownloadState.Error) downloadState.message else config.updateMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (downloadState is DownloadState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
                }
                
                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val progress = if (downloadState is DownloadState.Progress) downloadState.progress.toFloat() / 100f else 0f
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (downloadState is DownloadState.Progress) {
                        Text(
                            text = String.format(Locale.US, "%.1f MB / %.1f MB (%d%%)", downloadState.downloadedMb, downloadState.totalMb, downloadState.progress),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (downloadState is DownloadState.Success) {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Install Update", fontWeight = FontWeight.Bold)
                    }
                } else if (!isDownloading) {
                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (downloadState is DownloadState.Error) "Retry Download" else "Update Now", fontWeight = FontWeight.Bold)
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
}
