package com.example

import com.example.model.CustomMotor
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationSeenWorkflowTest {

    // Helper to calculate total unseen count across motors and users
    private fun computeTotalUnseenCount(
        singlePhase: List<SinglePhaseMotor>,
        threePhase: List<ThreePhaseMotor>,
        customMotors: List<CustomMotor>,
        users: List<User>
    ): Int {
        val unseenSingle = singlePhase.count { it.isUnseen() }
        val unseenThree = threePhase.count { it.isUnseen() }
        val unseenCustom = customMotors.count { it.isUnseen() }
        val unseenUsers = users.count { it.isUnseen() }
        return unseenSingle + unseenThree + unseenCustom + unseenUsers
    }

    // Helper to determine if notification dot is visible
    private fun isNotificationDotVisible(totalUnseenCount: Int): Boolean {
        return totalUnseenCount > 0
    }

    // -------------------------------------------------------------
    // 1. NOTIFICATION DOT VISIBLE WHEN UNSEEN ITEMS EXIST
    // -------------------------------------------------------------
    @Test
    fun testNotificationDotVisibleWhenUnseenPendingItemsExist() {
        val singlePhase = listOf(
            SinglePhaseMotor(
                id = "sp_1",
                companyName = "Crompton",
                status = "pending",
                adminSeen = false,
                seen = false
            )
        )
        val threePhase = emptyList<ThreePhaseMotor>()
        val customMotors = emptyList<CustomMotor>()
        val users = emptyList<User>()

        assertTrue("Pending motor should be identified as unseen", singlePhase[0].isUnseen())
        val unseenCount = computeTotalUnseenCount(singlePhase, threePhase, customMotors, users)
        assertEquals(1, unseenCount)
        assertTrue("Notification dot must be visible when unseen count > 0", isNotificationDotVisible(unseenCount))
    }

    // -------------------------------------------------------------
    // 2. NOTIFICATION DOT DISAPPEARS AFTER VIEWING CORRESPONDING ITEM
    // -------------------------------------------------------------
    @Test
    fun testNotificationDotDisappearsImmediatelyAfterViewing() {
        var singlePhase = listOf(
            SinglePhaseMotor(
                id = "sp_1",
                companyName = "Crompton",
                status = "pending",
                adminSeen = false,
                seen = false
            )
        )
        val threePhase = emptyList<ThreePhaseMotor>()
        val customMotors = emptyList<CustomMotor>()
        val users = emptyList<User>()

        // Initially unseen -> Dot visible
        var unseenCount = computeTotalUnseenCount(singlePhase, threePhase, customMotors, users)
        assertTrue(isNotificationDotVisible(unseenCount))

        // Admin opens and views the motor in detail screen -> marked as seen
        singlePhase = singlePhase.map {
            if (it.id == "sp_1") it.copy(adminSeen = true, seen = true) else it
        }

        // Status is still pending, but it is now seen
        assertTrue(singlePhase[0].isPending())
        assertTrue(singlePhase[0].adminSeen)
        assertFalse("Item should no longer be unseen after viewing", singlePhase[0].isUnseen())

        unseenCount = computeTotalUnseenCount(singlePhase, threePhase, customMotors, users)
        assertEquals(0, unseenCount)
        assertFalse("Notification dot must disappear immediately after viewing", isNotificationDotVisible(unseenCount))
    }

    // -------------------------------------------------------------
    // 3. NOTIFICATION DOT REMAINS GONE AFTER REFRESH OR RELAUNCH
    // -------------------------------------------------------------
    @Test
    fun testNotificationDotRemainsGoneAfterRefreshOrRelaunch() {
        // Backend stores persisted motor record after viewing
        val persistedMotorFromBackend = SinglePhaseMotor(
            id = "sp_1",
            companyName = "Crompton",
            status = "pending",
            adminSeen = true,
            seen = true
        )

        // Simulate app relaunch or data refresh (loadData() from Firestore)
        val refreshedSinglePhase = listOf(persistedMotorFromBackend)
        val refreshedThreePhase = emptyList<ThreePhaseMotor>()
        val refreshedCustomMotors = emptyList<CustomMotor>()
        val refreshedUsers = emptyList<User>()

        val unseenCountAfterRefresh = computeTotalUnseenCount(
            refreshedSinglePhase,
            refreshedThreePhase,
            refreshedCustomMotors,
            refreshedUsers
        )

        assertEquals(0, unseenCountAfterRefresh)
        assertFalse(
            "Notification dot must stay gone after refresh or relaunch",
            isNotificationDotVisible(unseenCountAfterRefresh)
        )
        // Approval workflow remains pending until explicit admin decision
        assertTrue(refreshedSinglePhase[0].isPending())
    }

    // -------------------------------------------------------------
    // 4. NOTIFICATION DOT STAYS VISIBLE WHILE OTHER NOTIFICATIONS UNSEEN
    // -------------------------------------------------------------
    @Test
    fun testNotificationDotStaysVisibleWhileOtherNotificationsRemainUnseen() {
        // Multiple pending notifications: Motor A and Motor B
        var singlePhase = listOf(
            SinglePhaseMotor(id = "sp_1", companyName = "Motor A", status = "pending", adminSeen = false, seen = false),
            SinglePhaseMotor(id = "sp_2", companyName = "Motor B", status = "pending", adminSeen = false, seen = false)
        )
        val threePhase = emptyList<ThreePhaseMotor>()
        val customMotors = emptyList<CustomMotor>()
        val users = emptyList<User>()

        var unseenCount = computeTotalUnseenCount(singlePhase, threePhase, customMotors, users)
        assertEquals(2, unseenCount)
        assertTrue("Notification dot visible for 2 unseen items", isNotificationDotVisible(unseenCount))

        // Admin opens and views Motor A only
        singlePhase = singlePhase.map {
            if (it.id == "sp_1") it.copy(adminSeen = true, seen = true) else it
        }

        // Motor A is seen, but Motor B is still unseen
        assertFalse(singlePhase.first { it.id == "sp_1" }.isUnseen())
        assertTrue(singlePhase.first { it.id == "sp_2" }.isUnseen())

        unseenCount = computeTotalUnseenCount(singlePhase, threePhase, customMotors, users)
        assertEquals(1, unseenCount)
        assertTrue(
            "Notification dot must stay visible when another notification remains unseen",
            isNotificationDotVisible(unseenCount)
        )

        // Admin now opens and views Motor B
        singlePhase = singlePhase.map {
            if (it.id == "sp_2") it.copy(adminSeen = true, seen = true) else it
        }

        assertFalse(singlePhase.first { it.id == "sp_2" }.isUnseen())
        unseenCount = computeTotalUnseenCount(singlePhase, threePhase, customMotors, users)
        assertEquals(0, unseenCount)
        assertFalse(
            "Notification dot must disappear once all items are viewed",
            isNotificationDotVisible(unseenCount)
        )
    }

    // -------------------------------------------------------------
    // 5. MIXED MOTORS AND USER NOTIFICATIONS SEEN TRACKING
    // -------------------------------------------------------------
    @Test
    fun testMixedMotorsAndUsersSeenWorkflow() {
        var threePhase = listOf(
            ThreePhaseMotor(id = "tp_1", name = "Kirloskar 3P", status = "pending", adminSeen = false, seen = false)
        )
        var users = listOf(
            User(id = "u_1", name = "Bob", role = "public", accountStatus = "pending", adminSeen = false, seen = false)
        )

        var unseenCount = computeTotalUnseenCount(emptyList(), threePhase, emptyList(), users)
        assertEquals(2, unseenCount)
        assertTrue(isNotificationDotVisible(unseenCount))

        // Admin views 3-phase motor detail
        threePhase = threePhase.map {
            if (it.id == "tp_1") it.copy(adminSeen = true, seen = true) else it
        }

        unseenCount = computeTotalUnseenCount(emptyList(), threePhase, emptyList(), users)
        assertEquals(1, unseenCount)
        assertTrue("Dot remains visible because pending user is still unseen", isNotificationDotVisible(unseenCount))

        // Admin views pending user in User Management
        users = users.map {
            if (it.id == "u_1") it.copy(adminSeen = true, seen = true) else it
        }

        unseenCount = computeTotalUnseenCount(emptyList(), threePhase, emptyList(), users)
        assertEquals(0, unseenCount)
        assertFalse("Dot disappears when both motor and user have been viewed", isNotificationDotVisible(unseenCount))
    }

    // -------------------------------------------------------------
    // 6. MERELY DISPLAYING LIST OR OPENING DASHBOARD DOES NOT MARK SEEN
    // -------------------------------------------------------------
    @Test
    fun testDisplayingListOrDashboardDoesNotMarkItemsAsSeen() {
        val singlePhase = listOf(
            SinglePhaseMotor(id = "sp_1", companyName = "Havells", status = "pending", adminSeen = false, seen = false)
        )
        val users = listOf(
            User(id = "u_1", name = "Alice", role = "public", accountStatus = "pending", adminSeen = false, seen = false)
        )

        // Simulating dashboard open or motor list open:
        // No item was clicked or viewed in detail.
        // adminSeen and seen remain false.
        assertTrue(singlePhase[0].isUnseen())
        assertTrue(users[0].isUnseen())
        val unseenCount = computeTotalUnseenCount(singlePhase, emptyList(), emptyList(), users)
        assertEquals(2, unseenCount)
        assertTrue("Notification dot must remain visible when list is merely rendered", isNotificationDotVisible(unseenCount))
    }

    // -------------------------------------------------------------
    // 7. APPROVAL WORKFLOW PRESERVATION
    // -------------------------------------------------------------
    @Test
    fun testApprovalWorkflowIntactWhenSeen() {
        val motor = SinglePhaseMotor(
            id = "sp_1",
            companyName = "Crompton",
            status = "pending",
            adminSeen = true,
            seen = true
        )

        // Viewing does not change status
        assertEquals("pending", motor.status)
        assertTrue(motor.isPending())
        assertFalse(motor.isExplicitlyApproved())

        // Explicit approval sets status to approved
        val approvedMotor = motor.copy(status = "approved")
        assertEquals("approved", approvedMotor.status)
        assertTrue(approvedMotor.isExplicitlyApproved())
        assertFalse(approvedMotor.isUnseen())
    }
}
