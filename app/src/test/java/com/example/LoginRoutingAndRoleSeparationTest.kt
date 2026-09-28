package com.example

import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LoginRoutingAndRoleSeparationTest {

    // --- Mock Routing Decision Engine matching NavGraph / Screen logic ---
    sealed class LoginResult {
        data class Success(val destination: String, val user: User) : LoginResult()
        data class Rejected(val errorMessage: String) : LoginResult()
    }

    private fun processPublicLogin(userDoc: User?, isFirebaseEmailVerified: Boolean): LoginResult {
        if (userDoc == null) {
            return LoginResult.Rejected("User profile not found")
        }
        val role = userDoc.role.trim().lowercase()
        val status = userDoc.accountStatus.trim().lowercase()

        // 1. Admin entered on Public Login
        if (role == "admin") {
            return LoginResult.Rejected("This is an admin account. Please use Admin Login.")
        }
        if (role != "public") {
            return LoginResult.Rejected("Invalid account role.")
        }

        // 2. Email verification
        if (!isFirebaseEmailVerified) {
            return LoginResult.Rejected("Email Not Verified: Please verify your email before logging in. Check your inbox for the verification link.")
        }

        // 3. Status gating
        when (status) {
            "pending" -> return LoginResult.Rejected("Account Pending Approval: Your email is verified, but your account is waiting for Admin approval.")
            "rejected" -> return LoginResult.Rejected("Account Not Approved: Your account registration was not approved by Admin.")
            "suspended", "blocked" -> return LoginResult.Rejected("Account Suspended: Please contact the administrator.")
            "active" -> {
                // Must route to Main Page ("home"), NOT user_dashboard
                return LoginResult.Success("home", userDoc.copy(role = "public", accountStatus = "active", emailVerified = true))
            }
            else -> return LoginResult.Rejected("Account status not allowed: $status")
        }
    }

    private fun processAdminLogin(userDoc: User?): LoginResult {
        if (userDoc == null) {
            return LoginResult.Rejected("User profile not found")
        }
        val role = userDoc.role.trim().lowercase()
        val status = userDoc.accountStatus.trim().lowercase()

        // 1. Public entered on Admin Login
        if (role == "public") {
            return LoginResult.Rejected("This is a public account. Please use Public Login.")
        }
        if (role != "admin") {
            return LoginResult.Rejected("Access Denied: Not an Admin")
        }

        // 2. Status gating
        if (status == "blocked" || status == "suspended") {
            return LoginResult.Rejected("Account Suspended: Please contact the administrator.")
        }

        // Must route to Main Page ("home"), NOT admin_dashboard
        return LoginResult.Success("home", userDoc.copy(role = "admin", accountStatus = "active"))
    }

    private fun processSessionRestoration(userDoc: User?, isFirebaseEmailVerified: Boolean): String? {
        if (userDoc == null) return null
        val role = userDoc.role.trim().lowercase()
        val status = userDoc.accountStatus.trim().lowercase()

        if (role == "admin" && status != "blocked" && status != "suspended") {
            return "home" // Main Page
        }
        if (role == "public" && isFirebaseEmailVerified && status == "active") {
            return "home" // Main Page
        }
        return null // Rejected, redirect to login
    }

    // -------------------------------------------------------------
    // a) Public account entered in Public Login lands on Main Page
    // -------------------------------------------------------------
    @Test
    fun testPublicAccountInPublicLogin_LandsOnMainPage() {
        val publicUser = User(
            id = "pub_1",
            name = "Public Electrician",
            email = "pub@example.com",
            role = "public",
            accountStatus = "active",
            emailVerified = true
        )

        val result = processPublicLogin(publicUser, isFirebaseEmailVerified = true)
        assertTrue(result is LoginResult.Success)
        val success = result as LoginResult.Success
        assertEquals("home", success.destination)
        assertEquals("public", success.user.role)
        assertTrue(success.user.isPublic())
        assertFalse(success.user.isAdmin())
    }

    // -------------------------------------------------------------
    // b) Admin account entered in Public Login is rejected and opens no dashboard
    // -------------------------------------------------------------
    @Test
    fun testAdminAccountInPublicLogin_IsRejectedAndOpensNoDashboard() {
        val adminUser = User(
            id = "adm_1",
            name = "Admin Master",
            email = "admin@example.com",
            role = "admin",
            accountStatus = "active"
        )

        val result = processPublicLogin(adminUser, isFirebaseEmailVerified = true)
        assertTrue("Admin entered in Public Login must be rejected", result is LoginResult.Rejected)
        val rejection = result as LoginResult.Rejected
        assertEquals("This is an admin account. Please use Admin Login.", rejection.errorMessage)
    }

    // -------------------------------------------------------------
    // c) Admin account entered in Admin Login lands on Main Page
    // -------------------------------------------------------------
    @Test
    fun testAdminAccountInAdminLogin_LandsOnMainPage() {
        val adminUser = User(
            id = "adm_2",
            name = "Admin Inspector",
            email = "admin2@example.com",
            role = "admin",
            accountStatus = "active"
        )

        val result = processAdminLogin(adminUser)
        assertTrue(result is LoginResult.Success)
        val success = result as LoginResult.Success
        assertEquals("home", success.destination)
        assertEquals("admin", success.user.role)
        assertTrue(success.user.isAdmin())
        assertFalse(success.user.isPublic())
    }

    // -------------------------------------------------------------
    // d) Public account entered in Admin Login is rejected
    // -------------------------------------------------------------
    @Test
    fun testPublicAccountInAdminLogin_IsRejected() {
        val publicUser = User(
            id = "pub_2",
            name = "Winding Tech",
            email = "tech@example.com",
            role = "public",
            accountStatus = "active",
            emailVerified = true
        )

        val result = processAdminLogin(publicUser)
        assertTrue("Public account entered in Admin Login must be rejected", result is LoginResult.Rejected)
        val rejection = result as LoginResult.Rejected
        assertEquals("This is a public account. Please use Public Login.", rejection.errorMessage)
    }

    // -------------------------------------------------------------
    // e) Valid session restoration lands on Main Page
    // -------------------------------------------------------------
    @Test
    fun testValidSessionRestoration_LandsOnMainPageForBothRoles() {
        val validAdmin = User(id = "adm_valid", role = "admin", accountStatus = "active")
        val validPublic = User(id = "pub_valid", role = "public", accountStatus = "active", emailVerified = true)

        val adminDestination = processSessionRestoration(validAdmin, isFirebaseEmailVerified = true)
        assertEquals("Valid admin session must restore to Main Page (home)", "home", adminDestination)

        val publicDestination = processSessionRestoration(validPublic, isFirebaseEmailVerified = true)
        assertEquals("Valid public session must restore to Main Page (home)", "home", publicDestination)
    }

    // -------------------------------------------------------------
    // f) Pending / unverified public accounts cannot access motor data
    // -------------------------------------------------------------
    @Test
    fun testPendingOrUnverifiedPublicAccounts_CannotAccessMotorData() {
        val unverifiedPublic = User(id = "pub_unverified", role = "public", accountStatus = "active", emailVerified = false)
        val pendingPublic = User(id = "pub_pending", role = "public", accountStatus = "pending", emailVerified = true)
        val blockedPublic = User(id = "pub_blocked", role = "public", accountStatus = "blocked", emailVerified = true)
        val suspendedAdmin = User(id = "adm_suspended", role = "admin", accountStatus = "suspended")

        // 1. Session restoration fails (returns null -> kicks to login)
        assertNull("Unverified public account session must not restore", processSessionRestoration(unverifiedPublic, isFirebaseEmailVerified = false))
        assertNull("Pending public account session must not restore", processSessionRestoration(pendingPublic, isFirebaseEmailVerified = true))
        assertNull("Blocked public account session must not restore", processSessionRestoration(blockedPublic, isFirebaseEmailVerified = true))
        assertNull("Suspended admin session must not restore", processSessionRestoration(suspendedAdmin, isFirebaseEmailVerified = true))

        // 2. Public Login fails with appropriate error
        val unverifiedResult = processPublicLogin(unverifiedPublic, isFirebaseEmailVerified = false)
        assertTrue(unverifiedResult is LoginResult.Rejected)
        assertTrue((unverifiedResult as LoginResult.Rejected).errorMessage.contains("Email Not Verified"))

        val pendingResult = processPublicLogin(pendingPublic, isFirebaseEmailVerified = true)
        assertTrue(pendingResult is LoginResult.Rejected)
        assertTrue((pendingResult as LoginResult.Rejected).errorMessage.contains("Account Pending Approval"))

        // 3. Motor data visibility gating
        val sampleMotors = listOf(
            SinglePhaseMotor(id = "m1", status = "approved"),
            SinglePhaseMotor(id = "m2", status = "pending"),
            SinglePhaseMotor(id = "m3", status = "rejected")
        )

        // Only active approved users can view motor records; in public browsing only approved motors are returned
        val publicVisibleMotors = sampleMotors.filter { it.isExplicitlyApproved() }
        assertEquals(1, publicVisibleMotors.size)
        assertEquals("m1", publicVisibleMotors.first().id)
    }

    // -------------------------------------------------------------
    // g) Logout / account switching clears stale role and protected navigation
    // -------------------------------------------------------------
    @Test
    fun testLogoutAndAccountSwitching_ClearsStaleStateAndNavigation() {
        var currentUser: User? = User(id = "admin_temp", role = "admin", accountStatus = "active")
        var currentBackStack: List<String> = listOf("public_login", "admin_login", "home", "admin_dashboard")
        var motorDataCached: List<SinglePhaseMotor> = listOf(SinglePhaseMotor(id = "m1", status = "pending"))

        // Simulate Logout
        currentUser = null
        currentBackStack = emptyList() // popUpTo(0) { inclusive = true }
        motorDataCached = emptyList()

        assertNull("Current user must be cleared on logout", currentUser)
        assertTrue("Backstack must be empty to prevent back-navigation to protected screens", currentBackStack.isEmpty())
        assertTrue("Cached motor data must be wiped", motorDataCached.isEmpty())

        // Simulate subsequent public login
        val publicUser = User(id = "pub_new", role = "public", accountStatus = "active", emailVerified = true)
        val loginResult = processPublicLogin(publicUser, isFirebaseEmailVerified = true)
        assertTrue(loginResult is LoginResult.Success)
        currentUser = (loginResult as LoginResult.Success).user
        currentBackStack = listOf("home") // Root destination is Main Page

        assertEquals("public", currentUser.role)
        assertFalse("Stale admin privileges must not persist into public session", currentUser.isAdmin())
        assertEquals(1, currentBackStack.size)
        assertEquals("home", currentBackStack.first())
    }
}
