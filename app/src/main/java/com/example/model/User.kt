package com.example.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val role: String = "public", // "admin" or "public"
    val accountStatus: String = "active", // "active", "blocked"
    val createdAt: Long = System.currentTimeMillis()
)
