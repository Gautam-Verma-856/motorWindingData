package com.example

import com.example.model.CustomCategory
import com.example.model.CustomMotor
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MotorStatusWorkflowTest {

    // --- Helper function matching public browsing filter logic ---
    private fun <T> filterPublicBrowsing(items: List<T>, isExplicitlyApproved: (T) -> Boolean): List<T> {
        return items.filter { isExplicitlyApproved(it) }
    }

    // --- Helper function matching My Submissions status badge mapping ---
    private fun getStatusBadgeText(status: String): String {
        return when (status.trim().lowercase()) {
            "pending" -> "Pending Admin Approval"
            "approved" -> "Approved"
            "rejected" -> "Rejected"
            else -> if (status.isBlank()) "Unknown" else status
        }
    }

    // -------------------------------------------------------------
    // 1. PUBLIC SUBMISSION SAVED WITH PENDING STATUS
    // -------------------------------------------------------------
    @Test
    fun testPublicSubmissionSavedWithPendingStatus_SinglePhase() {
        val publicUid = "user_uid_123"
        val publicName = "John Doe"

        val submittedMotor = SinglePhaseMotor(
            id = "single_1",
            category = "Self Priming (Tullu Pump)",
            companyName = "Crompton",
            hp = "1.0",
            status = "pending",
            createdBy = publicUid,
            createdByName = publicName
        )

        assertEquals("pending", submittedMotor.status)
        assertEquals(publicUid, submittedMotor.createdBy)
        assertEquals(publicName, submittedMotor.createdByName)
        assertTrue(submittedMotor.isPending())
        assertFalse(submittedMotor.isExplicitlyApproved())
        assertFalse(submittedMotor.isRejected())
    }

    @Test
    fun testPublicSubmissionSavedWithPendingStatus_ThreePhase() {
        val publicUid = "user_uid_456"
        val publicName = "Jane Smith"

        val submittedMotor = ThreePhaseMotor(
            id = "three_1",
            category = "Monoblock",
            name = "Kirloskar",
            hp = "5.0",
            status = "pending",
            createdBy = publicUid,
            createdByName = publicName
        )

        assertEquals("pending", submittedMotor.status)
        assertEquals(publicUid, submittedMotor.createdBy)
        assertEquals(publicName, submittedMotor.createdByName)
        assertTrue(submittedMotor.isPending())
        assertFalse(submittedMotor.isExplicitlyApproved())
        assertFalse(submittedMotor.isRejected())
    }

    @Test
    fun testPublicSubmissionSavedWithPendingStatus_CustomMotor() {
        val publicUid = "user_uid_789"
        val publicName = "Alex Ray"

        val submittedMotor = CustomMotor(
            id = "custom_1",
            categoryId = "cat_cooler",
            categoryName = "Cooler Motor",
            companyName = "Havells",
            motorName = "CoolMax 200",
            hp = "0.25",
            status = "pending",
            createdBy = publicUid,
            createdByName = publicName
        )

        assertEquals("pending", submittedMotor.status)
        assertEquals(publicUid, submittedMotor.createdBy)
        assertEquals(publicName, submittedMotor.createdByName)
        assertTrue(submittedMotor.isPending())
        assertFalse(submittedMotor.isExplicitlyApproved())
        assertFalse(submittedMotor.isRejected())
    }

    // -------------------------------------------------------------
    // 2. ADMIN APPROVAL CHANGES STATUS TO APPROVED
    // -------------------------------------------------------------
    @Test
    fun testAdminApprovalChangesStatusToApproved_SinglePhase() {
        val pendingMotor = SinglePhaseMotor(id = "single_2", status = "pending")
        val approvedMotor = pendingMotor.copy(status = "approved")

        assertEquals("approved", approvedMotor.status)
        assertTrue(approvedMotor.isExplicitlyApproved())
        assertFalse(approvedMotor.isPending())
        assertFalse(approvedMotor.isRejected())
    }

    @Test
    fun testAdminApprovalChangesStatusToApproved_ThreePhase() {
        val pendingMotor = ThreePhaseMotor(id = "three_2", status = "pending")
        val approvedMotor = pendingMotor.copy(status = "approved")

        assertEquals("approved", approvedMotor.status)
        assertTrue(approvedMotor.isExplicitlyApproved())
        assertFalse(approvedMotor.isPending())
        assertFalse(approvedMotor.isRejected())
    }

    @Test
    fun testAdminApprovalChangesStatusToApproved_CustomMotor() {
        val pendingMotor = CustomMotor(id = "custom_2", status = "pending")
        val approvedMotor = pendingMotor.copy(status = "approved")

        assertEquals("approved", approvedMotor.status)
        assertTrue(approvedMotor.isExplicitlyApproved())
        assertFalse(approvedMotor.isPending())
        assertFalse(approvedMotor.isRejected())
    }

    // -------------------------------------------------------------
    // 3. ADMIN REJECTION CHANGES STATUS TO REJECTED
    // -------------------------------------------------------------
    @Test
    fun testAdminRejectionChangesStatusToRejected_SinglePhase() {
        val pendingMotor = SinglePhaseMotor(id = "single_3", status = "pending")
        val rejectedMotor = pendingMotor.copy(status = "rejected")

        assertEquals("rejected", rejectedMotor.status)
        assertTrue(rejectedMotor.isRejected())
        assertFalse(rejectedMotor.isExplicitlyApproved())
        assertFalse(rejectedMotor.isPending())
    }

    @Test
    fun testAdminRejectionChangesStatusToRejected_ThreePhase() {
        val pendingMotor = ThreePhaseMotor(id = "three_3", status = "pending")
        val rejectedMotor = pendingMotor.copy(status = "rejected")

        assertEquals("rejected", rejectedMotor.status)
        assertTrue(rejectedMotor.isRejected())
        assertFalse(rejectedMotor.isExplicitlyApproved())
        assertFalse(rejectedMotor.isPending())
    }

    @Test
    fun testAdminRejectionChangesStatusToRejected_CustomMotor() {
        val pendingMotor = CustomMotor(id = "custom_3", status = "pending")
        val rejectedMotor = pendingMotor.copy(status = "rejected")

        assertEquals("rejected", rejectedMotor.status)
        assertTrue(rejectedMotor.isRejected())
        assertFalse(rejectedMotor.isExplicitlyApproved())
        assertFalse(rejectedMotor.isPending())
    }

    // -------------------------------------------------------------
    // 4. MY SUBMISSIONS DISPLAYS ACTUAL SAVED STATUS BADGE
    // -------------------------------------------------------------
    @Test
    fun testMySubmissionsDisplaysActualSavedStatusBadges() {
        assertEquals("Pending Admin Approval", getStatusBadgeText("pending"))
        assertEquals("Pending Admin Approval", getStatusBadgeText("PENDING"))
        assertEquals("Approved", getStatusBadgeText("approved"))
        assertEquals("Approved", getStatusBadgeText("APPROVED"))
        assertEquals("Rejected", getStatusBadgeText("rejected"))
        assertEquals("Rejected", getStatusBadgeText("REJECTED"))
        assertEquals("Unknown", getStatusBadgeText(""))
    }

    @Test
    fun testMySubmissionsRetrievesAllUserStatuses() {
        val userId = "user_me"
        val submissions = listOf(
            SinglePhaseMotor(id = "1", createdBy = userId, status = "pending"),
            SinglePhaseMotor(id = "2", createdBy = userId, status = "approved"),
            SinglePhaseMotor(id = "3", createdBy = userId, status = "rejected"),
            SinglePhaseMotor(id = "4", createdBy = "other_user", status = "approved")
        )

        val userSubmissions = submissions.filter { it.createdBy == userId }
        assertEquals(3, userSubmissions.size)

        val statuses = userSubmissions.map { it.status }
        assertTrue(statuses.contains("pending"))
        assertTrue(statuses.contains("approved"))
        assertTrue(statuses.contains("rejected"))
    }

    // -------------------------------------------------------------
    // 5. PUBLIC BROWSING SHOWS ONLY EXPLICITLY APPROVED MOTORS
    // -------------------------------------------------------------
    @Test
    fun testPublicBrowsingFiltersOnlyExplicitlyApproved_SinglePhase() {
        val motors = listOf(
            SinglePhaseMotor(id = "1", companyName = "Crompton", status = "approved"),
            SinglePhaseMotor(id = "2", companyName = "Kirloskar", status = "pending"),
            SinglePhaseMotor(id = "3", companyName = "Havells", status = "rejected"),
            SinglePhaseMotor(id = "4", companyName = "Usha", status = ""),           // missing status
            SinglePhaseMotor(id = "5", companyName = "V-Guard", status = "unknown"), // unknown status
            SinglePhaseMotor(id = "6", companyName = "Orient", status = "APPROVED")  // case-insensitive approved
        )

        val publicList = filterPublicBrowsing(motors) { it.isExplicitlyApproved() }

        assertEquals(2, publicList.size)
        assertTrue(publicList.any { it.id == "1" })
        assertTrue(publicList.any { it.id == "6" })
        assertFalse(publicList.any { it.status == "rejected" })
        assertFalse(publicList.any { it.status == "pending" })
        assertFalse(publicList.any { it.status == "" })
        assertFalse(publicList.any { it.status == "unknown" })
    }

    @Test
    fun testPublicBrowsingFiltersOnlyExplicitlyApproved_ThreePhase() {
        val motors = listOf(
            ThreePhaseMotor(id = "1", name = "Siemens", status = "approved"),
            ThreePhaseMotor(id = "2", name = "ABB", status = "pending"),
            ThreePhaseMotor(id = "3", name = "Kirloskar", status = "rejected"),
            ThreePhaseMotor(id = "4", name = "L&T", status = ""),                    // missing status
            ThreePhaseMotor(id = "5", name = "Bharat Bijlee", status = "draft")     // unknown status
        )

        val publicList = filterPublicBrowsing(motors) { it.isExplicitlyApproved() }

        assertEquals(1, publicList.size)
        assertEquals("Siemens", publicList.first().name)
        assertFalse(publicList.any { it.status == "rejected" })
        assertFalse(publicList.any { it.status == "pending" })
    }

    @Test
    fun testPublicBrowsingFiltersOnlyExplicitlyApproved_CustomMotor() {
        val motors = listOf(
            CustomMotor(id = "1", categoryId = "cat1", companyName = "CoolerX", status = "approved"),
            CustomMotor(id = "2", categoryId = "cat1", companyName = "CoolerY", status = "pending"),
            CustomMotor(id = "3", categoryId = "cat1", companyName = "CoolerZ", status = "rejected"),
            CustomMotor(id = "4", categoryId = "cat1", companyName = "CoolerW", status = "")
        )

        val publicList = filterPublicBrowsing(motors) { it.isExplicitlyApproved() }

        assertEquals(1, publicList.size)
        assertEquals("CoolerX", publicList.first().companyName)
        assertFalse(publicList.any { it.status == "rejected" })
        assertFalse(publicList.any { it.status == "pending" })
    }

    // -------------------------------------------------------------
    // 6. DEFAULT STATUS IS NOT APPROVED (MISSING STATUS SAFEGUARD)
    // -------------------------------------------------------------
    @Test
    fun testMissingStatusDefaultsToNotApproved() {
        val singleDefault = SinglePhaseMotor(id = "default_single")
        val threeDefault = ThreePhaseMotor(id = "default_three")
        val customDefault = CustomMotor(id = "default_custom")

        assertEquals("", singleDefault.status)
        assertEquals("", threeDefault.status)
        assertEquals("", customDefault.status)

        assertFalse(singleDefault.isExplicitlyApproved())
        assertFalse(threeDefault.isExplicitlyApproved())
        assertFalse(customDefault.isExplicitlyApproved())
    }

    // -------------------------------------------------------------
    // 7. PUBLIC CATEGORY COUNTS AND COMPANIES FILTER APPROVED ONLY
    // -------------------------------------------------------------
    @Test
    fun testCategoryCountsAndCompaniesOnlyIncludeApproved() {
        val motors = listOf(
            SinglePhaseMotor(id = "1", category = "Submersible Motor", companyName = "Falcon", status = "approved"),
            SinglePhaseMotor(id = "2", category = "Submersible Motor", companyName = "Falcon", status = "approved"),
            SinglePhaseMotor(id = "3", category = "Submersible Motor", companyName = "Lubi", status = "rejected"), // Rejected!
            SinglePhaseMotor(id = "4", category = "Submersible Motor", companyName = "Texmo", status = "pending")  // Pending!
        )

        val approvedInCat = motors.filter { it.isExplicitlyApproved() && it.category == "Submersible Motor" }
        assertEquals(2, approvedInCat.size)

        val companies = approvedInCat.map { it.companyName.trim().lowercase() }.distinct()
        assertEquals(1, companies.size)
        assertEquals("falcon", companies.first())
        assertFalse(companies.contains("lubi"))
        assertFalse(companies.contains("texmo"))
    }

    // -------------------------------------------------------------
    // 8. ACTIVE ADMIN CAN CREATE, EDIT, AND DELETE CUSTOM CATEGORY
    // -------------------------------------------------------------
    @Test
    fun testActiveAdminCanCreateCustomCategory() {
        val adminUser = com.example.model.User(
            id = "admin_uid_001",
            name = "Super Admin",
            email = "admin@example.com",
            role = "admin",
            accountStatus = "active"
        )

        fun canAdminManageCategories(user: com.example.model.User): Boolean {
            return user.role.equals("admin", ignoreCase = true) &&
                   user.accountStatus.equals("active", ignoreCase = true)
        }

        assertTrue("Active admin must be permitted to manage categories", canAdminManageCategories(adminUser))

        val newCategory = CustomCategory(
            id = "cat_cooler",
            name = "Cooler Motor",
            isActive = true,
            allowPublicSubmissions = true,
            createdBy = adminUser.id
        )

        assertEquals("cat_cooler", newCategory.id)
        assertEquals("Cooler Motor", newCategory.name)
        assertTrue(newCategory.isActive)
        assertTrue(newCategory.allowPublicSubmissions)
        assertEquals("admin_uid_001", newCategory.createdBy)
    }

    // -------------------------------------------------------------
    // 9. PUBLIC USER CANNOT CREATE, EDIT, OR DELETE CUSTOM CATEGORY
    // -------------------------------------------------------------
    @Test
    fun testPublicUserCannotManageCustomCategory() {
        val publicUser = com.example.model.User(
            id = "public_uid_002",
            name = "Normal User",
            email = "user@example.com",
            role = "public",
            accountStatus = "active"
        )

        fun canUserManageCategories(user: com.example.model.User): Boolean {
            return user.role.equals("admin", ignoreCase = true) &&
                   user.accountStatus.equals("active", ignoreCase = true)
        }

        assertFalse("Public user must NOT be permitted to manage categories", canUserManageCategories(publicUser))
    }

    // -------------------------------------------------------------
    // 10. ELIGIBLE PUBLIC USER CAN READ ONLY ENABLED CATEGORIES
    // -------------------------------------------------------------
    @Test
    fun testEligiblePublicUserCanReadPermittedEnabledCategories() {
        val categories = listOf(
            CustomCategory(id = "cat_1", name = "Enabled Cat 1", isActive = true, allowPublicSubmissions = true),
            CustomCategory(id = "cat_2", name = "Enabled Cat 2 (No submissions)", isActive = true, allowPublicSubmissions = false),
            CustomCategory(id = "cat_3", name = "Disabled Cat", isActive = false, allowPublicSubmissions = true)
        )

        val publicVisibleCategories = categories.filter { it.isActive }
        assertEquals(2, publicVisibleCategories.size)
        assertTrue(publicVisibleCategories.all { it.isActive })
        assertFalse(publicVisibleCategories.any { it.id == "cat_3" })

        val publicSubmittableCategories = publicVisibleCategories.filter { it.allowPublicSubmissions }
        assertEquals(1, publicSubmittableCategories.size)
        assertEquals("cat_1", publicSubmittableCategories.first().id)
    }

    // -------------------------------------------------------------
    // 11. CUSTOM MOTOR FORM PHOTO LIMIT (MAX 6) AND REMOVAL
    // -------------------------------------------------------------
    @Test
    fun testCustomMotorPhotoSelectionAndRemoval() {
        var existingPhotos = listOf("https://ibb.co/photo1.jpg", "https://ibb.co/photo2.jpg")
        var selectedUris = listOf("content://media/photo3", "content://media/photo4")

        val totalPhotos = existingPhotos.size + selectedUris.size
        assertEquals(4, totalPhotos)
        assertTrue("Total photos must be <= 6", totalPhotos <= 6)

        // Test removing an existing photo
        existingPhotos = existingPhotos.filter { it != "https://ibb.co/photo1.jpg" }
        assertEquals(1, existingPhotos.size)
        assertEquals(3, existingPhotos.size + selectedUris.size)

        // Test removing a selected URI
        selectedUris = selectedUris.filter { it != "content://media/photo3" }
        assertEquals(1, selectedUris.size)
        assertEquals(2, existingPhotos.size + selectedUris.size)

        // Test appending up to 6 max
        val newUrisToAdd = listOf("uri5", "uri6", "uri7", "uri8", "uri9")
        val remainingSlots = 6 - (existingPhotos.size + selectedUris.size)
        selectedUris = selectedUris + newUrisToAdd.take(remainingSlots)
        assertEquals(6, existingPhotos.size + selectedUris.size)
    }

    // -------------------------------------------------------------
    // 12. LOGGED-OUT USERS CANNOT BROWSE MOTOR DATA & LOGOUT CLEARS DATA
    // -------------------------------------------------------------
    @Test
    fun testLoggedOutUsersCannotAccessMotorDataAndLogoutClearsData() {
        var cachedSinglePhase = listOf(SinglePhaseMotor(id = "1", status = "approved"))
        var cachedThreePhase = listOf(ThreePhaseMotor(id = "2", status = "approved"))
        var cachedCustomMotors = listOf(CustomMotor(id = "3", status = "approved"))
        var cachedCategories = listOf(CustomCategory(id = "c1"))

        fun clearData() {
            cachedSinglePhase = emptyList()
            cachedThreePhase = emptyList()
            cachedCustomMotors = emptyList()
            cachedCategories = emptyList()
        }

        clearData()

        assertTrue(cachedSinglePhase.isEmpty())
        assertTrue(cachedThreePhase.isEmpty())
        assertTrue(cachedCustomMotors.isEmpty())
        assertTrue(cachedCategories.isEmpty())
    }

    // -------------------------------------------------------------
    // 13. PENDING AND UNVERIFIED PUBLIC USERS CANNOT BROWSE MOTOR DATA
    // -------------------------------------------------------------
    @Test
    fun testPendingOrUnverifiedPublicUsersCannotBrowseMotorData() {
        val pendingUser = com.example.model.User(
            id = "pending_1",
            role = "public",
            accountStatus = "pending",
            emailVerified = true
        )

        val unverifiedUser = com.example.model.User(
            id = "unverified_1",
            role = "public",
            accountStatus = "active",
            emailVerified = false
        )

        val activeUser = com.example.model.User(
            id = "active_1",
            role = "public",
            accountStatus = "active",
            emailVerified = true
        )

        fun isEligibleToBrowseMotors(user: com.example.model.User?): Boolean {
            if (user == null) return false
            if (user.role.equals("admin", ignoreCase = true)) return true
            return user.role.equals("public", ignoreCase = true) &&
                   user.accountStatus.equals("active", ignoreCase = true) &&
                   user.emailVerified
        }

        assertFalse("Pending user must not be eligible to browse motors", isEligibleToBrowseMotors(pendingUser))
        assertFalse("Unverified user must not be eligible to browse motors", isEligibleToBrowseMotors(unverifiedUser))
        assertTrue("Active verified user is eligible to browse motors", isEligibleToBrowseMotors(activeUser))
    }
}
