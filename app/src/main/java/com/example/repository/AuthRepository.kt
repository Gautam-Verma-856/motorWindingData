package com.example.repository

import android.util.Log
import com.example.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    suspend fun login(email: String, password: String): Result<User> {
        Log.d("AuthDebug", "Firebase Project ID: ${auth.app.options.projectId}")
        Log.d("AuthDebug", "Firebase App ID: ${auth.app.options.applicationId}")
        Log.d("AuthDebug", "Attempting login with email length: ${email.trim().length}, password length: ${password.length}")
        
        return try {
            val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
            Log.d("AuthDebug", "Firebase auth succeeded for email: ${res.user?.email}, uid: ${res.user?.uid}")
            val uid = res.user?.uid ?: throw Exception("User ID not found")
            
            val userDoc = db.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)!!
                when (user.accountStatus) {
                    "pending" -> {
                        auth.signOut()
                        Result.failure(Exception("Account Pending Approval: Your account has been created but is waiting for Admin approval."))
                    }
                    "rejected" -> {
                        auth.signOut()
                        Result.failure(Exception("Account Not Approved: Your account registration was not approved by Admin."))
                    }
                    "suspended", "blocked" -> {
                        auth.signOut()
                        Result.failure(Exception("Account Suspended: Please contact the administrator."))
                    }
                    else -> {
                        Result.success(user)
                    }
                }
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            Log.e("AuthDebug", "Firebase auth failed with Error Code: $errorCode, Exception: ${e.message}")
            if (errorCode == "ERROR_INVALID_CREDENTIAL" || errorCode == "INVALID_LOGIN_CREDENTIALS") {
                Result.failure(Exception("Invalid email or password. Please try again."))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun registerPublicUser(name: String, email: String, mobile: String, pass: String): Result<User> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = res.user?.uid ?: throw Exception("User ID not found")
            val user = User(
                id = uid,
                name = name,
                email = email,
                mobile = mobile,
                role = "public",
                accountStatus = "pending"
            )
            db.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            if (errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                Result.failure(Exception("This email is already in use. Please log in instead."))
            } else if (errorCode == "ERROR_WEAK_PASSWORD") {
                Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
            } else {
                Result.failure(e)
            }
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
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            if (errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                Result.failure(Exception("This email is already in use. Please log in instead."))
            } else if (errorCode == "ERROR_WEAK_PASSWORD") {
                Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun updateUserStatus(userId: String, status: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).update("accountStatus", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            if (errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                Result.failure(Exception("This email is already in use. Please log in instead."))
            } else if (errorCode == "ERROR_WEAK_PASSWORD") {
                Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            if (errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                Result.failure(Exception("This email is already in use. Please log in instead."))
            } else if (errorCode == "ERROR_WEAK_PASSWORD") {
                Result.failure(Exception("Password is too weak. Please use at least 6 characters."))
            } else {
                Result.failure(e)
            }
        }
    }
}
