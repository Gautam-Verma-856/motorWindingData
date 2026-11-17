package com.example.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    data class Progress(val progress: Int, val downloadedMb: Float, val totalMb: Float) : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class ApkDownloader(private val context: Context) {

    fun canInstallPackages(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun downloadApk(urlStr: String, fileName: String): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading)

        var connection: HttpURLConnection? = null
        try {
            var currentUrl = URL(urlStr)
            connection = (currentUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "MotorWindingData-Android-Updater")
            }
            connection.connect()

            // Handle redirects manually across HTTP/HTTPS hosts (e.g., GitHub releases -> CDN)
            var redirectCount = 0
            while (connection!!.responseCode in listOf(
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_SEE_OTHER,
                    307,
                    308
                ) && redirectCount < 5
            ) {
                val newLocation = connection.getHeaderField("Location")
                if (newLocation.isNullOrBlank()) break
                val nextUrl = URL(currentUrl, newLocation)
                connection.disconnect()
                currentUrl = nextUrl
                connection = (currentUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "MotorWindingData-Android-Updater")
                }
                connection.connect()
                redirectCount++
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                emit(DownloadState.Error("Server returned HTTP ${connection.responseCode}"))
                return@flow
            }

            val contentType = connection.contentType?.lowercase(Locale.US) ?: ""
            if (contentType.contains("text/html")) {
                emit(DownloadState.Error("Direct APK file was not returned. Please open the download link in your browser."))
                return@flow
            }

            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val outputFile = File(downloadDir, safeName)
            if (outputFile.exists()) {
                outputFile.delete()
            }

            val data = ByteArray(8192)
            var total = 0L
            var lastEmitTime = 0L

            connection.inputStream.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    var count: Int
                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count
                        outputStream.write(data, 0, count)

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastEmitTime > 100) {
                            lastEmitTime = currentTime
                            val downloadedMb = total.toFloat() / (1024 * 1024)
                            if (fileLength > 0) {
                                val progress = ((total * 100) / fileLength).toInt().coerceIn(0, 100)
                                val totalMb = fileLength.toFloat() / (1024 * 1024)
                                emit(DownloadState.Progress(progress, downloadedMb, totalMb))
                            } else {
                                emit(DownloadState.Progress(-1, downloadedMb, 0f))
                            }
                        }
                    }
                    outputStream.flush()
                }
            }

            if (total <= 0L || !outputFile.exists()) {
                emit(DownloadState.Error("Downloaded APK file is empty"))
                return@flow
            }

            emit(DownloadState.Success(outputFile))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(DownloadState.Error(e.message ?: "Download failed"))
        } finally {
            connection?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    fun installApk(file: File): Boolean {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
