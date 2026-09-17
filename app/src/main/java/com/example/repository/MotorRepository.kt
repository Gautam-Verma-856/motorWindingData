package com.example.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID

class MotorRepository {
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()
    
    // The API key is now securely read from BuildConfig, generated via the Secrets Gradle Plugin.
    // Users should configure this via the Secrets panel in AI Studio or the .env file.
    private val IMGBB_API_KEY = com.example.BuildConfig.IMGBB_API_KEY

    suspend fun uploadImage(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Read and compress image
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (originalBitmap == null) {
                    return@withContext Result.failure(Exception("Could not decode image"))
                }
                
                // Resize image to max 1200px to ensure fast upload
                val maxWidth = 1200f
                val maxHeight = 1200f
                val ratio = Math.min(maxWidth / originalBitmap.width, maxHeight / originalBitmap.height)
                val finalBitmap = if (ratio < 1) {
                    Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * ratio).toInt(), (originalBitmap.height * ratio).toInt(), true)
                } else {
                    originalBitmap
                }
                
                val outputStream = ByteArrayOutputStream()
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val imageBytes = outputStream.toByteArray()
                
                // Upload to ImgBB
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart(
                        "image", 
                        "upload.jpg", 
                        imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()
                    
                val requestUrl = "https://api.imgbb.com/1/upload"
                android.util.Log.d("ImgBBUpload", "Actual HTTP request URL: $requestUrl")
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()
                    
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val data = jsonObject.getJSONObject("data")
                    val url = data.getString("url") // Get the direct image URL
                    Result.success(url)
                } else {
                    val errorMsg = "Upload failed: " + response.code + " Body: " + responseBody
                    android.util.Log.e("ImgBBUpload", errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                android.util.Log.e("ImgBBUpload", "Exception during upload", e)
                Result.failure(e)
            }
        }
    }

    suspend fun saveSinglePhaseMotor(motor: SinglePhaseMotor): Result<Unit> {
        return try {
            val id = motor.id.ifEmpty { db.collection("single_phase_motors").document().id }
            db.collection("single_phase_motors").document(id).set(motor.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSinglePhaseMotors(): Result<List<SinglePhaseMotor>> {
        return try {
            val query = db.collection("single_phase_motors").get().await()
            Result.success(query.toObjects(SinglePhaseMotor::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSinglePhaseMotor(id: String): Result<Unit> {
        return try {
            db.collection("single_phase_motors").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSinglePhaseStatus(id: String, status: String): Result<Unit> {
        return try {
            db.collection("single_phase_motors").document(id).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveThreePhaseMotor(motor: ThreePhaseMotor): Result<Unit> {
        return try {
            val id = motor.id.ifEmpty { db.collection("three_phase_motors").document().id }
            db.collection("three_phase_motors").document(id).set(motor.copy(id = id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getThreePhaseMotors(): Result<List<ThreePhaseMotor>> {
        return try {
            val query = db.collection("three_phase_motors").get().await()
            Result.success(query.toObjects(ThreePhaseMotor::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteThreePhaseMotor(id: String): Result<Unit> {
        return try {
            db.collection("three_phase_motors").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateThreePhaseStatus(id: String, status: String): Result<Unit> {
        return try {
            db.collection("three_phase_motors").document(id).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
