package com.example.repository

import com.example.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val res = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = res.user?.uid ?: throw Exception("User ID not found")
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)!!
                if (user.accountStatus == "blocked") {
                    auth.signOut()
                    Result.failure(Exception("Account is blocked by Admin"))
                } else {
                    Result.success(user)
                }
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerPublicUser(name: String, email: String, pass: String): Result<User> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = res.user?.uid ?: throw Exception("User ID not found")
            val user = User(
                id = uid,
                name = name,
                email = email,
                role = "public",
                accountStatus = "active"
            )
            db.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
    
    suspend fun fetchCurrentUserProfile(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val query = db.collection("users").whereEqualTo("role", "public").get().await()
            val users = query.toObjects(User::class.java)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(userId: String, status: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).update("accountStatus", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
