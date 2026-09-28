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
}
