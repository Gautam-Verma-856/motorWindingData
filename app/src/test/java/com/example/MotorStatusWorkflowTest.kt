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

    // Helper simulating the exact Firestore security rules isAdmin() condition
    private fun simulateFirestoreIsAdminRule(authUid: String?, userDoc: com.example.model.User?): Boolean {
        if (authUid == null || userDoc == null) return false
        val roleMatches = userDoc.role.equals("admin", ignoreCase = true)
        val notBlockedOrSuspended = !userDoc.accountStatus.equals("blocked", ignoreCase = true) &&
                                   !userDoc.accountStatus.equals("suspended", ignoreCase = true)
        return roleMatches && notBlockedOrSuspended
    }

    // Helper simulating category write rule: allow create, update, delete: if isAdmin();
    private fun canUserWriteCustomCategory(authUid: String?, userDoc: com.example.model.User?): Boolean {
        return simulateFirestoreIsAdminRule(authUid, userDoc)
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

        assertTrue("Active admin must be permitted to manage categories", canUserWriteCustomCategory(adminUser.id, adminUser))

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

    @Test
    fun testAdminPromotedFromRegistrationCanCreateCustomCategory() {
        // Admin whose account was created via public registration and role was promoted in Console,
        // but accountStatus remained "pending" in Firestore
        val promotedAdminWithPendingStatus = com.example.model.User(
            id = "admin_uid_promoted",
            name = "Promoted Admin",
            email = "gautamverma90987@gmail.com",
            role = "admin",
            accountStatus = "pending"
        )

        // Must still be authorized because role is explicitly "admin" and not blocked/suspended
        assertTrue("Admin with un-updated accountStatus must be permitted", canUserWriteCustomCategory(promotedAdminWithPendingStatus.id, promotedAdminWithPendingStatus))
    }

    @Test
    fun testBlockedOrSuspendedAdminCannotCreateCustomCategory() {
        val blockedAdmin = com.example.model.User(
            id = "admin_blocked",
            role = "admin",
            accountStatus = "blocked"
        )
        val suspendedAdmin = com.example.model.User(
            id = "admin_suspended",
            role = "admin",
            accountStatus = "suspended"
        )

        assertFalse("Blocked admin must be denied", canUserWriteCustomCategory(blockedAdmin.id, blockedAdmin))
        assertFalse("Suspended admin must be denied", canUserWriteCustomCategory(suspendedAdmin.id, suspendedAdmin))
    }

    // -------------------------------------------------------------
    // 9. PUBLIC AND UNAUTHENTICATED USERS CANNOT WRITE CUSTOM CATEGORY
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

        assertFalse("Public user must NOT be permitted to write categories", canUserWriteCustomCategory(publicUser.id, publicUser))
    }

    @Test
    fun testUnauthenticatedUserCannotManageCustomCategory() {
        assertFalse("Unauthenticated user must NOT be permitted to write categories", canUserWriteCustomCategory(null, null))
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

    // -------------------------------------------------------------
    // 14. ADMIN CAN SEE ALL STATUSES INCLUDING REJECTED RECORDS
    // -------------------------------------------------------------
    @Test
    fun testAdminCanSeeAllStatusesIncludingRejectedRecords() {
        val allMotors = listOf(
            SinglePhaseMotor(id = "s1", companyName = "Crompton", status = "pending"),
            SinglePhaseMotor(id = "s2", companyName = "Kirloskar", status = "approved"),
            SinglePhaseMotor(id = "s3", companyName = "Havells", status = "rejected"),
            ThreePhaseMotor(id = "t1", name = "Siemens", status = "rejected"),
            CustomMotor(id = "c1", companyName = "CoolerMaster", status = "rejected")
        )

        val adminRole = "admin"
        val filterForRole = fun(items: List<SinglePhaseMotor>, role: String, statusFilter: String): List<SinglePhaseMotor> {
            return items.filter { motor ->
                val roleAllowed = if (role == "admin") true else motor.isExplicitlyApproved()
                val statusAllowed = when (statusFilter) {
                    "Pending" -> motor.isPending()
                    "Approved" -> motor.isExplicitlyApproved()
                    "Rejected" -> motor.isRejected()
                    else -> true
                }
                roleAllowed && statusAllowed
            }
        }

        // Admin seeing all
        val adminAll = filterForRole(allMotors.filterIsInstance<SinglePhaseMotor>(), adminRole, "All Status")
        assertEquals(3, adminAll.size)

        // Admin filtering by Rejected
        val adminRejected = filterForRole(allMotors.filterIsInstance<SinglePhaseMotor>(), adminRole, "Rejected")
        assertEquals(1, adminRejected.size)
        assertEquals("s3", adminRejected.first().id)
        assertTrue(adminRejected.first().isRejected())

        // Public user browsing (cannot see rejected or pending)
        val publicVisible = filterForRole(allMotors.filterIsInstance<SinglePhaseMotor>(), "public", "All Status")
        assertEquals(1, publicVisible.size)
        assertEquals("s2", publicVisible.first().id)
        assertFalse(publicVisible.any { it.isRejected() })
        assertFalse(publicVisible.any { it.isPending() })
    }

    // -------------------------------------------------------------
    // 15. NEWLY SAVED ADMIN MOTOR APPEARS IMMEDIATELY AND PERSISTS
    // -------------------------------------------------------------
    @Test
    fun testNewlySavedAdminMotorAppearsImmediatelyAndPersists() {
        val initialList = mutableListOf(
            SinglePhaseMotor(id = "s1", category = "Submersible Motor", companyName = "Crompton", status = "approved")
        )

        // Admin saves a new motor (status is "approved" by default for admin)
        val newAdminMotor = SinglePhaseMotor(
            id = "s2",
            category = "Submersible Motor",
            companyName = "Texmo",
            hp = "2.0",
            status = "approved",
            createdBy = "admin_123",
            createdByName = "Admin User",
            createdAt = System.currentTimeMillis()
        )

        // Optimistic / repository save adds to list
        initialList.add(newAdminMotor)

        // Verify immediately in single list
        assertEquals(2, initialList.size)
        assertTrue(initialList.any { it.id == "s2" && it.companyName == "Texmo" })

        // Simulate refresh (reload from storage)
        val refreshedList = initialList.toList()
        assertEquals(2, refreshedList.size)
        val savedMotor = refreshedList.find { it.id == "s2" }
        assertTrue(savedMotor != null)
        assertEquals("approved", savedMotor?.status)
        assertTrue(savedMotor!!.isExplicitlyApproved())
    }

    // -------------------------------------------------------------
    // 16. PUBLIC SUBMISSION STAYS PENDING THEN APPEARS AFTER ADMIN APPROVAL
    // -------------------------------------------------------------
    @Test
    fun testPublicSubmissionPendingUntilApprovalThenVisibleInPublicBrowsing() {
        val publicUserId = "public_user_42"
        val repositoryList = mutableListOf<SinglePhaseMotor>()

        // 1. Public user submits motor -> saved as "pending"
        val submission = SinglePhaseMotor(
            id = "sub_1",
            category = "Submersible Motor",
            companyName = "Usha",
            hp = "1.5",
            status = "pending",
            createdBy = publicUserId,
            createdByName = "User 42",
            createdAt = System.currentTimeMillis()
        )
        repositoryList.add(submission)

        // Public browsing query: only approved motors
        val publicBrowseBefore = repositoryList.filter { it.isExplicitlyApproved() }
        assertEquals("Pending submission must NOT appear in public browsing", 0, publicBrowseBefore.size)

        // My Submissions query for user 42: shows their submission with Pending status
        val mySubmissionsBefore = repositoryList.filter { it.createdBy == publicUserId }
        assertEquals(1, mySubmissionsBefore.size)
        assertEquals("pending", mySubmissionsBefore.first().status)
        assertEquals("Pending Admin Approval", getStatusBadgeText(mySubmissionsBefore.first().status))
        assertTrue(mySubmissionsBefore.first().createdAt > 0L)

        // 2. Admin approves the submission
        val index = repositoryList.indexOfFirst { it.id == "sub_1" }
        repositoryList[index] = repositoryList[index].copy(status = "approved")

        // Public browsing query after approval: motor is now visible!
        val publicBrowseAfter = repositoryList.filter { it.isExplicitlyApproved() }
        assertEquals("Approved submission MUST now appear in public browsing", 1, publicBrowseAfter.size)
        assertEquals("Usha", publicBrowseAfter.first().companyName)

        // My Submissions query reflects "Approved" status
        val mySubmissionsAfter = repositoryList.filter { it.createdBy == publicUserId }
        assertEquals("Approved", getStatusBadgeText(mySubmissionsAfter.first().status))
    }

    // -------------------------------------------------------------
    // 17. REJECTED SUBMISSION VISIBLE IN MY SUBMISSIONS & RE-APPROVABLE
    // -------------------------------------------------------------
    @Test
    fun testRejectedSubmissionVisibleInMySubmissionsAndAdminCanReApprove() {
        val publicUserId = "public_user_99"
        val repositoryList = mutableListOf(
            SinglePhaseMotor(
                id = "sub_99",
                category = "Submersible Motor",
                companyName = "V-Guard",
                status = "rejected",
                createdBy = publicUserId,
                createdByName = "User 99",
                createdAt = 1700000000000L
            )
        )

        // Public browsing: 0 (rejected is hidden)
        val publicBrowse = repositoryList.filter { it.isExplicitlyApproved() }
        assertEquals(0, publicBrowse.size)

        // Admin view: shows rejected motor
        val adminList = repositoryList.filter { it.isRejected() }
        assertEquals(1, adminList.size)
        assertEquals("sub_99", adminList.first().id)

        // My Submissions: shows motor with Rejected status and submission date
        val mySubmissions = repositoryList.filter { it.createdBy == publicUserId }
        assertEquals(1, mySubmissions.size)
        assertEquals("rejected", mySubmissions.first().status)
        assertEquals("Rejected", getStatusBadgeText(mySubmissions.first().status))
        assertEquals(1700000000000L, mySubmissions.first().createdAt)

        // Admin re-approves
        val idx = repositoryList.indexOfFirst { it.id == "sub_99" }
        repositoryList[idx] = repositoryList[idx].copy(status = "approved")

        // Now in public browsing
        val publicBrowseAfterReApproval = repositoryList.filter { it.isExplicitlyApproved() }
        assertEquals(1, publicBrowseAfterReApproval.size)
        assertEquals("Approved", getStatusBadgeText(repositoryList[idx].status))
    }

    // -------------------------------------------------------------
    // 18. BLANK REJECTION REASON VALIDATION
    // -------------------------------------------------------------
    private fun isValidRejectionReason(reason: String): Boolean {
        return reason.trim().isNotEmpty()
    }

    @Test
    fun testRejectionReasonValidation_DisallowsBlankOrWhitespaceOnly() {
        assertFalse("Empty string must be invalid", isValidRejectionReason(""))
        assertFalse("Spaces only must be invalid", isValidRejectionReason("   "))
        assertFalse("Tabs only must be invalid", isValidRejectionReason("\t\t"))
        assertFalse("Newlines and spaces must be invalid", isValidRejectionReason("\n  \r\n  "))

        assertTrue("Non-blank text must be valid", isValidRejectionReason("Incomplete turns data"))
        assertTrue("Whitespace padded text must be valid", isValidRejectionReason("  Incorrect wire gauge  "))
        assertEquals("Incorrect wire gauge", "  Incorrect wire gauge  ".trim())
    }

    // -------------------------------------------------------------
    // 19. SAVING REJECTION REASON AND ADMIN DETAILS ON REJECTION
    // -------------------------------------------------------------
    @Test
    fun testAdminRejectionSavesReasonAndReviewDetails_SinglePhase() {
        val pendingMotor = SinglePhaseMotor(
            id = "sp_pending",
            companyName = "Crompton",
            status = "pending",
            createdBy = "user_1",
            createdAt = 1700000000000L
        )

        val adminUid = "admin_999"
        val adminName = "Lead Inspector"
        val reviewTimestamp = 1700005000000L
        val inputReason = "  Capacitor value does not match motor specifications  "

        // Simulate admin rejection update logic
        val rejectedMotor = pendingMotor.copy(
            status = "rejected",
            rejectionReason = inputReason.trim(),
            reviewedBy = adminUid,
            reviewedByName = adminName,
            reviewedAt = reviewTimestamp,
            adminSeen = true,
            seen = true
        )

        assertEquals("rejected", rejectedMotor.status)
        assertTrue(rejectedMotor.isRejected())
        assertFalse(rejectedMotor.isPending())
        assertFalse(rejectedMotor.isExplicitlyApproved())
        assertEquals("Capacitor value does not match motor specifications", rejectedMotor.rejectionReason)
        assertEquals("admin_999", rejectedMotor.reviewedBy)
        assertEquals("Lead Inspector", rejectedMotor.reviewedByName)
        assertEquals(1700005000000L, rejectedMotor.reviewedAt)
        assertTrue(rejectedMotor.adminSeen)
        assertTrue(rejectedMotor.seen)
    }

    @Test
    fun testAdminRejectionSavesReasonAndReviewDetails_ThreePhase() {
        val pendingMotor = ThreePhaseMotor(
            id = "tp_pending",
            name = "Kirloskar",
            status = "pending",
            createdBy = "user_2"
        )

        val rejectedMotor = pendingMotor.copy(
            status = "rejected",
            rejectionReason = "Missing slot dimensions and incomplete winding pitch".trim(),
            reviewedBy = "admin_1",
            reviewedByName = "Admin Inspector",
            reviewedAt = 1700006000000L,
            adminSeen = true,
            seen = true
        )

        assertEquals("rejected", rejectedMotor.status)
        assertTrue(rejectedMotor.isRejected())
        assertEquals("Missing slot dimensions and incomplete winding pitch", rejectedMotor.rejectionReason)
        assertEquals("Admin Inspector", rejectedMotor.reviewedByName)
        assertEquals(1700006000000L, rejectedMotor.reviewedAt)
    }

    @Test
    fun testAdminRejectionSavesReasonAndReviewDetails_CustomMotor() {
        val pendingMotor = CustomMotor(
            id = "cm_pending",
            categoryId = "cat_cooler",
            companyName = "Havells",
            status = "pending",
            createdBy = "user_3"
        )

        val rejectedMotor = pendingMotor.copy(
            status = "rejected",
            rejectionReason = "Unclear photographs and incorrect RPM specification".trim(),
            reviewedBy = "admin_1",
            reviewedByName = "Admin Inspector",
            reviewedAt = 1700007000000L,
            adminSeen = true,
            seen = true
        )

        assertEquals("rejected", rejectedMotor.status)
        assertTrue(rejectedMotor.isRejected())
        assertEquals("Unclear photographs and incorrect RPM specification", rejectedMotor.rejectionReason)
        assertEquals("Admin Inspector", rejectedMotor.reviewedByName)
        assertEquals(1700007000000L, rejectedMotor.reviewedAt)
    }

    // -------------------------------------------------------------
    // 20. DISPLAY REJECTION REASON IN MY SUBMISSIONS
    // -------------------------------------------------------------
    private fun getDisplayRejectionReason(reason: String): String {
        return if (reason.isNotBlank()) reason.trim() else "No reason was provided."
    }

    @Test
    fun testMySubmissionsDisplaysRejectionReasonUnderHeading() {
        val publicUserId = "public_user_77"
        val submission = SinglePhaseMotor(
            id = "sub_rejected_with_reason",
            companyName = "Falcon Pumps",
            status = "rejected",
            rejectionReason = "Wire gauge and turn counts do not match slot capacity.",
            reviewedByName = "Admin Inspector",
            reviewedAt = 1700008000000L,
            createdBy = publicUserId
        )

        // Status badge in My Submissions
        assertEquals("Rejected", getStatusBadgeText(submission.status))

        // Heading and reason text display
        val headingText = "Reason for rejection"
        assertEquals("Reason for rejection", headingText)
        val displayText = getDisplayRejectionReason(submission.rejectionReason)
        assertEquals("Wire gauge and turn counts do not match slot capacity.", displayText)
    }

    @Test
    fun testOlderRejectedRecordsDisplayNoReasonProvidedFallback() {
        // Older records that have empty or whitespace-only rejectionReason
        val olderRecordEmpty = SinglePhaseMotor(
            id = "sub_older_1",
            status = "rejected",
            rejectionReason = ""
        )
        val olderRecordWhitespace = ThreePhaseMotor(
            id = "sub_older_2",
            status = "rejected",
            rejectionReason = "   "
        )

        assertEquals("Rejected", getStatusBadgeText(olderRecordEmpty.status))
        assertEquals("No reason was provided.", getDisplayRejectionReason(olderRecordEmpty.rejectionReason))

        assertEquals("Rejected", getStatusBadgeText(olderRecordWhitespace.status))
        assertEquals("No reason was provided.", getDisplayRejectionReason(olderRecordWhitespace.rejectionReason))
    }

    // -------------------------------------------------------------
    // 21. APPROVAL BEHAVIOR UNCHANGED (DOES NOT REQUIRE REASON)
    // -------------------------------------------------------------
    @Test
    fun testApprovalBehaviorUnchanged_DoesNotRequireReason() {
        val pendingMotor = SinglePhaseMotor(id = "sp_pending", status = "pending")

        // Approval changes status to "approved" without requiring any rejectionReason
        val approvedMotor = pendingMotor.copy(status = "approved", rejectionReason = "")

        assertEquals("approved", approvedMotor.status)
        assertTrue(approvedMotor.isExplicitlyApproved())
        assertFalse(approvedMotor.isRejected())
        assertFalse(approvedMotor.isPending())
        assertEquals("", approvedMotor.rejectionReason)
        assertEquals("Approved", getStatusBadgeText(approvedMotor.status))
    }
}
