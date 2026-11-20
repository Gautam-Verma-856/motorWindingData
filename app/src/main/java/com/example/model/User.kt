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
    val seen: Boolean = false,
    val photoUrl: String = ""
) {
    fun isUnseen(): Boolean = role.trim().equals("public", ignoreCase = true) &&
        accountStatus.trim().equals("pending", ignoreCase = true) && !adminSeen && !seen

    fun isAdmin(): Boolean = role.trim().equals("admin", ignoreCase = true)
    fun isPublic(): Boolean = role.trim().equals("public", ignoreCase = true)
    fun isActive(): Boolean = accountStatus.trim().equals("active", ignoreCase = true)
    fun isPending(): Boolean = accountStatus.trim().equals("pending", ignoreCase = true)
    fun isBlockedOrSuspended(): Boolean =
        accountStatus.trim().equals("blocked", ignoreCase = true) ||
        accountStatus.trim().equals("suspended", ignoreCase = true)
}
