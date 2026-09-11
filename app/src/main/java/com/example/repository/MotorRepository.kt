package com.example.repository

import android.net.Uri
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MotorRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val ref = storage.reference.child("motor_photos").child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
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
