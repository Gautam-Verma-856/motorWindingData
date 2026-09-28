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
    val currentUser get() = auth.currentUser

    suspend fun login(email: String, password: String, expectedRole: String? = null): Result<User> {
        val trimmedEmail = email.trim()
        Log.d("AuthDebug", "Attempting login with email: $trimmedEmail, expectedRole: $expectedRole")
        
        return try {
            val res = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
            val firebaseUser = res.user ?: throw Exception("User ID not found")
            val uid = firebaseUser.uid
            
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                auth.signOut()
                return Result.failure(Exception("User profile not found"))
            }

            var user = userDoc.toObject(User::class.java) ?: throw Exception("Unable to parse user profile")
            val profileRole = (userDoc.getString("role") ?: user.role).trim().lowercase()
            val profileStatus = (userDoc.getString("accountStatus") ?: user.accountStatus).trim().lowercase()
            val targetRole = expectedRole?.trim()?.lowercase()

            // 1. Expected Public Login flow
            if (targetRole == "public") {
                if (profileRole == "admin") {
                    auth.signOut()
                    return Result.failure(Exception("This is an admin account. Please use Admin Login."))
                }
                if (profileRole != "public") {
                    auth.signOut()
                    return Result.failure(Exception("Invalid account role."))
                }
                if (!firebaseUser.isEmailVerified) {
                    auth.signOut()
                    return Result.failure(Exception("Email Not Verified: Please verify your email before logging in. Check your inbox for the verification link."))
                }
                when (profileStatus) {
                    "pending" -> {
                        auth.signOut()
                        return Result.failure(Exception("Account Pending Approval: Your email is verified, but your account is waiting for Admin approval."))
                    }
                    "rejected" -> {
                        auth.signOut()
                        return Result.failure(Exception("Account Not Approved: Your account registration was not approved by Admin."))
                    }
                    "suspended", "blocked" -> {
                        auth.signOut()
                        return Result.failure(Exception("Account Suspended: Please contact the administrator."))
                    }
                    "active" -> {
                        user = user.copy(id = uid, role = "public", accountStatus = "active")
                        if (!user.emailVerified) {
                            try {
                                db.collection("users").document(uid).update("emailVerified", true).await()
                                user = user.copy(emailVerified = true)
                            } catch (_: Exception) {}
                        }
                        return Result.success(user)
                    }
                    else -> {
                        auth.signOut()
                        return Result.failure(Exception("Account status not allowed: $profileStatus"))
                    }
                }
            }

            // 2. Expected Admin Login flow
            if (targetRole == "admin") {
                if (profileRole == "public") {
                    auth.signOut()
                    return Result.failure(Exception("This is a public account. Please use Public Login."))
                }
                if (profileRole != "admin") {
                    auth.signOut()
                    return Result.failure(Exception("Access Denied: Not an Admin"))
                }
                if (profileStatus == "suspended" || profileStatus == "blocked") {
                    auth.signOut()
                    return Result.failure(Exception("Account Suspended: Please contact the administrator."))
                }

                var needsUpdate = false
                val updates = mutableMapOf<String, Any>()
                if (!user.accountStatus.equals("active", ignoreCase = true)) {
                    updates["accountStatus"] = "active"
                    user = user.copy(accountStatus = "active")
                    needsUpdate = true
                }
                if (user.id != uid) {
                    updates["id"] = uid
                    user = user.copy(id = uid)
                    needsUpdate = true
                }
                if (user.role != "admin") {
                    updates["role"] = "admin"
                    user = user.copy(role = "admin")
                    needsUpdate = true
                }
                if (needsUpdate) {
                    try {
                        db.collection("users").document(uid).update(updates).await()
                    } catch (_: Exception) {}
                }
                return Result.success(user.copy(id = uid, role = "admin", accountStatus = "active"))
            }

            // 3. Generic login (fallback without expected role)
            if (profileRole == "admin") {
                if (profileStatus == "suspended" || profileStatus == "blocked") {
                    auth.signOut()
                    return Result.failure(Exception("Account Suspended: Please contact the administrator."))
                }
                return Result.success(user.copy(id = uid, role = "admin", accountStatus = "active"))
            } else if (profileRole == "public") {
                if (!firebaseUser.isEmailVerified) {
                    auth.signOut()
                    return Result.failure(Exception("Email Not Verified: Please verify your email before logging in. Check your inbox for the verification link."))
                }
                when (profileStatus) {
                    "pending" -> {
                        auth.signOut()
                        return Result.failure(Exception("Account Pending Approval: Your email is verified, but your account is waiting for Admin approval."))
                    }
                    "rejected" -> {
                        auth.signOut()
                        return Result.failure(Exception("Account Not Approved: Your account registration was not approved by Admin."))
                    }
                    "suspended", "blocked" -> {
                        auth.signOut()
                        return Result.failure(Exception("Account Suspended: Please contact the administrator."))
                    }
                    "active" -> {
                        return Result.success(user.copy(id = uid, role = "public", accountStatus = "active", emailVerified = true))
                    }
                    else -> {
                        auth.signOut()
                        return Result.failure(Exception("Account status not allowed: $profileStatus"))
                    }
                }
            } else {
                auth.signOut()
                return Result.failure(Exception("Unknown user role."))
            }
        } catch (e: Exception) {
            val errorCode = if (e is FirebaseAuthException) e.errorCode else "UNKNOWN"
            if (errorCode == "ERROR_INVALID_CREDENTIAL" || errorCode == "INVALID_LOGIN_CREDENTIALS") {
                Log.d("AuthDebug", "Login failed: Invalid email or password.")
                Result.failure(Exception("Invalid email or password. Please verify your credentials and try again."))
            } else {
                Log.e("AuthDebug", "Firebase auth failed with Error Code: $errorCode, Exception: ${e.message}")
                Result.failure(e)
            }
        }
    }

    suspend fun registerPublicUser(name: String, email: String, mobile: String, pass: String): Result<User> {
        val trimmedEmail = email.trim()
        return try {
            val res = auth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
            val firebaseUser = res.user ?: throw Exception("User ID not found")
            val uid = firebaseUser.uid

            // Send Firebase email verification immediately
            try {
                firebaseUser.sendEmailVerification().await()
            } catch (e: Exception) {
                Log.e("AuthDebug", "Failed to send verification email: ${e.message}")
            }

            val user = User(
                id = uid,
                name = name.trim(),
                email = trimmedEmail,
                mobile = mobile.trim(),
                role = "public",
                accountStatus = "pending",
                createdAt = System.currentTimeMillis(),
                phoneVerified = false,
                emailVerified = false
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

    suspend fun checkCurrentUserEmailVerification(): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No active user session."))
            user.reload().await()
            val isVerified = user.isEmailVerified
            if (isVerified) {
                db.collection("users").document(user.uid).update("emailVerified", true).await()
            }
            Result.success(isVerified)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No active user session."))
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmailForCredentials(email: String, pass: String): Result<Unit> {
        return try {
            val res = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            res.user?.sendEmailVerification()?.await()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            auth.signOut()
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
    
    suspend fun fetchCurrentUserProfile(): User? {
        val firebaseUser = auth.currentUser ?: return null
        val uid = firebaseUser.uid
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                auth.signOut()
                return null
            }
            var user = doc.toObject(User::class.java) ?: run {
                auth.signOut()
                return null
            }

            val profileRole = (doc.getString("role") ?: user.role).trim().lowercase()
            val profileStatus = (doc.getString("accountStatus") ?: user.accountStatus).trim().lowercase()

            if (profileRole == "admin") {
                if (profileStatus == "blocked" || profileStatus == "suspended") {
                    auth.signOut()
                    return null
                }
                var needsUpdate = false
                val updates = mutableMapOf<String, Any>()
                if (!user.accountStatus.equals("active", ignoreCase = true)) {
                    updates["accountStatus"] = "active"
                    user = user.copy(accountStatus = "active")
                    needsUpdate = true
                }
                if (user.id != uid) {
                    updates["id"] = uid
                    user = user.copy(id = uid)
                    needsUpdate = true
                }
                if (user.role != "admin") {
                    updates["role"] = "admin"
                    user = user.copy(role = "admin")
                    needsUpdate = true
                }
                if (needsUpdate) {
                    try {
                        db.collection("users").document(uid).update(updates).await()
                    } catch (_: Exception) {}
                }
                user.copy(id = uid, role = "admin", accountStatus = "active")
            } else if (profileRole == "public") {
                // Must be active and email verified
                if (!firebaseUser.isEmailVerified || profileStatus != "active") {
                    auth.signOut()
                    return null
                }
                user.copy(id = uid, role = "public", accountStatus = "active", emailVerified = true)
            } else {
                auth.signOut()
                null
            }
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
            db.collection("users").document(userId).update(
                mapOf(
                    "accountStatus" to status,
                    "adminSeen" to true,
                    "seen" to true
                )
            ).await()
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

    suspend fun markUserAsSeen(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(
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
