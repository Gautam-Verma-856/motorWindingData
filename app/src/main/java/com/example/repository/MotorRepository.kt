package com.example.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.model.CustomCategory
import com.example.model.CustomMotor
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
import java.util.concurrent.TimeUnit

class MotorRepository {
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    // The API key is securely injected into BuildConfig from .env or environment variables.
    private val IMGBB_API_KEY = com.example.BuildConfig.IMGBB_API_KEY

    enum class SafeKeyStatus {
        KEY_PRESENT,
        KEY_EMPTY,
        KEY_LITERAL_PLACEHOLDER
    }

    data class SafeDiagnostic(
        val status: SafeKeyStatus,
        val length: Int,
        val isUsable: Boolean,
        val summary: String
    )

    fun getSafeDiagnostic(): SafeDiagnostic {
        val rawKey = IMGBB_API_KEY?.trim() ?: ""
        val length = rawKey.length
        val isPlaceholder = rawKey.startsWith("YOUR_") ||
            rawKey.startsWith("PASTE_") ||
            rawKey.startsWith("$") ||
            rawKey.startsWith("<") ||
            rawKey.equals("null", ignoreCase = true) ||
            rawKey.equals("undefined", ignoreCase = true) ||
            rawKey.equals("MY_API_KEY", ignoreCase = true)

        val status = when {
            length == 0 -> SafeKeyStatus.KEY_EMPTY
            isPlaceholder -> SafeKeyStatus.KEY_LITERAL_PLACEHOLDER
            else -> SafeKeyStatus.KEY_PRESENT
        }

        return SafeDiagnostic(
            status = status,
            length = length,
            isUsable = (status == SafeKeyStatus.KEY_PRESENT),
            summary = when (status) {
                SafeKeyStatus.KEY_PRESENT -> "KEY_PRESENT (length: $length)"
                SafeKeyStatus.KEY_EMPTY -> "KEY_EMPTY (length: 0)"
                SafeKeyStatus.KEY_LITERAL_PLACEHOLDER -> "KEY_LITERAL_PLACEHOLDER (length: $length)"
            }
        )
    }

    data class ImgBBDiagnostic(
        val isConfigured: Boolean,
        val statusSummary: String,
        val keyStatus: SafeKeyStatus = SafeKeyStatus.KEY_EMPTY,
        val keyLength: Int = 0
    )

    fun getImgBBConfigDiagnostic(): ImgBBDiagnostic {
        val diag = getSafeDiagnostic()
        return ImgBBDiagnostic(
            isConfigured = diag.isUsable,
            statusSummary = diag.summary,
            keyStatus = diag.status,
            keyLength = diag.length
        )
    }

    suspend fun uploadImage(context: Context, uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = IMGBB_API_KEY?.trim() ?: ""
                val diag = getSafeDiagnostic()
                android.util.Log.d("ImgBBUpload", "Diagnostic: status=${diag.status}, length=${diag.length}")

                if (!diag.isUsable) {
                    val reason = when (diag.status) {
                        SafeKeyStatus.KEY_EMPTY -> "API key is missing in build configuration"
                        SafeKeyStatus.KEY_LITERAL_PLACEHOLDER -> "API key is set to a placeholder rather than a real key"
                        else -> "API key is not configured"
                    }
                    android.util.Log.e("ImgBBUpload", "Upload aborted: $reason")
                    return@withContext Result.failure(Exception("ImgBB key status: ${diag.status}. Please check AI Studio Secrets."))
                }

                android.util.Log.d("ImgBBUpload", "Upload initiated. Status: ${diag.status}, length: ${diag.length}")

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
                    .addFormDataPart("key", apiKey)
                    .addFormDataPart(
                        "image", 
                        "upload.jpg", 
                        imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()
                    
                val requestUrl = "https://api.imgbb.com/1/upload"
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()
                    
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val jsonObject = JSONObject(responseBody)
                    if (jsonObject.optBoolean("success", false) || jsonObject.optInt("status", 0) == 200) {
                        val data = jsonObject.optJSONObject("data")
                        val url = data?.optString("url")?.takeIf { it.isNotEmpty() }
                            ?: data?.optString("display_url")?.takeIf { it.isNotEmpty() }
                        if (!url.isNullOrBlank()) {
                            Result.success(url)
                        } else {
                            Result.failure(Exception("ImgBB returned success but image URL was empty"))
                        }
                    } else {
                        val errorObj = jsonObject.optJSONObject("error")
                        val msg = errorObj?.optString("message") ?: "Upload failed"
                        Result.failure(Exception(msg))
                    }
                } else {
                    val errorMsg = try {
                        if (!responseBody.isNullOrBlank()) {
                            val json = JSONObject(responseBody)
                            val errorObj = json.optJSONObject("error")
                            val message = errorObj?.optString("message")?.trim() ?: ""
                            val statusTxt = json.optString("status_txt").trim()
                            
                            when {
                                message.contains("Invalid API v1 key", ignoreCase = true) ||
                                message.contains("Empty API v1 key", ignoreCase = true) -> {
                                    "Invalid ImgBB API key. Please check your key in AI Studio Secrets."
                                }
                                message.contains("forbidden", ignoreCase = true) -> {
                                    "ImgBB API key is forbidden or revoked. Please provide a new key in AI Studio Secrets."
                                }
                                message.isNotEmpty() -> {
                                    message
                                }
                                statusTxt.isNotEmpty() -> {
                                    "Upload failed: $statusTxt (HTTP ${response.code})"
                                }
                                else -> {
                                    "Upload failed (HTTP ${response.code})"
                                }
                            }
                        } else {
                            "Upload failed (HTTP ${response.code})"
                        }
                    } catch (_: Exception) {
                        "Upload failed (HTTP ${response.code})"
                    }
                    android.util.Log.e("ImgBBUpload", "Upload failed: HTTP ${response.code} -> $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                android.util.Log.e("ImgBBUpload", "Exception during upload", e)
                Result.failure(e)
            }
        }
    }

    suspend fun saveSinglePhaseMotor(motor: SinglePhaseMotor): Result<SinglePhaseMotor> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val id = motor.id.ifEmpty { db.collection("single_phase_motors").document().id }
            val currentUid = authUser.uid
            val statusToSave = motor.status.ifBlank { "pending" }
            val motorToSave = motor.copy(
                id = id,
                status = statusToSave,
                createdBy = motor.createdBy.ifEmpty { currentUid },
                createdAt = if (motor.createdAt == 0L) System.currentTimeMillis() else motor.createdAt
            )
            db.collection("single_phase_motors").document(id).set(motorToSave).await()
            Result.success(motorToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSinglePhaseMotors(isAdmin: Boolean = false, currentUserId: String? = null): Result<List<SinglePhaseMotor>> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        val effectiveUid = currentUserId?.ifBlank { authUser.uid } ?: authUser.uid
        return try {
            if (isAdmin) {
                val query = db.collection("single_phase_motors").get().await()
                Result.success(query.toObjects(SinglePhaseMotor::class.java))
            } else {
                // Public user: query approved records AND user's own submissions
                val approvedSnap = db.collection("single_phase_motors")
                    .whereEqualTo("status", "approved")
                    .get().await()
                val approvedMotors = approvedSnap.toObjects(SinglePhaseMotor::class.java).filter { it.isExplicitlyApproved() }
                
                val userMotors = if (effectiveUid.isNotEmpty()) {
                    try {
                        val userSnap = db.collection("single_phase_motors")
                            .whereEqualTo("createdBy", effectiveUid)
                            .get().await()
                        userSnap.toObjects(SinglePhaseMotor::class.java)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Result.success((approvedMotors + userMotors).distinctBy { it.id })
            }
        } catch (e: Exception) {
            try {
                val approvedSnap = db.collection("single_phase_motors")
                    .whereEqualTo("status", "approved")
                    .get().await()
                val approvedMotors = approvedSnap.toObjects(SinglePhaseMotor::class.java).filter { it.isExplicitlyApproved() }
                val userMotors = if (effectiveUid.isNotEmpty()) {
                    try {
                        db.collection("single_phase_motors")
                            .whereEqualTo("createdBy", effectiveUid)
                            .get().await().toObjects(SinglePhaseMotor::class.java)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Result.success((approvedMotors + userMotors).distinctBy { it.id })
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getApprovedSinglePhaseMotors(): Result<List<SinglePhaseMotor>> {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val query = db.collection("single_phase_motors")
                .whereEqualTo("status", "approved")
                .get().await()
            val list = query.toObjects(SinglePhaseMotor::class.java).filter { it.isExplicitlyApproved() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserSinglePhaseMotors(userId: String): Result<List<SinglePhaseMotor>> {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val query = db.collection("single_phase_motors")
                .whereEqualTo("createdBy", userId)
                .get().await()
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

    suspend fun updateSinglePhaseStatus(id: String, status: String, rejectionReason: String = ""): Result<Unit> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val adminUid = authUser?.uid ?: ""
        val adminName = authUser?.displayName ?: "Admin"
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "rejectionReason" to (if (status == "rejected") rejectionReason.trim() else ""),
            "reviewedAt" to System.currentTimeMillis(),
            "reviewedBy" to adminUid,
            "reviewedByName" to adminName,
            "adminSeen" to true,
            "seen" to true
        )
        return try {
            db.collection("single_phase_motors").document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveThreePhaseMotor(motor: ThreePhaseMotor): Result<ThreePhaseMotor> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val id = motor.id.ifEmpty { db.collection("three_phase_motors").document().id }
            val currentUid = authUser.uid
            val statusToSave = motor.status.ifBlank { "pending" }
            val motorToSave = motor.copy(
                id = id,
                status = statusToSave,
                createdBy = motor.createdBy.ifEmpty { currentUid },
                createdAt = if (motor.createdAt == 0L) System.currentTimeMillis() else motor.createdAt
            )
            db.collection("three_phase_motors").document(id).set(motorToSave).await()
            Result.success(motorToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getThreePhaseMotors(isAdmin: Boolean = false, currentUserId: String? = null): Result<List<ThreePhaseMotor>> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        val effectiveUid = currentUserId?.ifBlank { authUser.uid } ?: authUser.uid
        return try {
            if (isAdmin) {
                val query = db.collection("three_phase_motors").get().await()
                Result.success(query.toObjects(ThreePhaseMotor::class.java))
            } else {
                val approvedSnap = db.collection("three_phase_motors")
                    .whereEqualTo("status", "approved")
                    .get().await()
                val approvedMotors = approvedSnap.toObjects(ThreePhaseMotor::class.java).filter { it.isExplicitlyApproved() }
                
                val userMotors = if (effectiveUid.isNotEmpty()) {
                    try {
                        val userSnap = db.collection("three_phase_motors")
                            .whereEqualTo("createdBy", effectiveUid)
                            .get().await()
                        userSnap.toObjects(ThreePhaseMotor::class.java)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Result.success((approvedMotors + userMotors).distinctBy { it.id })
            }
        } catch (e: Exception) {
            try {
                val approvedSnap = db.collection("three_phase_motors")
                    .whereEqualTo("status", "approved")
                    .get().await()
                val approvedMotors = approvedSnap.toObjects(ThreePhaseMotor::class.java).filter { it.isExplicitlyApproved() }
                val userMotors = if (effectiveUid.isNotEmpty()) {
                    try {
                        db.collection("three_phase_motors")
                            .whereEqualTo("createdBy", effectiveUid)
                            .get().await().toObjects(ThreePhaseMotor::class.java)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Result.success((approvedMotors + userMotors).distinctBy { it.id })
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getApprovedThreePhaseMotors(): Result<List<ThreePhaseMotor>> {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val query = db.collection("three_phase_motors")
                .whereEqualTo("status", "approved")
                .get().await()
            val list = query.toObjects(ThreePhaseMotor::class.java).filter { it.isExplicitlyApproved() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserThreePhaseMotors(userId: String): Result<List<ThreePhaseMotor>> {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val query = db.collection("three_phase_motors")
                .whereEqualTo("createdBy", userId)
                .get().await()
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

    suspend fun updateThreePhaseStatus(id: String, status: String, rejectionReason: String = ""): Result<Unit> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val adminUid = authUser?.uid ?: ""
        val adminName = authUser?.displayName ?: "Admin"
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "rejectionReason" to (if (status == "rejected") rejectionReason.trim() else ""),
            "reviewedAt" to System.currentTimeMillis(),
            "reviewedBy" to adminUid,
            "reviewedByName" to adminName,
            "adminSeen" to true,
            "seen" to true
        )
        return try {
            db.collection("three_phase_motors").document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Custom Category Methods ---
    suspend fun saveCustomCategory(category: CustomCategory): Result<Unit> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            Log.e("MotorRepository", "saveCustomCategory failed: Authentication required (currentUser is null)")
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val id = category.id.ifEmpty { db.collection("custom_categories").document().id }
            val currentUid = authUser.uid
            val categoryToSave = category.copy(
                id = id,
                createdBy = category.createdBy.ifEmpty { currentUid },
                createdAt = if (category.createdAt == 0L) System.currentTimeMillis() else category.createdAt
            )
            Log.d("MotorRepository", "saveCustomCategory: Saving category id=$id, name='${categoryToSave.name}', uid=$currentUid to collection 'custom_categories'")
            db.collection("custom_categories").document(id).set(categoryToSave).await()
            Log.d("MotorRepository", "saveCustomCategory: Successfully saved category id=$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MotorRepository", "saveCustomCategory failed for category id=${category.id.ifEmpty { "new" }}: ${e.javaClass.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCustomCategories(isAdmin: Boolean = false): Result<List<CustomCategory>> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val query = if (isAdmin) {
                db.collection("custom_categories").get().await()
            } else {
                db.collection("custom_categories").whereEqualTo("isActive", true).get().await()
            }
            Result.success(query.toObjects(CustomCategory::class.java))
        } catch (e: Exception) {
            try {
                val query = db.collection("custom_categories").whereEqualTo("isActive", true).get().await()
                Result.success(query.toObjects(CustomCategory::class.java))
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteCustomCategory(id: String): Result<Unit> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            Log.e("MotorRepository", "deleteCustomCategory failed: Authentication required (currentUser is null)")
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            Log.d("MotorRepository", "deleteCustomCategory: Deleting category id=$id, uid=${authUser.uid} from collection 'custom_categories'")
            db.collection("custom_categories").document(id).delete().await()
            Log.d("MotorRepository", "deleteCustomCategory: Successfully deleted category id=$id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MotorRepository", "deleteCustomCategory failed for category id=$id: ${e.javaClass.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    // --- Custom Motor Methods ---
    suspend fun saveCustomMotor(motor: CustomMotor): Result<CustomMotor> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val id = motor.id.ifEmpty { db.collection("custom_motors").document().id }
            val currentUid = authUser.uid
            val statusToSave = motor.status.ifBlank { "pending" }
            val motorToSave = motor.copy(
                id = id,
                status = statusToSave,
                createdBy = motor.createdBy.ifEmpty { currentUid },
                createdAt = if (motor.createdAt == 0L) System.currentTimeMillis() else motor.createdAt
            )
            db.collection("custom_motors").document(id).set(motorToSave).await()
            Result.success(motorToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomMotors(isAdmin: Boolean = false, currentUserId: String? = null): Result<List<CustomMotor>> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        val effectiveUid = currentUserId?.ifBlank { authUser.uid } ?: authUser.uid
        return try {
            if (isAdmin) {
                val query = db.collection("custom_motors").get().await()
                Result.success(query.toObjects(CustomMotor::class.java))
            } else {
                val approvedSnap = db.collection("custom_motors")
                    .whereEqualTo("status", "approved")
                    .get().await()
                val approvedMotors = approvedSnap.toObjects(CustomMotor::class.java).filter { it.isExplicitlyApproved() }
                
                val userMotors = if (effectiveUid.isNotEmpty()) {
                    try {
                        val userSnap = db.collection("custom_motors")
                            .whereEqualTo("createdBy", effectiveUid)
                            .get().await()
                        userSnap.toObjects(CustomMotor::class.java)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Result.success((approvedMotors + userMotors).distinctBy { it.id })
            }
        } catch (e: Exception) {
            try {
                val approvedSnap = db.collection("custom_motors")
                    .whereEqualTo("status", "approved")
                    .get().await()
                val approvedMotors = approvedSnap.toObjects(CustomMotor::class.java).filter { it.isExplicitlyApproved() }
                val userMotors = if (effectiveUid.isNotEmpty()) {
                    try {
                        db.collection("custom_motors")
                            .whereEqualTo("createdBy", effectiveUid)
                            .get().await().toObjects(CustomMotor::class.java)
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Result.success((approvedMotors + userMotors).distinctBy { it.id })
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserCustomMotors(userId: String): Result<List<CustomMotor>> {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            val query = db.collection("custom_motors")
                .whereEqualTo("createdBy", userId)
                .get().await()
            Result.success(query.toObjects(CustomMotor::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApprovedCustomMotors(categoryId: String? = null): Result<List<CustomMotor>> {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser == null) {
            return Result.failure(Exception("Authentication required"))
        }
        return try {
            var query = db.collection("custom_motors")
                .whereEqualTo("status", "approved")
            if (!categoryId.isNullOrBlank()) {
                query = query.whereEqualTo("categoryId", categoryId)
            }
            val snapshot = query.get().await()
            val list = snapshot.toObjects(CustomMotor::class.java).filter { it.isExplicitlyApproved() }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomMotor(id: String): Result<Unit> {
        return try {
            db.collection("custom_motors").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomMotorStatus(id: String, status: String, rejectionReason: String = ""): Result<Unit> {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val adminUid = authUser?.uid ?: ""
        val adminName = authUser?.displayName ?: "Admin"
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "rejectionReason" to (if (status == "rejected") rejectionReason.trim() else ""),
            "reviewedAt" to System.currentTimeMillis(),
            "reviewedBy" to adminUid,
            "reviewedByName" to adminName,
            "adminSeen" to true,
            "seen" to true
        )
        return try {
            db.collection("custom_motors").document(id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markMotorAsSeen(type: String, id: String): Result<Unit> {
        return try {
            val collectionName = when (type.lowercase()) {
                "single" -> "single_phase_motors"
                "three" -> "three_phase_motors"
                "custom" -> "custom_motors"
                else -> "single_phase_motors"
            }
            db.collection(collectionName).document(id).update(
                mapOf(
                    "adminSeen" to true,
                    "seen" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
