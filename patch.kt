package com.example.repository

import android.util.Log
import com.example.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    suspend fun login(email: String, password: String): Result<User> {
        Log.d("AuthDebug", "Attempting login with email length: ${email.trim().length}, password length: ${password.length}")
        return try {
            val res = auth.signInWithEmailAndPassword(email.trim(), password).await()
            Log.d("AuthDebug", "Firebase auth succeeded for uid: ${res.user?.uid}")
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
            Log.e("AuthDebug", "Firebase auth failed with exception: ${e.javaClass.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }
