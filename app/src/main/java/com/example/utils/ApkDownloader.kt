package com.example.utils

import android.content.Context
import android.net.Uri
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
import android.content.Intent

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    data class Progress(val progress: Int, val downloadedMb: Float, val totalMb: Float) : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class ApkDownloader(private val context: Context) {

    fun downloadApk(urlStr: String, fileName: String): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading)
        
        try {
            val url = URL(urlStr)
            var connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.instanceFollowRedirects = true
            connection.connect()

            // Handle redirects manually if needed (GitHub releases redirect to AWS S3)
            var redirectCount = 0
            while ((connection.responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                   connection.responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                   connection.responseCode == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
                val newUrl = connection.getHeaderField("Location")
                connection.disconnect()
                connection = URL(newUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()
                redirectCount++
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                emit(DownloadState.Error("Server returned HTTP ${connection.responseCode}"))
                return@flow
            }

            val fileLength = connection.contentLength
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val outputFile = File(downloadDir, fileName)

            if (outputFile.exists()) {
                outputFile.delete()
            }

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(outputFile)

            val data = ByteArray(8192)
            var total: Long = 0
            var count: Int
            
            var lastEmitTime = 0L

            while (inputStream.read(data).also { count = it } != -1) {
                total += count
                outputStream.write(data, 0, count)
                
                val currentTime = System.currentTimeMillis()
                if (fileLength > 0 && currentTime - lastEmitTime > 100) { // Emit at most every 100ms
                    lastEmitTime = currentTime
                    val progress = (total * 100 / fileLength).toInt()
                    val downloadedMb = total.toFloat() / (1024 * 1024)
                    val totalMb = fileLength.toFloat() / (1024 * 1024)
                    emit(DownloadState.Progress(progress, downloadedMb, totalMb))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()

            emit(DownloadState.Success(outputFile))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(DownloadState.Error(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    fun installApk(file: File) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
