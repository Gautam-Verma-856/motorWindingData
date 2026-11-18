package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import com.example.model.MotorSearchEngine
import com.example.model.MotorSearchFilter
import com.example.model.NumericFilterSpec
import com.example.model.SearchApprovalStatus
import com.example.model.SearchMotorType
import com.example.model.SearchRecentlyAdded
import com.example.model.SearchSortOption
import com.example.model.SinglePhaseCategories
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.ui.screens.AdvancedSearchScreen
import com.example.ui.theme.MyApplicationTheme
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
class AdvancedSearchWorkflowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val now = 1_750_000_000_000L

    private fun sampleSinglePhaseMotors(): List<SinglePhaseMotor> = listOf(
        SinglePhaseMotor(
            id = "sp_1",
            category = SinglePhaseCategories.SELF_PRIMING,
            companyName = "Crompton  Greaves",
            hp = "1 HP",
            capacitor = "36 mfd",
            rpm = "2880",
            amps = "5.4",
            coilSize = "22 SWG + 24 SWG",
            connection = "Series",
            runningSwg = "22",
            startingSwg = "24",
            status = "approved",
            createdAt = now - 2 * 60 * 60 * 1000L, // 2h ago
            createdBy = "admin_1",
            createdByName = "PrivateAdminName"
        ),
        SinglePhaseMotor(
            id = "sp_2",
            category = SinglePhaseCategories.COOLER_EXHAUST,
            companyName = "Symphony Cooler",
            hp = "0.25 HP",
            capacitor = "4 mfd",
            rpm = "1440",
            amps = "1.2",
            coilSize = "30 SWG",
            connection = "Parallel",
            runningSwg = "30",
            startingSwg = "32",
            status = "approved",
            createdAt = now - 5 * 24 * 60 * 60 * 1000L // 5d ago
        ),
        SinglePhaseMotor(
            id = "sp_legacy",
            category = "",
            companyName = "Usha Classic",
            hp = "0.5 HP",
            capacitor = "20 mfd",
            rpm = "",
            amps = "",
            coilSize = "",
            connection = "",
            runningSwg = "25",
            startingSwg = "27",
            status = "approved",
            createdAt = now - 45 * 24 * 60 * 60 * 1000L // 45d ago
        ),
        SinglePhaseMotor(
            id = "sp_pending",
            category = SinglePhaseCategories.SUBMERSIBLE,
            companyName = "Havells Pending",
            hp = "1.5 HP",
            rpm = "2800",
            amps = "7.5",
            coilSize = "21 SWG",
            connection = "Star",
            runningSwg = "21",
            startingSwg = "23",
            status = "pending",
            createdAt = now - 30 * 60 * 1000L,
            createdBy = "pub_secret_user",
            createdByName = "Secret User Email"
        ),
        SinglePhaseMotor(
            id = "sp_rejected",
            category = SinglePhaseCategories.WASHING_MACHINE,
            companyName = "LG Rejected",
            hp = "0.5 HP",
            rpm = "1380",
            amps = "2.2",
            coilSize = "27 SWG",
            connection = "Series",
            status = "rejected",
            rejectionReason = "Incorrect turn count",
            createdAt = now - 60 * 60 * 1000L
        )
    )

    private fun sampleThreePhaseMotors(): List<ThreePhaseMotor> = listOf(
        ThreePhaseMotor(
            id = "tp_1",
            category = "Monoblock",
            name = "Kirloskar Pump",
            slot = "24",
            hp = "5 HP",
            rpm = "1440",
            amps = "7.8",
            coilSize = "19 SWG",
            connection = "Delta",
            swg = "19",
            status = "approved",
            createdAt = now - 10 * 60 * 60 * 1000L // 10h ago
        ),
        ThreePhaseMotor(
            id = "tp_2",
            category = "Submersible",
            name = "Texmo Borewell",
            slot = "36",
            hp = "7.5 HP",
            rpm = "2880",
            amps = "11.5",
            coilSize = "1.1 mm",
            connection = "Star-Delta",
            swg = "18",
            status = "approved",
            createdAt = now - 20 * 24 * 60 * 60 * 1000L // 20d ago
        ),
        ThreePhaseMotor(
            id = "tp_custom_conn",
            category = "Pandubi",
            name = "ABB Heavy",
            slot = "48",
            hp = "15 HP",
            rpm = "960",
            amps = "21.0",
            coilSize = "16 SWG Dual",
            connection = "Double Delta",
            swg = "16",
            status = "approved",
            createdAt = now - 3 * 24 * 60 * 60 * 1000L // 3d ago
        )
    )

    private fun unifiedSample() = MotorSearchEngine.buildUnifiedList(
        sampleSinglePhaseMotors(),
        sampleThreePhaseMotors()
    )

    @Test
    fun nameQueryNormalization_handlesExtraSpacesAndPartialCaseInsensitiveMatch() {
        val results = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(query = "  crompton   greaves "),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(listOf("sp_1"), results.map { it.id })
    }

    @Test
    fun publicSearch_strictlyExcludesPendingAndRejectedEvenIfFilterRequestsThem() {
        val publicDefault = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(approvalStatus = SearchApprovalStatus.PENDING),
            isAdmin = false,
            nowMillis = now
        )
        assertTrue(publicDefault.all { it.isApproved })
        assertFalse(publicDefault.any { it.id == "sp_pending" || it.id == "sp_rejected" })
        assertEquals(6, publicDefault.size)
    }

    @Test
    fun adminSearch_includesAllStatusesAndFiltersByApprovalStatus() {
        val adminAll = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(approvalStatus = SearchApprovalStatus.ALL),
            isAdmin = true,
            nowMillis = now
        )
        assertEquals(8, adminAll.size)

        val adminPending = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(approvalStatus = SearchApprovalStatus.PENDING),
            isAdmin = true,
            nowMillis = now
        )
        assertEquals(listOf("sp_pending"), adminPending.map { it.id })

        val adminRejected = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(approvalStatus = SearchApprovalStatus.REJECTED),
            isAdmin = true,
            nowMillis = now
        )
        assertEquals(listOf("sp_rejected"), adminRejected.map { it.id })
    }

    @Test
    fun numericExactAndRangeFilters_workForHpRpmAmpsAndSlot() {
        // HP Range 1.0..6.0 -> sp_1 (1 HP), tp_1 (5 HP)
        val hpRange = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(hp = NumericFilterSpec(min = "1", max = "6")),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(setOf("sp_1", "tp_1"), hpRange.map { it.id }.toSet())

        // RPM Exact 1440 -> sp_2 (1440), tp_1 (1440)
        val rpmExact = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(rpm = NumericFilterSpec(exact = "1440")),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(setOf("sp_2", "tp_1"), rpmExact.map { it.id }.toSet())

        // Amps Range 7..12 -> tp_1 (7.8), tp_2 (11.5)
        val ampsRange = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(amps = NumericFilterSpec(min = "7", max = "12")),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(setOf("tp_1", "tp_2"), ampsRange.map { it.id }.toSet())

        // Slot Range 30..48 -> tp_2 (36), tp_custom_conn (48); excludes single-phase without slot cleanly
        val slotRange = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(slot = NumericFilterSpec(min = "30", max = "48")),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(setOf("tp_2", "tp_custom_conn"), slotRange.map { it.id }.toSet())
    }

    @Test
    fun swgCoilSizeConnectionAndRecentlyAdded_filterCorrectlySimultaneously() {
        // Multi-filter: Single Phase + SWG 24 + Series + Last 24 Hours -> sp_1
        val combined = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(
                motorType = SearchMotorType.SINGLE_PHASE,
                swg = "24 SWG",
                coilSize = "22 swg",
                connection = "Series",
                recentlyAdded = SearchRecentlyAdded.LAST_24_HOURS
            ),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(listOf("sp_1"), combined.map { it.id })

        // Connection "Other" matches custom connection "Double Delta" only
        val otherConn = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(connection = "Other"),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(listOf("tp_custom_conn"), otherConn.map { it.id })

        // Recently added 7 days
        val recent7d = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(recentlyAdded = SearchRecentlyAdded.LAST_7_DAYS),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(setOf("sp_1", "tp_1", "tp_custom_conn", "sp_2"), recent7d.map { it.id }.toSet())
    }

    @Test
    fun sortingOptions_handleRecentlyAddedCompanyNameHpAndRpmWithLegacyBlankFields() {
        val byRecent = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(sortBy = SearchSortOption.RECENTLY_ADDED),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals("sp_1", byRecent.first().id)
        assertEquals("sp_legacy", byRecent.last().id)

        val byName = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(sortBy = SearchSortOption.COMPANY_NAME),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(listOf("tp_custom_conn", "sp_1", "tp_1", "sp_2", "tp_2", "sp_legacy"), byName.map { it.id })

        val byHp = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(sortBy = SearchSortOption.HP),
            isAdmin = false,
            nowMillis = now
        )
        assertEquals(listOf("sp_2", "sp_legacy", "sp_1", "tp_1", "tp_2", "tp_custom_conn"), byHp.map { it.id })

        val byRpm = MotorSearchEngine.filterAndSort(
            items = unifiedSample(),
            filter = MotorSearchFilter(sortBy = SearchSortOption.RPM),
            isAdmin = false,
            nowMillis = now
        )
        // Missing RPM (sp_legacy) sorted cleanly at the end without crash
        assertEquals("tp_custom_conn", byRpm.first().id) // 960 RPM
        assertEquals("sp_legacy", byRpm.last().id) // "" RPM
    }

    @Test
    fun publicAdvancedSearchScreen_hidesAdminControlsAndPrivateData_andSupportsFiltersAndClearAll() {
        val viewModel = MotorViewModel()
        viewModel.seedSearchDataForTest(
            singlePhaseMotors = sampleSinglePhaseMotors(),
            threePhaseMotors = sampleThreePhaseMotors(),
            isAdmin = false,
            hasMore = true
        )

        composeRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                AdvancedSearchScreen(
                    navController = navController,
                    motorViewModel = viewModel,
                    role = "public"
                )
            }
        }
        composeRule.waitForIdle()

        // Public sees only 6 approved records; pending/rejected never appear
        composeRule.onNodeWithText("6 Matches").assertIsDisplayed()
        composeRule.onNodeWithText("Havells Pending").assertDoesNotExist()
        composeRule.onNodeWithText("LG Rejected").assertDoesNotExist()
        composeRule.onNodeWithText("PrivateAdminName").assertDoesNotExist()
        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("load_more_search_button"))
        composeRule.onNodeWithTag("load_more_search_button").assertIsDisplayed()

        // Open Filters panel -> Approval Status filter must NOT exist for public users
        composeRule.onNodeWithTag("toggle_filters_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("advanced_filter_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("filter_status_pending").assertDoesNotExist()

        // Apply Three Phase + Connection Delta
        composeRule.onNodeWithTag("filter_type_three").performClick()
        composeRule.onNodeWithTag("filter_connection_delta").performScrollTo().performClick()
        composeRule.onNodeWithTag("apply_filters_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("1 Match").assertIsDisplayed()
        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("search_result_card_three_tp_1"))
        composeRule.onNodeWithTag("search_result_card_three_tp_1").assertIsDisplayed()

        // Clear All restores all 6 approved results
        composeRule.onNodeWithTag("quick_clear_filters_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("6 Matches").assertIsDisplayed()

        // Query producing empty results shows friendly empty state
        composeRule.onNodeWithTag("advanced_search_input").performTextInput("NonExistentMotorBrand999")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("0 Matches").assertIsDisplayed()
        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("search_empty_state"))
        composeRule.onNodeWithTag("search_empty_state").assertIsDisplayed()
    }

    @Test
    fun adminAdvancedSearchScreen_showsAllStatusesAndApprovalFilterAndActions() {
        val viewModel = MotorViewModel()
        viewModel.seedSearchDataForTest(
            singlePhaseMotors = sampleSinglePhaseMotors(),
            threePhaseMotors = sampleThreePhaseMotors(),
            isAdmin = true,
            hasMore = false
        )

        composeRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                AdvancedSearchScreen(
                    navController = navController,
                    motorViewModel = viewModel,
                    role = "admin"
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("8 Matches").assertIsDisplayed()
        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("search_result_card_single_sp_pending"))
        composeRule.onNodeWithTag("search_result_status_single_sp_pending", useUnmergedTree = true)
            .assertExists()

        // Open Filters to filter by Pending status
        composeRule.onNodeWithTag("toggle_filters_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("filter_status_pending").performScrollTo().performClick()
        composeRule.onNodeWithTag("apply_filters_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("1 Match").assertIsDisplayed()
        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("search_result_card_single_sp_pending"))
        composeRule.onNodeWithTag("search_result_card_single_sp_pending").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysErrorStateWhenRepositoryFails() {
        val viewModel = MotorViewModel()
        viewModel.setSearchErrorForTest("Network unavailable")

        composeRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                AdvancedSearchScreen(
                    navController = navController,
                    motorViewModel = viewModel,
                    role = "public"
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("search_error_state"))
        composeRule.onNodeWithTag("search_error_state").assertIsDisplayed()
        composeRule.onNodeWithText("Network unavailable").assertIsDisplayed()
    }

    @Test
    fun searchScreen_supportsNumericExactRangeInputsAndSorting() {
        val viewModel = MotorViewModel()
        viewModel.seedSearchDataForTest(
            singlePhaseMotors = sampleSinglePhaseMotors(),
            threePhaseMotors = sampleThreePhaseMotors(),
            isAdmin = false,
            hasMore = false
        )

        composeRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                AdvancedSearchScreen(
                    navController = navController,
                    motorViewModel = viewModel,
                    role = "public"
                )
            }
        }
        composeRule.waitForIdle()

        // Sort by HP and RPM chips work cleanly without crashing on legacy empty fields
        composeRule.onNodeWithTag("sort_hp").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sort_rpm").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("6 Matches").assertIsDisplayed()

        // Expand Filters -> use HP Range (1..5) + RPM Exact (1440)
        composeRule.onNodeWithTag("toggle_filters_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("hp_mode_range").performScrollTo().performClick()
        composeRule.onNodeWithTag("hp_min_input").performScrollTo().performTextInput("1")
        composeRule.onNodeWithTag("hp_max_input").performScrollTo().performTextInput("5")
        composeRule.onNodeWithTag("rpm_exact_input").performScrollTo().performTextInput("1440")
        composeRule.onNodeWithTag("apply_filters_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Only tp_1 (5 HP, 1440 RPM) matches
        composeRule.onNodeWithText("1 Match").assertIsDisplayed()
        composeRule.onNodeWithTag("search_results_list")
            .performScrollToNode(hasTestTag("search_result_card_three_tp_1"))
        composeRule.onNodeWithTag("search_result_card_three_tp_1").assertIsDisplayed()
    }
}
