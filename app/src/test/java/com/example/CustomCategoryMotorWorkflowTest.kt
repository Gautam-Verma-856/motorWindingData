package com.example

import com.example.model.CustomCategory
import com.example.model.CustomCategoryField
import com.example.model.CustomMotor
import com.example.model.User
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CustomCategoryMotorWorkflowTest {

    // --- Helper function matching public browsing filter logic ---
    private fun filterCategoryMotorsForPublicBrowsing(
        motors: List<CustomMotor>,
        categoryId: String
    ): List<CustomMotor> {
        return motors.filter { it.categoryId == categoryId && it.isExplicitlyApproved() }
    }

    // --- Helper function matching user's "My Submissions" filter logic ---
    private fun filterUserSubmissions(
        motors: List<CustomMotor>,
        userId: String
    ): List<CustomMotor> {
        return motors.filter { it.createdBy == userId }
    }

    // --- Simulation of Add Custom Motor Permission & Status Decision ---
    sealed class SubmissionDecision {
        data class Allowed(val initialStatus: String, val motor: CustomMotor) : SubmissionDecision()
        data class Denied(val reason: String) : SubmissionDecision()
    }

    private fun evaluateCustomMotorSubmission(
        user: User?,
        category: CustomCategory,
        companyName: String,
        motorName: String,
        hp: String,
        photoUrls: List<String>,
        customFields: Map<String, String>
    ): SubmissionDecision {
        if (user == null) {
            return SubmissionDecision.Denied("Authentication required")
        }

        if (companyName.isBlank()) {
            return SubmissionDecision.Denied("Company Name is required")
        }

        // Validate required custom fields
        val missingField = category.fields.firstOrNull { it.isRequired && customFields[it.fieldName].isNullOrBlank() }
        if (missingField != null) {
            return SubmissionDecision.Denied("Required field missing: ${missingField.fieldName}")
        }

        val role = user.role.trim().lowercase()
        val isPublic = role == "public"

        if (isPublic) {
            if (!user.emailVerified) {
                return SubmissionDecision.Denied("Only active, email-verified accounts can submit motor data.")
            }
            if (user.accountStatus.trim().lowercase() != "active") {
                return SubmissionDecision.Denied("Only active, email-verified accounts can submit motor data.")
            }
            if (!category.allowPublicSubmissions) {
                return SubmissionDecision.Denied("Public submissions are not allowed for this category.")
            }
            val pendingMotor = CustomMotor(
                id = "cm_${System.currentTimeMillis()}",
                categoryId = category.id,
                categoryName = category.name,
                companyName = companyName.trim(),
                motorName = motorName.trim(),
                hp = hp.trim(),
                photoUrl = photoUrls.firstOrNull() ?: "",
                photoUrls = photoUrls,
                customFields = customFields,
                status = "pending",
                createdBy = user.id,
                createdByName = user.name
            )
            return SubmissionDecision.Allowed("pending", pendingMotor)
        } else if (role == "admin") {
            val approvedMotor = CustomMotor(
                id = "cm_${System.currentTimeMillis()}",
                categoryId = category.id,
                categoryName = category.name,
                companyName = companyName.trim(),
                motorName = motorName.trim(),
                hp = hp.trim(),
                photoUrl = photoUrls.firstOrNull() ?: "",
                photoUrls = photoUrls,
                customFields = customFields,
                status = "approved",
                createdBy = user.id,
                createdByName = user.name
            )
            return SubmissionDecision.Allowed("approved", approvedMotor)
        } else {
            return SubmissionDecision.Denied("Unauthorized role: $role")
        }
    }

    // -------------------------------------------------------------
    // 1. Custom Category Creation and Data Model Integrity
    // -------------------------------------------------------------
    @Test
    fun testCustomCategoryCreationAndDataModel() {
        val category = CustomCategory(
            id = "cat_cooler",
            name = "Cooler Motor",
            description = "High efficiency desert and room cooler winding data",
            iconName = "FlashOn",
            isActive = true,
            allowPublicSubmissions = true,
            fields = listOf(
                CustomCategoryField(id = "f1", fieldName = "RPM", fieldType = "number", isRequired = true, order = 1),
                CustomCategoryField(id = "f2", fieldName = "Capacitor", fieldType = "text", isRequired = true, order = 2),
                CustomCategoryField(id = "f3", fieldName = "Wire Gauge", fieldType = "text", isRequired = false, order = 3)
            )
        )

        assertEquals("cat_cooler", category.id)
        assertEquals("Cooler Motor", category.name)
        assertTrue(category.isActive)
        assertTrue(category.allowPublicSubmissions)
        assertEquals(3, category.fields.size)
        assertTrue(category.fields.any { it.fieldName == "RPM" && it.isRequired })
    }

    // -------------------------------------------------------------
    // 2. Admin Adding Motor Data -> Saved as "approved" & Immediately Visible
    // -------------------------------------------------------------
    @Test
    fun testAdminCanAddMotorDataToCustomCategory_IsDirectlyApproved() {
        val adminUser = User(id = "admin_01", name = "Admin Verma", role = "admin", accountStatus = "active")
        val category = CustomCategory(id = "cat_fan", name = "Exhaust Fan Motor", allowPublicSubmissions = true)

        val decision = evaluateCustomMotorSubmission(
            user = adminUser,
            category = category,
            companyName = "Almonard",
            motorName = "Heavy Duty 18 Inch",
            hp = "0.5 HP",
            photoUrls = listOf("https://i.ibb.co/fan1.jpg"),
            customFields = mapOf("Blade Size" to "450mm", "RPM" to "1440")
        )

        assertTrue(decision is SubmissionDecision.Allowed)
        val allowed = decision as SubmissionDecision.Allowed
        assertEquals("approved", allowed.initialStatus)
        assertEquals("cat_fan", allowed.motor.categoryId)
        assertEquals("Exhaust Fan Motor", allowed.motor.categoryName)
        assertEquals("Almonard", allowed.motor.companyName)
        assertEquals("https://i.ibb.co/fan1.jpg", allowed.motor.photoUrl)
        assertTrue(allowed.motor.isExplicitlyApproved())
        assertFalse(allowed.motor.isPending())

        // Check public browsing list
        val publicList = filterCategoryMotorsForPublicBrowsing(listOf(allowed.motor), "cat_fan")
        assertEquals(1, publicList.size)
        assertEquals("Almonard", publicList.first().companyName)
    }

    // -------------------------------------------------------------
    // 3. Active, Verified Public User Adding Motor Data -> Status is "pending"
    // -------------------------------------------------------------
    @Test
    fun testActiveVerifiedPublicUserCanAddMotorData_StatusIsPending() {
        val publicUser = User(
            id = "user_77",
            name = "Ramesh Kumar",
            role = "public",
            emailVerified = true,
            accountStatus = "active"
        )
        val category = CustomCategory(id = "cat_cooler", name = "Cooler Motor", allowPublicSubmissions = true)

        val decision = evaluateCustomMotorSubmission(
            user = publicUser,
            category = category,
            companyName = "Usha",
            motorName = "Striker Cooler",
            hp = "110W",
            photoUrls = listOf("https://i.ibb.co/cooler1.jpg"),
            customFields = mapOf("Speed" to "3 Speed")
        )

        assertTrue(decision is SubmissionDecision.Allowed)
        val allowed = decision as SubmissionDecision.Allowed
        assertEquals("pending", allowed.initialStatus)
        assertEquals("pending", allowed.motor.status)
        assertEquals("user_77", allowed.motor.createdBy)
        assertEquals("Ramesh Kumar", allowed.motor.createdByName)
        assertTrue(allowed.motor.isPending())
        assertFalse(allowed.motor.isExplicitlyApproved())

        // Public browsing must NOT include this record yet
        val publicList = filterCategoryMotorsForPublicBrowsing(listOf(allowed.motor), "cat_cooler")
        assertTrue("Pending public submission must not be visible in public category list", publicList.isEmpty())
    }

    // -------------------------------------------------------------
    // 4. Public Submission Appears in "My Submissions"
    // -------------------------------------------------------------
    @Test
    fun testPublicSubmissionShowsInUserMySubmissions() {
        val publicUser = User(id = "user_77", name = "Ramesh Kumar", role = "public", emailVerified = true, accountStatus = "active")
        val otherUser = User(id = "user_99", name = "Suresh", role = "public", emailVerified = true, accountStatus = "active")

        val motor1 = CustomMotor(id = "m1", categoryId = "cat_cooler", companyName = "Usha", status = "pending", createdBy = "user_77")
        val motor2 = CustomMotor(id = "m2", categoryId = "cat_fan", companyName = "Bajaj", status = "approved", createdBy = "user_77")
        val motorOther = CustomMotor(id = "m3", categoryId = "cat_cooler", companyName = "Crompton", status = "pending", createdBy = "user_99")

        val allMotors = listOf(motor1, motor2, motorOther)

        val rameshSubmissions = filterUserSubmissions(allMotors, publicUser.id)
        assertEquals(2, rameshSubmissions.size)
        assertTrue(rameshSubmissions.any { it.id == "m1" && it.isPending() })
        assertTrue(rameshSubmissions.any { it.id == "m2" && it.isExplicitlyApproved() })
        assertFalse(rameshSubmissions.any { it.id == "m3" })
    }

    // -------------------------------------------------------------
    // 5. Unverified or Inactive Public Users Blocked from Submitting
    // -------------------------------------------------------------
    @Test
    fun testUnverifiedOrPendingPublicUser_BlockedFromSubmitting() {
        val unverifiedUser = User(id = "u1", role = "public", emailVerified = false, accountStatus = "active")
        val pendingUser = User(id = "u2", role = "public", emailVerified = true, accountStatus = "pending")
        val suspendedUser = User(id = "u3", role = "public", emailVerified = true, accountStatus = "suspended")
        val category = CustomCategory(id = "cat_1", name = "Farata Motor", allowPublicSubmissions = true)

        val res1 = evaluateCustomMotorSubmission(unverifiedUser, category, "Usha", "Model X", "1 HP", emptyList(), emptyMap())
        assertTrue(res1 is SubmissionDecision.Denied)
        assertTrue((res1 as SubmissionDecision.Denied).reason.contains("email-verified"))

        val res2 = evaluateCustomMotorSubmission(pendingUser, category, "Usha", "Model X", "1 HP", emptyList(), emptyMap())
        assertTrue(res2 is SubmissionDecision.Denied)
        assertTrue((res2 as SubmissionDecision.Denied).reason.contains("active"))

        val res3 = evaluateCustomMotorSubmission(suspendedUser, category, "Usha", "Model X", "1 HP", emptyList(), emptyMap())
        assertTrue(res3 is SubmissionDecision.Denied)
        assertTrue((res3 as SubmissionDecision.Denied).reason.contains("active"))
    }

    // -------------------------------------------------------------
    // 6. Submissions Blocked When Category Disallows Public Submissions
    // -------------------------------------------------------------
    @Test
    fun testCategoryDisallowingPublicSubmissions_BlocksPublicUsers() {
        val publicUser = User(id = "u_active", role = "public", emailVerified = true, accountStatus = "active")
        val privateCategory = CustomCategory(id = "cat_internal", name = "Experimental Motors", allowPublicSubmissions = false)

        val decision = evaluateCustomMotorSubmission(publicUser, privateCategory, "CustomCo", "Proto 1", "2 HP", emptyList(), emptyMap())
        assertTrue(decision is SubmissionDecision.Denied)
        assertTrue((decision as SubmissionDecision.Denied).reason.contains("Public submissions are not allowed"))

        // Admin can still submit even if allowPublicSubmissions is false
        val adminUser = User(id = "admin_1", role = "admin", accountStatus = "active")
        val adminDecision = evaluateCustomMotorSubmission(adminUser, privateCategory, "CustomCo", "Proto 1", "2 HP", emptyList(), emptyMap())
        assertTrue(adminDecision is SubmissionDecision.Allowed)
        assertEquals("approved", (adminDecision as SubmissionDecision.Allowed).initialStatus)
    }

    // -------------------------------------------------------------
    // 7. Admin Approval Workflow for Custom Category Motor
    // -------------------------------------------------------------
    @Test
    fun testAdminApprovesCustomMotor_BecomesPubliclyVisible() {
        val pendingMotor = CustomMotor(
            id = "cm_pending_1",
            categoryId = "cat_cooler",
            companyName = "Symphony",
            status = "pending",
            createdBy = "user_10"
        )

        assertFalse(pendingMotor.isExplicitlyApproved())

        // Simulate admin approval
        val approvedMotor = pendingMotor.copy(
            status = "approved",
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = "admin_master",
            reviewedByName = "Super Admin"
        )

        assertTrue(approvedMotor.isExplicitlyApproved())
        assertFalse(approvedMotor.isPending())
        assertEquals("admin_master", approvedMotor.reviewedBy)

        val publicList = filterCategoryMotorsForPublicBrowsing(listOf(approvedMotor), "cat_cooler")
        assertEquals(1, publicList.size)
        assertEquals("Symphony", publicList.first().companyName)
    }

    // -------------------------------------------------------------
    // 8. Admin Rejection Workflow with Mandatory Reason
    // -------------------------------------------------------------
    @Test
    fun testAdminRejectsCustomMotor_WithMandatoryReason() {
        val pendingMotor = CustomMotor(
            id = "cm_pending_2",
            categoryId = "cat_cooler",
            companyName = "Unknown Brand",
            status = "pending",
            createdBy = "user_10"
        )

        val rejectionReason = "Winding pitch data is incomplete. Please specify running coil pitch."
        val rejectedMotor = pendingMotor.copy(
            status = "rejected",
            rejectionReason = rejectionReason,
            reviewedAt = System.currentTimeMillis(),
            reviewedBy = "admin_01",
            reviewedByName = "Admin Verma"
        )

        assertTrue(rejectedMotor.isRejected())
        assertFalse(rejectedMotor.isExplicitlyApproved())
        assertEquals(rejectionReason, rejectedMotor.rejectionReason)
        assertEquals("Admin Verma", rejectedMotor.reviewedByName)

        // Must not be visible in public category browsing
        val publicList = filterCategoryMotorsForPublicBrowsing(listOf(rejectedMotor), "cat_cooler")
        assertTrue(publicList.isEmpty())

        // But must show in submitter's "My Submissions" with rejection reason
        val userSubmissions = filterUserSubmissions(listOf(rejectedMotor), "user_10")
        assertEquals(1, userSubmissions.size)
        assertEquals(rejectionReason, userSubmissions.first().rejectionReason)
    }

    // -------------------------------------------------------------
    // 9. Photo Upload URL Parsing & Assignment (No Firebase Storage)
    // -------------------------------------------------------------
    @Test
    fun testPhotoUploadResultHandlingAndUrlsSaving() {
        val mockImgBBResponse = """
            {
                "data": {
                    "id": "abc123xyz",
                    "title": "cooler_winding",
                    "url_viewer": "https://ibb.co/abc123xyz",
                    "url": "https://i.ibb.co/7XyZ/cooler_winding.jpg",
                    "display_url": "https://i.ibb.co/7XyZ/cooler_winding.jpg"
                },
                "success": true,
                "status": 200
            }
        """.trimIndent()

        val jsonObject = JSONObject(mockImgBBResponse)
        val dataObj = jsonObject.optJSONObject("data")
        val extractedUrl = dataObj?.optString("url") ?: ""
        assertEquals("https://i.ibb.co/7XyZ/cooler_winding.jpg", extractedUrl)

        // Multiple photo uploads assigned to CustomMotor
        val uploadedUrls = listOf(
            extractedUrl,
            "https://i.ibb.co/8AbC/cooler_connection.jpg"
        )

        val motor = CustomMotor(
            id = "cm_photos",
            categoryId = "cat_cooler",
            companyName = "Kenstar",
            photoUrl = uploadedUrls.first(),
            photoUrls = uploadedUrls
        )

        assertEquals("https://i.ibb.co/7XyZ/cooler_winding.jpg", motor.photoUrl)
        assertEquals(2, motor.photoUrls.size)
        assertEquals("https://i.ibb.co/8AbC/cooler_connection.jpg", motor.photoUrls[1])
    }

    // -------------------------------------------------------------
    // 10. Motor Records Strictly Bound to Correct Category
    // -------------------------------------------------------------
    @Test
    fun testMotorsStrictlyBoundToCorrectCategoryId() {
        val coolerMotor = CustomMotor(id = "m_cooler", categoryId = "cat_cooler", companyName = "Usha", status = "approved")
        val fanMotor = CustomMotor(id = "m_fan", categoryId = "cat_fan", companyName = "Almonard", status = "approved")
        val farataMotor = CustomMotor(id = "m_farata", categoryId = "cat_farata", companyName = "Orient", status = "approved")

        val allMotors = listOf(coolerMotor, fanMotor, farataMotor)

        val coolerList = filterCategoryMotorsForPublicBrowsing(allMotors, "cat_cooler")
        assertEquals(1, coolerList.size)
        assertEquals("m_cooler", coolerList.first().id)

        val fanList = filterCategoryMotorsForPublicBrowsing(allMotors, "cat_fan")
        assertEquals(1, fanList.size)
        assertEquals("m_fan", fanList.first().id)

        val farataList = filterCategoryMotorsForPublicBrowsing(allMotors, "cat_farata")
        assertEquals(1, farataList.size)
        assertEquals("m_farata", farataList.first().id)
    }

    // -------------------------------------------------------------
    // 11. UI `canAddData` Gating for Public vs Admin
    // -------------------------------------------------------------
    private fun computeCanAddData(user: User?, category: CustomCategory): Boolean {
        val role = user?.role?.trim() ?: "public"
        val isAdmin = role.equals("admin", ignoreCase = true)
        val isEmailVerified = user?.emailVerified == true
        val isAccountActive = user?.accountStatus?.trim()?.equals("active", ignoreCase = true) == true
        val isPublicActiveAndVerified = user != null &&
                role.equals("public", ignoreCase = true) &&
                isEmailVerified &&
                isAccountActive
        return isAdmin || (isPublicActiveAndVerified && category.allowPublicSubmissions)
    }

    @Test
    fun testCanAddDataGating() {
        val enabledCat = CustomCategory(id = "c1", allowPublicSubmissions = true)
        val disabledCat = CustomCategory(id = "c2", allowPublicSubmissions = false)

        val activeVerifiedPublic = User(id = "u1", role = "public", emailVerified = true, accountStatus = "active")
        val unverifiedPublic = User(id = "u2", role = "public", emailVerified = false, accountStatus = "active")
        val pendingPublic = User(id = "u3", role = "public", emailVerified = true, accountStatus = "pending")
        val adminUser = User(id = "a1", role = "admin", accountStatus = "active")

        // 1. Active verified public gets Add Data in enabled category
        assertTrue("Active verified public must have canAddData in enabled category", computeCanAddData(activeVerifiedPublic, enabledCat))

        // 2. Public user cannot add in disabled category
        assertFalse("Public user cannot add data in disabled-submission category", computeCanAddData(activeVerifiedPublic, disabledCat))

        // 3. Unverified or pending public users cannot add data
        assertFalse("Unverified public user cannot add data", computeCanAddData(unverifiedPublic, enabledCat))
        assertFalse("Pending public user cannot add data", computeCanAddData(pendingPublic, enabledCat))

        // 4. Admin always has canAddData in any category
        assertTrue("Admin can add data in enabled category", computeCanAddData(adminUser, enabledCat))
        assertTrue("Admin can add data even in disabled-submission category", computeCanAddData(adminUser, disabledCat))
    }

    // -------------------------------------------------------------
    // 12. Security Gating: Public User Cannot Modify Status or Other's Submissions
    // -------------------------------------------------------------
    @Test
    fun testPublicUserCannotApproveOrModifyOthersSubmissions() {
        val submitterUser = User(id = "sub_01", role = "public", emailVerified = true, accountStatus = "active")
        val attackerUser = User(id = "att_02", role = "public", emailVerified = true, accountStatus = "active")
        val adminUser = User(id = "adm_01", role = "admin", accountStatus = "active")

        val motor = CustomMotor(
            id = "cm_sec_1",
            categoryId = "cat_1",
            companyName = "Test Co",
            status = "pending",
            createdBy = "sub_01"
        )

        // Function checking if a user is permitted to change approval status
        fun canChangeApprovalStatus(user: User?): Boolean {
            return user != null && user.role.trim().equals("admin", ignoreCase = true)
        }

        // Function checking if a user is permitted to edit a motor
        fun canEditMotor(user: User?, motor: CustomMotor): Boolean {
            if (user == null) return false
            val role = user.role.trim().lowercase()
            if (role == "admin") return true
            // Public users can never edit once submitted (read-only for public)
            return false
        }

        // Public users cannot change status
        assertFalse("Public user cannot approve motor", canChangeApprovalStatus(submitterUser))
        assertFalse("Another public user cannot approve motor", canChangeApprovalStatus(attackerUser))
        assertTrue("Admin can approve motor", canChangeApprovalStatus(adminUser))

        // Public users cannot edit motor records
        assertFalse("Submitter public user cannot edit submitted record", canEditMotor(submitterUser, motor))
        assertFalse("Other public user cannot edit someone else's record", canEditMotor(attackerUser, motor))
        assertTrue("Admin can edit record", canEditMotor(adminUser, motor))
    }

    // -------------------------------------------------------------
    // 13. Validation: Missing/Invalid CategoryId or Unauthorized Role Fails Save
    // -------------------------------------------------------------
    @Test
    fun testMissingCategoryIdOrUnauthorizedRoleFailsSave() {
        val publicUser = User(id = "u1", role = "public", emailVerified = true, accountStatus = "active")
        val blankCategory = CustomCategory(id = "", name = "")
        val invalidRoleUser = User(id = "u_bad", role = "guest", accountStatus = "active")
        val validCat = CustomCategory(id = "cat_test", name = "Test Cat")

        // 1. Missing categoryId
        fun validateSaveCustomMotor(user: User?, catId: String, companyName: String): Result<Unit> {
            if (user == null) return Result.failure(Exception("Authentication required"))
            val role = user.role.trim().lowercase()
            if (role != "admin" && role != "public") return Result.failure(Exception("Unauthorized role: $role"))
            if (catId.trim().isEmpty()) return Result.failure(Exception("Category ID cannot be blank"))
            if (companyName.trim().isEmpty()) return Result.failure(Exception("Company Name is required"))
            return Result.success(Unit)
        }

        assertTrue("Blank categoryId must fail validation", validateSaveCustomMotor(publicUser, "", "Usha").isFailure)
        assertTrue("Blank companyName must fail validation", validateSaveCustomMotor(publicUser, "cat_1", "").isFailure)
        assertTrue("Invalid role must fail validation", validateSaveCustomMotor(invalidRoleUser, "cat_1", "Usha").isFailure)
        assertTrue("Valid admin submission must succeed validation", validateSaveCustomMotor(User(id = "a1", role = "admin"), "cat_1", "Usha").isSuccess)
    }

    // -------------------------------------------------------------
    // 14. Cache Refresh and Optimistic Update Simulation
    // -------------------------------------------------------------
    @Test
    fun testCacheRefreshAndOptimisticUpdate_AvoidsStaleData() {
        var currentCache: List<CustomMotor> = listOf(
            CustomMotor(id = "m1", categoryId = "cat_cooler", companyName = "Existing Motor", status = "approved")
        )

        // 1. Admin adds a new custom motor -> optimistically updates cache immediately
        val newAdminMotor = CustomMotor(id = "m2", categoryId = "cat_cooler", companyName = "New Admin Motor", status = "approved")
        currentCache = listOf(newAdminMotor) + currentCache

        assertEquals(2, currentCache.size)
        assertEquals("m2", currentCache.first().id)
        val publicListAfterAdd = filterCategoryMotorsForPublicBrowsing(currentCache, "cat_cooler")
        assertEquals(2, publicListAfterAdd.size)

        // 2. Admin approves a pending motor -> status updated optimistically in cache
        val pendingMotor = CustomMotor(id = "m3", categoryId = "cat_cooler", companyName = "Pending Motor", status = "pending")
        currentCache = currentCache + listOf(pendingMotor)

        // Before approval, pending is not in public list
        assertEquals(2, filterCategoryMotorsForPublicBrowsing(currentCache, "cat_cooler").size)

        // Optimistic approval update
        currentCache = currentCache.map { if (it.id == "m3") it.copy(status = "approved") else it }

        // After approval, immediately visible in public list without stale cache delay
        val publicListAfterApprove = filterCategoryMotorsForPublicBrowsing(currentCache, "cat_cooler")
        assertEquals(3, publicListAfterApprove.size)
        assertTrue(publicListAfterApprove.any { it.id == "m3" && it.isExplicitlyApproved() })
    }

    // -------------------------------------------------------------
    // 15. Security Rule Simulation: Public Create Requires Real Existing, Active, Enabled Category
    // -------------------------------------------------------------
    @Test
    fun testFirestoreRule_PublicCreateRequiresExistingActiveAndEnabledCategory() {
        data class FirestoreCategoryDoc(
            val exists: Boolean,
            val isActive: Boolean?,
            val allowPublicSubmissions: Boolean?
        )

        fun canPublicUserCreateMotorInCategory(
            user: User?,
            categoryId: String,
            categoryDoc: FirestoreCategoryDoc?,
            status: String,
            createdBy: String
        ): Boolean {
            if (user == null) return false
            val isPublic = user.role.trim().equals("public", ignoreCase = true)
            val isVerified = user.emailVerified
            val isActive = user.accountStatus.trim().equals("active", ignoreCase = true)
            if (!isPublic || !isVerified || !isActive) return false

            if (status != "pending" || createdBy != user.id || categoryId.isBlank()) return false
            if (categoryDoc == null || !categoryDoc.exists) return false
            if (categoryDoc.isActive == null || categoryDoc.isActive != true) return false
            if (categoryDoc.allowPublicSubmissions == null || categoryDoc.allowPublicSubmissions != true) return false

            return true
        }

        val publicUser = User(id = "u_verified", role = "public", emailVerified = true, accountStatus = "active")

        // 1. Missing category doc -> Denied
        assertFalse(
            "Missing category doc must deny public create",
            canPublicUserCreateMotorInCategory(publicUser, "cat_unknown", null, "pending", "u_verified")
        )
        assertFalse(
            "Non-existent category doc must deny public create",
            canPublicUserCreateMotorInCategory(
                publicUser,
                "cat_404",
                FirestoreCategoryDoc(exists = false, isActive = true, allowPublicSubmissions = true),
                "pending",
                "u_verified"
            )
        )

        // 2. Missing isActive field or isActive == false -> Denied
        assertFalse(
            "Category missing isActive field must deny public create",
            canPublicUserCreateMotorInCategory(
                publicUser,
                "cat_1",
                FirestoreCategoryDoc(exists = true, isActive = null, allowPublicSubmissions = true),
                "pending",
                "u_verified"
            )
        )
        assertFalse(
            "Category with isActive == false must deny public create",
            canPublicUserCreateMotorInCategory(
                publicUser,
                "cat_1",
                FirestoreCategoryDoc(exists = true, isActive = false, allowPublicSubmissions = true),
                "pending",
                "u_verified"
            )
        )

        // 3. Missing allowPublicSubmissions field or allowPublicSubmissions == false -> Denied
        assertFalse(
            "Category missing allowPublicSubmissions field must deny public create",
            canPublicUserCreateMotorInCategory(
                publicUser,
                "cat_1",
                FirestoreCategoryDoc(exists = true, isActive = true, allowPublicSubmissions = null),
                "pending",
                "u_verified"
            )
        )
        assertFalse(
            "Category with allowPublicSubmissions == false must deny public create",
            canPublicUserCreateMotorInCategory(
                publicUser,
                "cat_1",
                FirestoreCategoryDoc(exists = true, isActive = true, allowPublicSubmissions = false),
                "pending",
                "u_verified"
            )
        )

        // 4. Valid existing, active, and allowPublicSubmissions == true category -> Allowed
        assertTrue(
            "Valid active category with allowPublicSubmissions == true must allow public create",
            canPublicUserCreateMotorInCategory(
                publicUser,
                "cat_1",
                FirestoreCategoryDoc(exists = true, isActive = true, allowPublicSubmissions = true),
                "pending",
                "u_verified"
            )
        )
    }

    // -------------------------------------------------------------
    // 16. Security Rule Simulation: Public Read Access Restricted to Active+Verified Users
    // -------------------------------------------------------------
    @Test
    fun testFirestoreRule_PublicReadRestrictedToActiveVerifiedUsers() {
        fun canReadApprovedMotorsAndCategories(user: User?): Boolean {
            if (user == null) return false
            val role = user.role.trim().lowercase()
            if (role == "admin") {
                val status = user.accountStatus.trim().lowercase()
                return status != "blocked" && status != "suspended"
            }
            if (role == "public") {
                return user.emailVerified && user.accountStatus.trim().lowercase() == "active"
            }
            return false
        }

        val unauthenticated: User? = null
        val unverifiedPublic = User(id = "u1", role = "public", emailVerified = false, accountStatus = "active")
        val suspendedPublic = User(id = "u2", role = "public", emailVerified = true, accountStatus = "suspended")
        val pendingPublic = User(id = "u3", role = "public", emailVerified = true, accountStatus = "pending")
        val activeVerifiedPublic = User(id = "u4", role = "public", emailVerified = true, accountStatus = "active")
        val admin = User(id = "a1", role = "admin", accountStatus = "active")
        val blockedAdmin = User(id = "a2", role = "admin", accountStatus = "blocked")

        assertFalse("Unauthenticated cannot read", canReadApprovedMotorsAndCategories(unauthenticated))
        assertFalse("Unverified public cannot read", canReadApprovedMotorsAndCategories(unverifiedPublic))
        assertFalse("Suspended public cannot read", canReadApprovedMotorsAndCategories(suspendedPublic))
        assertFalse("Pending public cannot read", canReadApprovedMotorsAndCategories(pendingPublic))
        assertTrue("Active verified public can read", canReadApprovedMotorsAndCategories(activeVerifiedPublic))
        assertTrue("Active admin can read", canReadApprovedMotorsAndCategories(admin))
        assertFalse("Blocked admin cannot read", canReadApprovedMotorsAndCategories(blockedAdmin))
    }

    // -------------------------------------------------------------
    // 17. Security Rule Simulation: Pending/Rejected Records Isolated to Admin & Submitter
    // -------------------------------------------------------------
    @Test
    fun testFirestoreRule_PendingAndRejectedMotorsIsolatedToAdminAndSubmitter() {
        fun canReadSpecificMotorDoc(user: User?, motor: CustomMotor): Boolean {
            if (user == null) return false
            val role = user.role.trim().lowercase()
            val isAdmin = role == "admin" && user.accountStatus.trim().lowercase() != "blocked"
            if (isAdmin) return true

            val isPublicActiveVerified = role == "public" &&
                    user.emailVerified &&
                    user.accountStatus.trim().lowercase() == "active"

            if (isPublicActiveVerified) {
                // Public user can read if it's approved OR if they are the submitter (createdBy)
                return motor.isExplicitlyApproved() || motor.createdBy == user.id
            }

            return false
        }

        val userA = User(id = "user_A", role = "public", emailVerified = true, accountStatus = "active")
        val userB = User(id = "user_B", role = "public", emailVerified = true, accountStatus = "active")
        val admin = User(id = "admin_1", role = "admin", accountStatus = "active")

        val pendingByA = CustomMotor(id = "m_pending", categoryId = "c1", status = "pending", createdBy = "user_A")
        val rejectedByA = CustomMotor(id = "m_rejected", categoryId = "c1", status = "rejected", createdBy = "user_A")
        val approvedByA = CustomMotor(id = "m_approved", categoryId = "c1", status = "approved", createdBy = "user_A")

        // Submitter A can read their own pending and rejected records
        assertTrue("Submitter User A can read their own pending motor", canReadSpecificMotorDoc(userA, pendingByA))
        assertTrue("Submitter User A can read their own rejected motor", canReadSpecificMotorDoc(userA, rejectedByA))
        assertTrue("Submitter User A can read their own approved motor", canReadSpecificMotorDoc(userA, approvedByA))

        // Another public user B CANNOT read A's pending or rejected records
        assertFalse("Other public User B cannot read User A's pending motor", canReadSpecificMotorDoc(userB, pendingByA))
        assertFalse("Other public User B cannot read User A's rejected motor", canReadSpecificMotorDoc(userB, rejectedByA))
        assertTrue("Other public User B can read approved motor", canReadSpecificMotorDoc(userB, approvedByA))

        // Admin can read all records
        assertTrue("Admin can read pending motor", canReadSpecificMotorDoc(admin, pendingByA))
        assertTrue("Admin can read rejected motor", canReadSpecificMotorDoc(admin, rejectedByA))
        assertTrue("Admin can read approved motor", canReadSpecificMotorDoc(admin, approvedByA))
    }

    // -------------------------------------------------------------
    // 18. Security Rule Simulation: Admin Status = Approved, Public Status = Pending
    // -------------------------------------------------------------
    @Test
    fun testFirestoreRule_StatusValidationOnCreate() {
        fun validateCreateRequest(user: User?, categoryId: String, status: String, createdBy: String): Boolean {
            if (user == null) return false
            val role = user.role.trim().lowercase()
            val isAdmin = role == "admin" && user.accountStatus.trim().lowercase() != "blocked"
            if (isAdmin) {
                // Admin create must have approved status and non-blank categoryId
                return (status.equals("approved", ignoreCase = true)) && categoryId.isNotBlank()
            }

            val isPublicActiveVerified = role == "public" &&
                    user.emailVerified &&
                    user.accountStatus.trim().lowercase() == "active"

            if (isPublicActiveVerified) {
                // Public create must have pending status, matching createdBy UID, and non-blank categoryId
                return status == "pending" && createdBy == user.id && categoryId.isNotBlank()
            }

            return false
        }

        val publicUser = User(id = "u_pub", role = "public", emailVerified = true, accountStatus = "active")
        val adminUser = User(id = "u_adm", role = "admin", accountStatus = "active")

        // 1. Admin creating approved -> Allowed
        assertTrue("Admin creating approved status is allowed", validateCreateRequest(adminUser, "cat_1", "approved", "u_adm"))

        // 2. Admin creating with blank categoryId -> Denied
        assertFalse("Admin creating with blank categoryId is denied", validateCreateRequest(adminUser, "", "approved", "u_adm"))

        // 3. Public attempting to create directly with approved status -> Denied
        assertFalse("Public attempting approved status is denied", validateCreateRequest(publicUser, "cat_1", "approved", "u_pub"))

        // 4. Public attempting to spoof another user's UID -> Denied
        assertFalse("Public spoofing createdBy is denied", validateCreateRequest(publicUser, "cat_1", "pending", "someone_else"))

        // 5. Public creating with valid pending status and own UID -> Allowed
        assertTrue("Public creating pending status with own UID is allowed", validateCreateRequest(publicUser, "cat_1", "pending", "u_pub"))
    }
}
