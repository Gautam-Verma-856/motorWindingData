package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.utils.ApkDownloader
import com.example.utils.DownloadState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Locale

data class UpdateConfig(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val minimumSupportedVersionCode: Int,
    val updateMessage: String,
    val updateUrl: String,
    val forceUpdate: Boolean
)

private fun isDirectApkUrl(url: String): Boolean {
    val cleanUrl = url.substringBefore("?").substringBefore("#").lowercase(Locale.US)
    return cleanUrl.endsWith(".apk")
}

@Composable
fun AppUpdateWrapper(content: @Composable () -> Unit) {
    var updateConfig by remember { mutableStateOf<UpdateConfig?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentVersionCode = BuildConfig.VERSION_CODE

    val coroutineScope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    val apkDownloader = remember { ApkDownloader(context) }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val downloadedFile = (downloadState as? DownloadState.Success)?.file
        if (downloadedFile != null && apkDownloader.canInstallPackages()) {
            val launched = apkDownloader.installApk(downloadedFile)
            if (!launched) {
                Toast.makeText(context, "Unable to open APK installer.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun tryInstallOrRequestPermission(file: File) {
        if (apkDownloader.canInstallPackages()) {
            val launched = apkDownloader.installApk(file)
            if (!launched) {
                Toast.makeText(context, "Unable to open APK installer.", Toast.LENGTH_SHORT).show()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(
                context,
                "Please allow 'Install unknown apps' permission to complete the update.",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
            installPermissionLauncher.launch(intent)
        }
    }

    fun openUpdateInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open update link", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        Log.d("AppUpdateChecker", "Starting update check for versionCode=$currentVersionCode")
        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("app_config").document("update")
            val snapshot = try {
                docRef.get(Source.SERVER).await()
            } catch (_: Exception) {
                docRef.get().await()
            }

            if (snapshot.exists()) {
                val latestVersionCode = snapshot.get("latestVersionCode")
                    ?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val latestVersionName = snapshot.get("latestVersionName")?.toString()?.trim() ?: ""
                val minimumSupportedVersionCode = snapshot.get("minimumSupportedVersionCode")
                    ?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                val updateMessage = snapshot.get("updateMessage")?.toString()?.takeIf { it.isNotBlank() }
                    ?: "A new version of Motor Winding Data is available. Please update the app to continue."
                val updateUrl = snapshot.get("updateUrl")?.toString()?.trim() ?: ""
                val rawForceUpdate = snapshot.get("forceUpdate")
                val forceUpdate = rawForceUpdate?.toString()?.toBooleanStrictOrNull()
                    ?: rawForceUpdate?.toString()?.toBoolean()
                    ?: false

                val config = UpdateConfig(
                    latestVersionCode = latestVersionCode,
                    latestVersionName = latestVersionName,
                    minimumSupportedVersionCode = minimumSupportedVersionCode,
                    updateMessage = updateMessage,
                    updateUrl = updateUrl,
                    forceUpdate = forceUpdate
                )

                val needsUpdate = currentVersionCode < config.latestVersionCode ||
                    currentVersionCode < config.minimumSupportedVersionCode

                Log.d(
                    "AppUpdateChecker",
                    "Update check result: current=$currentVersionCode, latest=${config.latestVersionCode}, min=${config.minimumSupportedVersionCode}, force=${config.forceUpdate}, needsUpdate=$needsUpdate"
                )

                if (needsUpdate) {
                    updateConfig = config
                    showDialog = true
                }
            } else {
                Log.d("AppUpdateChecker", "Update config document does not exist")
            }
        } catch (e: Exception) {
            Log.e("AppUpdateChecker", "Firebase update check failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (showDialog && updateConfig != null) {
            val config = updateConfig!!
            val isMandatory = config.forceUpdate || currentVersionCode < config.minimumSupportedVersionCode

            AppUpdateDialog(
                config = config,
                isMandatory = isMandatory,
                downloadState = downloadState,
                onDismiss = {
                    if (!isMandatory &&
                        downloadState !is DownloadState.Downloading &&
                        downloadState !is DownloadState.Progress
                    ) {
                        showDialog = false
                    }
                },
                onUpdate = {
                    val url = config.updateUrl.trim()
                    if (url.isNotBlank()) {
                        if (isDirectApkUrl(url)) {
                            val versionLabel = config.latestVersionName.ifBlank {
                                config.latestVersionCode.toString()
                            }
                            coroutineScope.launch {
                                apkDownloader.downloadApk(url, "motor_winding_update_${versionLabel}.apk")
                                    .collect { state ->
                                        downloadState = state
                                        if (state is DownloadState.Success) {
                                            tryInstallOrRequestPermission(state.file)
                                        }
                                    }
                            }
                        } else {
                            openUpdateInBrowser(url)
                        }
                    } else {
                        Toast.makeText(context, "Update URL is not available", Toast.LENGTH_SHORT).show()
                    }
                },
                onInstall = {
                    val file = (downloadState as? DownloadState.Success)?.file
                    if (file != null) {
                        tryInstallOrRequestPermission(file)
                    }
                },
                onOpenBrowser = {
                    val url = config.updateUrl.trim()
                    if (url.isNotBlank()) {
                        openUpdateInBrowser(url)
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
    downloadState: DownloadState,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit,
    onInstall: () -> Unit,
    onOpenBrowser: () -> Unit
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
                        else -> if (isMandatory) "Update Required" else "Update Available"
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

                    val targetVersionLabel = config.latestVersionName.ifBlank {
                        config.latestVersionCode.toString()
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Installed: v${BuildConfig.VERSION_NAME}  •  Latest: v$targetVersionLabel",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (downloadState is DownloadState.Progress && downloadState.progress >= 0) {
                        LinearProgressIndicator(
                            progress = { downloadState.progress.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.1f MB / %.1f MB (%d%%)",
                                downloadState.downloadedMb,
                                downloadState.totalMb,
                                downloadState.progress
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        if (downloadState is DownloadState.Progress) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f MB downloaded", downloadState.downloadedMb),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (downloadState is DownloadState.Success) {
                    Button(
                        onClick = onInstall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Install Update", fontWeight = FontWeight.Bold)
                    }
                } else if (!isDownloading) {
                    Button(
                        onClick = onUpdate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            if (downloadState is DownloadState.Error) "Retry Download" else "Update Now",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (downloadState is DownloadState.Error && config.updateUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenBrowser,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open Download Link in Browser", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (!isMandatory) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
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
