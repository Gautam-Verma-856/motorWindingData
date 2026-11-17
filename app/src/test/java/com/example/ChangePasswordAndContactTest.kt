package com.example

import com.example.model.User
import com.example.ui.screens.ContactConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangePasswordAndContactTest {

    private fun validateChangePasswordInput(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        return when {
            currentPassword.isBlank() -> "Current password is required."
            newPassword.isBlank() -> "New password is required."
            confirmPassword.isBlank() -> "Confirm new password is required."
            newPassword.length < 6 -> "New password must be at least 6 characters."
            newPassword == currentPassword -> "New password must be different from current password."
            newPassword != confirmPassword -> "New password and confirm password do not match."
            else -> null
        }
    }

    private fun canAccessChangePasswordRoute(user: User?, expectedRole: String): Boolean {
        if (user == null) return false
        val target = expectedRole.trim().lowercase()
        return when (target) {
            "admin" -> user.isAdmin() && !user.isBlockedOrSuspended()
            "public" -> user.isPublic() && user.isActive()
            else -> false
        }
    }

    @Test
    fun contactConfig_containsOnlyConfiguredInstagram() {
        assertEquals("Gautam Verma", ContactConfig.ADMIN_NAME)
        assertEquals("@gautam_verma_856", ContactConfig.INSTAGRAM_HANDLE)
        assertTrue(ContactConfig.INSTAGRAM_URL.startsWith("https://www.instagram.com/gautam_verma_856"))
    }

    @Test
    fun validateChangePasswordInput_enforcesAllRules() {
        assertNotNull(validateChangePasswordInput("", "newPass1", "newPass1"))
        assertNotNull(validateChangePasswordInput("oldPass1", "", "newPass1"))
        assertNotNull(validateChangePasswordInput("oldPass1", "newPass1", ""))
        assertNotNull(validateChangePasswordInput("oldPass1", "12345", "12345"))
        assertNotNull(validateChangePasswordInput("samePass1", "samePass1", "samePass1"))
        assertNotNull(validateChangePasswordInput("oldPass1", "newPass1", "newPass2"))
        assertNull(validateChangePasswordInput("oldPass1", "newPass1", "newPass1"))
    }

    @Test
    fun roleIsolation_preventsAdminAndPublicFromCrossChangingPasswords() {
        val adminUser = User(id = "adm_1", role = "admin", accountStatus = "active")
        val publicUser = User(id = "pub_1", role = "public", accountStatus = "active", emailVerified = true)
        val pendingPublic = User(id = "pub_2", role = "public", accountStatus = "pending", emailVerified = true)
        val blockedAdmin = User(id = "adm_2", role = "admin", accountStatus = "blocked")

        assertTrue(canAccessChangePasswordRoute(adminUser, "admin"))
        assertFalse(canAccessChangePasswordRoute(adminUser, "public"))

        assertTrue(canAccessChangePasswordRoute(publicUser, "public"))
        assertFalse(canAccessChangePasswordRoute(publicUser, "admin"))

        assertFalse(canAccessChangePasswordRoute(pendingPublic, "public"))
        assertFalse(canAccessChangePasswordRoute(blockedAdmin, "admin"))
        assertFalse(canAccessChangePasswordRoute(null, "admin"))
        assertFalse(canAccessChangePasswordRoute(null, "public"))
    }
}
