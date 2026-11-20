package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.model.User
import com.example.model.UserAdminAction
import com.example.model.UserManagementEngine
import com.example.model.UserSortOption
import com.example.model.UserStatusFilter
import com.example.ui.screens.UserDetailScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserManagementWorkflowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val baseTime = 1_750_000_000_000L

    private fun adminUser() = User(
        id = "admin_1",
        name = "Super Admin",
        email = "admin@motorapp.com",
        role = "admin",
        accountStatus = "active",
        emailVerified = true
    )

    private fun sampleUsers(): List<User> = listOf(
        adminUser(),
        User(
            id = "usr_active_1",
            name = "Aarav Patel",
            email = "aarav@example.com",
            mobile = "+919876543210",
            role = "public",
            accountStatus = "active",
            createdAt = baseTime - 1_000L,
            emailVerified = true,
            phoneVerified = true,
            updatedAt = baseTime - 500L
        ),
        User(
            id = "usr_pending_2",
            name = "Bhavna Sharma",
            email = "bhavna@example.com",
            mobile = "+919111222333",
            role = "public",
            accountStatus = "pending",
            createdAt = baseTime - 5_000L,
            emailVerified = true,
            phoneVerified = false
        ),
        User(
            id = "usr_suspended_3",
            name = "Chetan Verma",
            email = "chetan@example.com",
            mobile = "",
            role = "public",
            accountStatus = "blocked",
            createdAt = baseTime - 10_000L,
            emailVerified = true,
            statusReason = "Duplicate spam submissions",
            statusUpdatedAt = baseTime - 2_000L
        ),
        User(
            id = "usr_rejected_4",
            name = "Deepak Joshi",
            email = "deepak@example.com",
            mobile = "+919444555666",
            role = "public",
            accountStatus = "rejected",
            createdAt = baseTime - 20_000L,
            emailVerified = false,
            statusReason = "Incomplete workshop details",
            statusUpdatedAt = baseTime - 3_000L
        )
    )

    private fun sampleSinglePhaseMotors(): List<SinglePhaseMotor> = listOf(
        SinglePhaseMotor(
            id = "sp_1",
            companyName = "Crompton 1HP",
            hp = "1 HP",
            status = "approved",
            createdBy = "usr_active_1",
            createdByName = "Aarav Patel"
        ),
        SinglePhaseMotor(
            id = "sp_2",
            companyName = "Havells 0.5HP",
            hp = "0.5 HP",
            status = "pending",
            createdBy = "usr_active_1",
            createdByName = "Aarav Patel"
        ),
        SinglePhaseMotor(
            id = "sp_3",
            companyName = "Legacy Cooler",
            hp = "0.25 HP",
            status = "rejected",
            createdBy = "",
            createdByName = "Bhavna Sharma"
        )
    )

    private fun sampleThreePhaseMotors(): List<ThreePhaseMotor> = listOf(
        ThreePhaseMotor(
            id = "tp_1",
            name = "Kirloskar 5HP",
            hp = "5 HP",
            status = "approved",
            createdBy = "usr_active_1",
            createdByName = "Aarav Patel"
        )
    )

    // -------------------------------------------------------------
    // 1. Domain Logic Tests (Counts, Search, Filter, Sort, Actions)
    // -------------------------------------------------------------
    @Test
    fun overviewStats_andSubmissionCounts_computeAccuratelyFromData() {
        val users = sampleUsers()
        val stats = UserManagementEngine.computeOverviewStats(users)

        assertEquals(4, stats.totalPublicUsers)
        assertEquals(1, stats.activeUsers)
        assertEquals(1, stats.pendingUsers)
        assertEquals(1, stats.suspendedUsers)
        assertEquals(1, stats.rejectedUsers)

        val aarav = users.first { it.id == "usr_active_1" }
        val aaravStats = UserManagementEngine.computeUserSubmissions(
            user = aarav,
            singlePhaseMotors = sampleSinglePhaseMotors(),
            threePhaseMotors = sampleThreePhaseMotors()
        )
        assertEquals(3, aaravStats.totalSubmissions)
        assertEquals(2, aaravStats.singlePhaseCount)
        assertEquals(1, aaravStats.threePhaseCount)
        assertEquals(2, aaravStats.approvedCount)
        assertEquals(1, aaravStats.pendingCount)
        assertEquals(0, aaravStats.rejectedCount)

        val bhavna = users.first { it.id == "usr_pending_2" }
        val bhavnaStats = UserManagementEngine.computeUserSubmissions(
            user = bhavna,
            singlePhaseMotors = sampleSinglePhaseMotors(),
            threePhaseMotors = sampleThreePhaseMotors()
        )
        assertEquals(1, bhavnaStats.totalSubmissions)
        assertEquals(1, bhavnaStats.rejectedCount)
    }

    @Test
    fun filterAndSortUsers_supportsNameEmailMobileUserIdAndSortOrder() {
        val users = sampleUsers()

        // Search by Name
        assertEquals(
            listOf("usr_active_1"),
            UserManagementEngine.filterAndSortUsers(users, "Aarav", UserStatusFilter.ALL, UserSortOption.NEWEST).map { it.id }
        )
        // Search by Email
        assertEquals(
            listOf("usr_pending_2"),
            UserManagementEngine.filterAndSortUsers(users, "bhavna@example.com", UserStatusFilter.ALL, UserSortOption.NEWEST).map { it.id }
        )
        // Search by Mobile
        assertEquals(
            listOf("usr_rejected_4"),
            UserManagementEngine.filterAndSortUsers(users, "9444555666", UserStatusFilter.ALL, UserSortOption.NEWEST).map { it.id }
        )
        // Search by User ID
        assertEquals(
            listOf("usr_suspended_3"),
            UserManagementEngine.filterAndSortUsers(users, "usr_suspended_3", UserStatusFilter.ALL, UserSortOption.NEWEST).map { it.id }
        )
        // Sort Newest vs Oldest
        assertEquals(
            listOf("usr_active_1", "usr_pending_2", "usr_suspended_3", "usr_rejected_4"),
            UserManagementEngine.filterAndSortUsers(users, "", UserStatusFilter.ALL, UserSortOption.NEWEST).map { it.id }
        )
        assertEquals(
            listOf("usr_rejected_4", "usr_suspended_3", "usr_pending_2", "usr_active_1"),
            UserManagementEngine.filterAndSortUsers(users, "", UserStatusFilter.ALL, UserSortOption.OLDEST).map { it.id }
        )
    }

    @Test
    fun allowedActionsForStatus_returnsOnlyStatusAppropriateActions() {
        assertEquals(listOf(UserAdminAction.APPROVE, UserAdminAction.REJECT), UserManagementEngine.allowedActionsForStatus("pending"))
        assertEquals(listOf(UserAdminAction.SUSPEND), UserManagementEngine.allowedActionsForStatus("active"))
        assertEquals(listOf(UserAdminAction.ACTIVATE), UserManagementEngine.allowedActionsForStatus("blocked"))
        assertEquals(listOf(UserAdminAction.ACTIVATE), UserManagementEngine.allowedActionsForStatus("suspended"))
        assertEquals(listOf(UserAdminAction.ACTIVATE), UserManagementEngine.allowedActionsForStatus("rejected"))
    }

    // -------------------------------------------------------------
    // 2. Compose UI: Overview, Search, Filters, Sort & Clear Filters
    // -------------------------------------------------------------
    @Test
    fun userManagementScreen_displaysOverviewAndSupportsSearchFilterSortClear() {
        val authVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(adminUser(), sampleUsers())
        }
        val motorVm = MotorViewModel().apply {
            seedMotorsForTest(sampleSinglePhaseMotors(), sampleThreePhaseMotors())
        }

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                val navController = rememberNavController()
                UserManagementScreen(
                    navController = navController,
                    authViewModel = authVm,
                    motorViewModel = motorVm
                )
            }
        }

        // Overview cards visible
        composeRule.onNodeWithTag("overview_card_total").assertIsDisplayed()
        composeRule.onNodeWithTag("overview_card_active").assertIsDisplayed()
        composeRule.onNodeWithTag("overview_card_pending").assertIsDisplayed()
        composeRule.onNodeWithTag("overview_card_suspended").assertIsDisplayed()

        // Submission badge on Aarav's card (3 Motors)
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_active_1"))
        composeRule.onNodeWithText("3 Motors").assertIsDisplayed()

        // Search by User ID
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_search_input"))
        composeRule.onNodeWithTag("user_search_input").performTextInput("usr_suspended_3")
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_suspended_3"))
        composeRule.onNodeWithTag("user_card_usr_suspended_3").assertIsDisplayed()
        composeRule.onAllNodesWithTag("user_card_usr_active_1").assertCountEquals(0)

        // Clear Filters restores all 4 public users
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_clear_filters_button"))
        composeRule.onNodeWithTag("user_clear_filters_button").performClick()
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_active_1"))
        composeRule.onNodeWithTag("user_card_usr_active_1").assertIsDisplayed()

        // Filter by Pending
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("filter_chip_pending"))
        composeRule.onNodeWithTag("filter_chip_pending").performClick()
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_pending_2"))
        composeRule.onNodeWithTag("user_card_usr_pending_2").assertIsDisplayed()
        composeRule.onAllNodesWithTag("user_card_usr_active_1").assertCountEquals(0)

        // Clear Filters again
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_clear_filters_button"))
        composeRule.onNodeWithTag("user_clear_filters_button").performClick()
        composeRule.onNodeWithTag("sort_chip_oldest").performClick()
        composeRule.onNodeWithTag("user_clear_filters_button").assertIsDisplayed()
    }

    // -------------------------------------------------------------
    // 3. Compose UI: Status Actions + Confirmation Dialog + Reason
    // -------------------------------------------------------------
    @Test
    fun userManagementScreen_showsStatusAppropriateActions_andConfirmsWithOptionalReason() {
        val authVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(adminUser(), sampleUsers())
        }
        val motorVm = MotorViewModel().apply {
            seedMotorsForTest(sampleSinglePhaseMotors(), sampleThreePhaseMotors())
        }

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                val navController = rememberNavController()
                UserManagementScreen(
                    navController = navController,
                    authViewModel = authVm,
                    motorViewModel = motorVm
                )
            }
        }

        // Active user has Suspend
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_active_1"))
        composeRule.onNodeWithTag("user_action_suspend_usr_active_1").assertExists()

        // Pending user has Approve and Reject
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_pending_2"))
        composeRule.onNodeWithTag("user_action_approve_usr_pending_2").assertExists()
        composeRule.onNodeWithTag("user_action_reject_usr_pending_2").assertExists()

        // Click Reject on pending user -> confirmation dialog with reason input
        composeRule.onNodeWithTag("user_action_reject_usr_pending_2").performClick()
        composeRule.onNodeWithTag("user_action_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("user_action_reason_input").performTextInput("Invalid details")
        composeRule.onNodeWithTag("user_action_confirm_button").performClick()

        val updatedPending = authVm.usersList.value.first { it.id == "usr_pending_2" }
        assertEquals("rejected", updatedPending.accountStatus)
        assertEquals("Invalid details", updatedPending.statusReason)
        assertTrue(updatedPending.statusUpdatedAt > 0L)
    }

    // -------------------------------------------------------------
    // 4. Compose UI: Navigation to UserDetailScreen & Detail Sections
    // -------------------------------------------------------------
    @Test
    fun clickingViewProfile_opensUserDetailScreen_withCompleteProfileStatsAndHistory() {
        val authVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(adminUser(), sampleUsers())
        }
        val motorVm = MotorViewModel().apply {
            seedMotorsForTest(sampleSinglePhaseMotors(), sampleThreePhaseMotors())
        }

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "user_management") {
                    composable("user_management") {
                        UserManagementScreen(
                            navController = navController,
                            authViewModel = authVm,
                            motorViewModel = motorVm
                        )
                    }
                    composable(
                        "user_detail/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { backStack ->
                        UserDetailScreen(
                            navController = navController,
                            authViewModel = authVm,
                            motorViewModel = motorVm,
                            userId = backStack.arguments?.getString("userId").orEmpty()
                        )
                    }
                    composable("motor_detail/{type}/{id}") {}
                }
            }
        }

        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("user_card_usr_active_1"))
        composeRule.onNodeWithTag("user_view_profile_usr_active_1").performClick()

        composeRule.onNodeWithText("User Profile Details").assertIsDisplayed()
        composeRule.onAllNodesWithText("Aarav Patel")[0].assertIsDisplayed()
        composeRule.onNodeWithTag("user_detail_list")
            .performScrollToNode(hasTestTag("user_detail_submission_stats"))
        composeRule.onNodeWithText("usr_active_1").assertExists()
        composeRule.onNodeWithTag("user_detail_submission_stats").assertIsDisplayed()
        composeRule.onNodeWithText("Breakdown: 2 Single Phase • 1 Three Phase").assertIsDisplayed()
        composeRule.onNodeWithTag("user_detail_list")
            .performScrollToNode(hasTestTag("detail_action_suspend"))
        composeRule.onNodeWithTag("detail_action_suspend").assertIsDisplayed()
    }

    // -------------------------------------------------------------
    // 5. Compose UI: Loading, Error + Retry, Empty State, Pagination
    // -------------------------------------------------------------
    @Test
    fun userManagementScreen_handlesLoadingErrorEmptyAndPaginationStates() {
        // Loading state
        val loadingVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(adminUser(), emptyList(), errorMessage = null, isLoading = true)
        }
        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                UserManagementScreen(
                    navController = rememberNavController(),
                    authViewModel = loadingVm,
                    motorViewModel = null
                )
            }
        }
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("users_loading_skeleton"))
        composeRule.onNodeWithTag("users_loading_skeleton").assertIsDisplayed()
    }

    @Test
    fun userManagementScreen_displaysErrorWithRetry_andPaginationLoadMore() {
        val manyUsers = (1..25).map { idx ->
            User(
                id = "usr_$idx",
                name = "Public User $idx",
                email = "user$idx@example.com",
                role = "public",
                accountStatus = "active",
                createdAt = baseTime - idx * 1000L
            )
        }
        val authVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(adminUser(), manyUsers)
        }

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                UserManagementScreen(
                    navController = rememberNavController(),
                    authViewModel = authVm,
                    motorViewModel = null
                )
            }
        }

        // First 20 shown, Load More shows remaining 5
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasText("Showing 20 of 25 users"))
        composeRule.onNodeWithText("Showing 20 of 25 users").assertIsDisplayed()
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("load_more_users_button"))
        composeRule.onNodeWithText("Load More Users (5 remaining)").performClick()
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasText("Showing 25 of 25 users"))
        composeRule.onNodeWithText("Showing 25 of 25 users").assertIsDisplayed()

        // Search non-matching query triggers empty state
        composeRule.onNodeWithTag("user_search_input").performTextInput("no_such_user_xyz")
        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("users_empty_state"))
        composeRule.onNodeWithTag("users_empty_state").assertIsDisplayed()
        composeRule.onNodeWithTag("user_search_input").performTextClearance()
    }

    @Test
    fun userManagementScreen_displaysErrorStateWithRetryButton() {
        val errorVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(adminUser(), emptyList(), errorMessage = "Network error occurred")
        }

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                UserManagementScreen(
                    navController = rememberNavController(),
                    authViewModel = errorVm,
                    motorViewModel = null
                )
            }
        }

        composeRule.onNodeWithTag("user_management_list")
            .performScrollToNode(hasTestTag("users_error_state"))
        composeRule.onNodeWithTag("users_error_state").assertIsDisplayed()
        composeRule.onNodeWithTag("users_retry_button").assertIsDisplayed()
    }

    // -------------------------------------------------------------
    // 6. Security: Non-Admin / Public User Guard
    // -------------------------------------------------------------
    @Test
    fun nonAdminUser_isBlockedFromUserManagementAndUserDetails() {
        val publicUser = User(
            id = "pub_1",
            name = "Public User",
            email = "pub@example.com",
            role = "public",
            accountStatus = "active",
            emailVerified = true
        )
        val authVm = AuthViewModel().apply {
            seedAdminAndUsersForTest(publicUser, sampleUsers())
        }

        var navigatedHome = false
        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "user_management") {
                    composable("user_management") {
                        UserManagementScreen(
                            navController = navController,
                            authViewModel = authVm,
                            motorViewModel = null
                        )
                    }
                    composable("home") {
                        navigatedHome = true
                    }
                    composable("public_login") {}
                }
            }
        }

        composeRule.waitForIdle()
        assertTrue(navigatedHome)
        composeRule.onAllNodesWithTag("overview_card_total").assertCountEquals(0)
        assertFalse(publicUser.isAdmin())
    }
}
