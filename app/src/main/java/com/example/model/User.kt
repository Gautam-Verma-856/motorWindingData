package com.example.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val role: String = "public", // "admin" or "public"
    val accountStatus: String = "active", // "pending", "active", "blocked", "rejected"
    val createdAt: Long = System.currentTimeMillis(),
    val phoneVerified: Boolean = false,
    val emailVerified: Boolean = false,
    val adminSeen: Boolean = false,
    val seen: Boolean = false
) {
    fun isUnseen(): Boolean = role.equals("public", ignoreCase = true) &&
        accountStatus.equals("pending", ignoreCase = true) && !adminSeen && !seen
}
