with open('app/src/main/java/com/example/ui/components/AppUpdateChecker.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.utils.ApkDownloader
import com.example.utils.DownloadState
import java.util.Locale"""

if "import com.example.utils.ApkDownloader" not in content:
    content = content.replace("import android.widget.Toast", "import android.widget.Toast\n" + imports)

# We need to replace the AppUpdateChecker content block that has showDialog
# We'll just regex replace from `Box(modifier = Modifier.fillMaxSize()) {` to the end of the file.

new_ui = """    val coroutineScope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    val apkDownloader = remember { ApkDownloader(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        if (showDialog && updateConfig != null) {
            val config = updateConfig!!
            val isMandatory = currentVersionCode < config.minimumSupportedVersionCode || config.forceUpdate
            
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
"""

import re
# We find Box(modifier = Modifier.fillMaxSize()) { ... to the end
pattern = re.compile(r'    Box\(modifier = Modifier\.fillMaxSize\(\)\) \{.*', re.DOTALL)
content = pattern.sub(new_ui, content)

with open('app/src/main/java/com/example/ui/components/AppUpdateChecker.kt', 'w') as f:
    f.write(content)
print("Patched AppUpdateChecker")
