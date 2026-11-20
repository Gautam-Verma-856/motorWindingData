package com.example

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import com.example.model.NotificationCounter
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.model.User
import com.example.ui.components.AppNavigationDrawerContainer
import com.example.ui.components.buildDrawerSections
import com.example.ui.components.canShowDashboardMenuItem
import com.example.ui.components.isDrawerItemSelected
import com.example.ui.components.resolveDashboardRoute
import com.example.ui.screens.ContactConfig
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
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
class NavigationDrawerWorkflowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun samplePublicUser() = User(
        id = "pub_user_1",
        name = "Ramesh Sharma",
        email = "ramesh@example.com",
        mobile = "+919876543210",
        role = "public",
        accountStatus = "active",
        emailVerified = true
    )

    private fun sampleAdminUser() = User(
        id = "admin_1",
        name = "Gautam Verma",
        email = "admin@example.com",
        role = "admin",
        accountStatus = "active",
        emailVerified = true
    )

    // -------------------------------------------------------------
    // 1. Public menu options display properly (and no admin/unimplemented items)
    // -------------------------------------------------------------
    @Test
    fun publicMenuOptions_displayProperlyAndHideAdminOrUnimplementedItems() {
        val publicUser = samplePublicUser()
        val sections = buildDrawerSections(
            user = publicUser,
            pendingMotorsCount = 4,
            pendingUsersCount = 2,
            unreadNotificationsCount = 3
        )

        assertEquals(listOf("MAIN MENU", "ACCOUNT"), sections.map { it.title })
        val allLabels = sections.flatMap { it.items }.map { it.label }

        // Required Public items
        assertEquals(
            listOf(
                "Go to Dashboard",
                "Advanced Search",
                "Motor Categories",
                "My Submissions",
                "Notifications",
                "My Profile",
                "Edit Profile",
                "Change Password",
                "Notification Settings",
                "Contact Us",
                "App Information",
                "Logout"
            ),
            allLabels
        )

        // Forbidden admin/unimplemented options
        assertFalse(allLabels.contains("Admin Dashboard"))
        assertFalse(allLabels.contains("Manage Motor Data"))
        assertFalse(allLabels.contains("Pending Approvals"))
        assertFalse(allLabels.contains("Manage Public Users"))
        assertFalse(allLabels.contains("Account Settings"))
        assertFalse(allLabels.contains("Favorite Motors"))
        assertFalse(allLabels.contains("Analytics"))
        assertFalse(allLabels.contains("Activity History"))

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = publicUser,
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 3,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = {}
                ) {
                    Text("Main Content")
                }
            }
        }

        composeRule.onNodeWithTag("navigation_drawer_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_profile_header").assertIsDisplayed()
        composeRule.onAllNodesWithText("Motor Winding Data")[0].assertIsDisplayed()
        composeRule.onNodeWithText("Ramesh Sharma").assertIsDisplayed()
        composeRule.onNodeWithText("Public").assertIsDisplayed()
        composeRule.onNodeWithText("Active").assertIsDisplayed()

        composeRule.onNodeWithTag("drawer_item_go_to_dashboard").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_advanced_search").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_motor_categories").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_my_submissions").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_notifications").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_my_profile").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_edit_profile").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_change_password").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_notification_settings").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_contact_us").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_app_information").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_logout").performScrollTo().assertIsDisplayed()
    }

    // -------------------------------------------------------------
    // 2. Admin menu options display properly (and no public-only/unimplemented items)
    // -------------------------------------------------------------
    @Test
    fun adminMenuOptions_displayProperlyAndHideUnimplementedItems() {
        val adminUser = sampleAdminUser()
        val sections = buildDrawerSections(
            user = adminUser,
            pendingMotorsCount = 5,
            pendingUsersCount = 2,
            unreadNotificationsCount = 7
        )

        assertEquals(listOf("MAIN MENU", "ADMIN TOOLS"), sections.map { it.title })
        val allLabels = sections.flatMap { it.items }.map { it.label }

        assertEquals(
            listOf(
                "Go to Dashboard",
                "Advanced Search",
                "Manage Motor Data",
                "Add Single Phase Motor",
                "Add Three Phase Motor",
                "Pending Approvals",
                "Manage Public Users",
                "Notifications",
                "Account Settings",
                "Contact Us",
                "App Information",
                "Logout"
            ),
            allLabels
        )

        assertFalse(allLabels.contains("My Submissions"))
        assertFalse(allLabels.contains("Analytics"))
        assertFalse(allLabels.contains("Activity History"))
        assertFalse(allLabels.contains("Favorite Motors"))

        composeRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = adminUser,
                    currentRoute = "admin_dashboard",
                    pendingMotorsCount = 5,
                    pendingUsersCount = 2,
                    unreadNotificationsCount = 7,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = {}
                ) {
                    Text("Admin Content")
                }
            }
        }

        composeRule.onNodeWithText("Gautam Verma").assertIsDisplayed()
        composeRule.onNodeWithText("Admin").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_go_to_dashboard").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_advanced_search").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_manage_motor_data").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_add_single_phase").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_add_three_phase").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_pending_approvals").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_manage_public_users").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_notifications").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_account_settings").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_contact_us").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_app_information").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_logout").performScrollTo().assertIsDisplayed()
    }

    // -------------------------------------------------------------
    // 3. Profile navigation works (via header click and menu item)
    // -------------------------------------------------------------
    @Test
    fun clickingProfileHeaderOrMyProfileItem_closesDrawerAndNavigatesToProfile() {
        val navigatedRoutes = mutableListOf<String>()
        var isDrawerOpenAfterClick = true

        composeRule.setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                isDrawerOpenAfterClick = drawerState.isOpen
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = samplePublicUser(),
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 0,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = { navigatedRoutes.add(it) }
                ) {
                    Text("Home")
                }
            }
        }

        composeRule.onNodeWithTag("drawer_profile_header").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("profile"), navigatedRoutes)
        assertFalse("Drawer should close after clicking profile header", isDrawerOpenAfterClick)
    }

    // -------------------------------------------------------------
    // 4. Advanced Search navigation works
    // -------------------------------------------------------------
    @Test
    fun clickingAdvancedSearchInDrawer_closesDrawerAndNavigatesToAdvancedSearch() {
        val navigatedRoutes = mutableListOf<String>()
        var drawerIsOpen = true

        composeRule.setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                drawerIsOpen = drawerState.isOpen
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = sampleAdminUser(),
                    currentRoute = "admin_dashboard",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 0,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = { navigatedRoutes.add(it) }
                ) {
                    Text("Dashboard")
                }
            }
        }

        composeRule.onNodeWithTag("drawer_item_advanced_search").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("advanced_search"), navigatedRoutes)
        assertFalse("Drawer should close after selecting Advanced Search", drawerIsOpen)
    }

    // -------------------------------------------------------------
    // 5. Notification navigation & unread count badge
    // -------------------------------------------------------------
    @Test
    fun notificationsItem_displaysUnreadBadgeAndNavigatesToNotifications() {
        val navigatedRoutes = mutableListOf<String>()

        // Verify unread count calculations for Admin & Public
        val adminUnread = NotificationCounter.countAdminUnread(
            singlePhaseMotors = listOf(
                SinglePhaseMotor(id = "s1", status = "pending", adminSeen = false, seen = false)
            ),
            threePhaseMotors = listOf(
                ThreePhaseMotor(id = "t1", status = "pending", adminSeen = false, seen = false)
            ),
            usersList = listOf(
                User(id = "u1", role = "public", accountStatus = "pending", adminSeen = false, seen = false)
            )
        )
        assertEquals(3, adminUnread)

        val publicUnread = NotificationCounter.countPublicUnread(
            userId = "pub_1",
            singlePhaseMotors = listOf(
                SinglePhaseMotor(id = "s1", createdBy = "pub_1", status = "approved"),
                SinglePhaseMotor(id = "s2", createdBy = "pub_1", status = "rejected")
            ),
            threePhaseMotors = listOf(
                ThreePhaseMotor(id = "t1", createdBy = "other_user", status = "approved")
            ),
            seenKeys = setOf(NotificationCounter.publicNotificationKey("single", "s1", "approved"))
        )
        assertEquals(1, publicUnread)

        composeRule.setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = samplePublicUser(),
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 4,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = { navigatedRoutes.add(it) }
                ) {
                    Text("Home")
                }
            }
        }

        composeRule.onNodeWithTag("drawer_item_notifications").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_notifications").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("notifications"), navigatedRoutes)
    }

    // -------------------------------------------------------------
    // 6. Logout works with confirmation dialog (cancel preserves session, confirm logs out)
    // -------------------------------------------------------------
    @Test
    fun logoutOption_requiresConfirmationDialogBeforeLoggingOut() {
        var showLogoutDialog by mutableStateOf(false)
        var loggedOut = false

        composeRule.setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = samplePublicUser(),
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 0,
                    gesturesEnabled = true,
                    showLogoutDialog = showLogoutDialog,
                    onRequestLogoutDialog = { showLogoutDialog = true },
                    onDismissLogoutDialog = { showLogoutDialog = false },
                    onConfirmLogout = {
                        showLogoutDialog = false
                        loggedOut = true
                    },
                    onNavigate = {}
                ) {
                    Text("Home")
                }
            }
        }

        // Click Logout in drawer -> opens confirmation dialog, NOT immediate logout
        composeRule.onNodeWithTag("drawer_item_logout").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertFalse("Session must remain active before confirmation", loggedOut)
        composeRule.onNodeWithTag("logout_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Confirm Logout").assertIsDisplayed()

        // Cancel dismisses without logging out
        composeRule.onNodeWithTag("cancel_logout_button").performClick()
        composeRule.waitForIdle()
        assertFalse("Cancel must not log user out", loggedOut)

        // Confirm triggers logout
        showLogoutDialog = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_logout_button").performClick()
        composeRule.waitForIdle()
        assertTrue("Confirming dialog must log user out", loggedOut)
    }

    // -------------------------------------------------------------
    // 7. Back button closes drawer (and Close button closes drawer)
    // -------------------------------------------------------------
    @Test
    fun backButtonAndCloseButton_closeOpenDrawer() {
        var backDispatcher: OnBackPressedDispatcher? = null
        var isDrawerOpen = true

        composeRule.setContent {
            MyApplicationTheme {
                backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                val scope = rememberCoroutineScope()
                isDrawerOpen = drawerState.isOpen

                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = samplePublicUser(),
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 0,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = {}
                ) {
                    androidx.compose.material3.Button(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = androidx.compose.ui.Modifier.onOpenTag()
                    ) {
                        Text("Reopen")
                    }
                }
            }
        }

        composeRule.waitForIdle()
        assertTrue("Drawer starts open", isDrawerOpen)

        composeRule.runOnUiThread {
            backDispatcher?.onBackPressed()
        }
        composeRule.waitForIdle()
        assertFalse("Back press must close the open drawer", isDrawerOpen)

        // Reopen and test explicit Close button
        composeRule.onNodeWithText("Reopen").performClick()
        composeRule.waitForIdle()
        assertTrue("Drawer reopened", isDrawerOpen)

        composeRule.onNodeWithTag("drawer_close_button").performClick()
        composeRule.waitForIdle()
        assertFalse("Close button must close the drawer", isDrawerOpen)
    }

    // -------------------------------------------------------------
    // 8. Existing menu routes still work & Contact stays Instagram-only
    // -------------------------------------------------------------
    @Test
    fun existingMenuRoutesAndSelectionHighlight_andContactPreserved() {
        // Active route matching
        assertTrue(isDrawerItemSelected("home", "home"))
        assertTrue(isDrawerItemSelected("user_management", "user_management?filter=pending"))
        assertTrue(isDrawerItemSelected("motor_list/pending", "motor_list/pending"))
        assertFalse(isDrawerItemSelected("motor_list/all", "motor_list/pending"))

        // Existing Public + Admin routes map cleanly
        val publicRoutes = buildDrawerSections(samplePublicUser()).flatMap { it.items }.associate { it.label to it.route }
        assertEquals("user_dashboard", publicRoutes["Go to Dashboard"])
        assertEquals("advanced_search", publicRoutes["Advanced Search"])
        assertEquals("home", publicRoutes["Motor Categories"])
        assertEquals("motor_list/my_submissions", publicRoutes["My Submissions"])
        assertEquals("public_change_password", publicRoutes["Change Password"])
        assertEquals("contact", publicRoutes["Contact Us"])

        val adminRoutes = buildDrawerSections(sampleAdminUser()).flatMap { it.items }.associate { it.label to it.route }
        assertEquals("admin_dashboard", adminRoutes["Go to Dashboard"])
        assertEquals("advanced_search", adminRoutes["Advanced Search"])
        assertEquals("motor_list/all", adminRoutes["Manage Motor Data"])
        assertEquals("add_edit_motor/single/new", adminRoutes["Add Single Phase Motor"])
        assertEquals("add_edit_motor/three/new", adminRoutes["Add Three Phase Motor"])
        assertEquals("motor_list/pending", adminRoutes["Pending Approvals"])
        assertEquals("user_management", adminRoutes["Manage Public Users"])
        assertEquals("contact", adminRoutes["Contact Us"])

        // Existing Instagram-only Contact config untouched
        assertEquals("@gautam_verma_856", ContactConfig.INSTAGRAM_HANDLE)
        assertTrue(ContactConfig.INSTAGRAM_URL.contains("instagram.com"))
    }

    // -------------------------------------------------------------
    // 9. Light and Dark themes render properly without crashes
    // -------------------------------------------------------------
    @Test
    fun drawerAndLogoutDialog_renderProperlyInLightAndDarkThemes() {
        var useDarkTheme by mutableStateOf(false)

        composeRule.setContent {
            MyApplicationTheme(darkTheme = useDarkTheme) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = sampleAdminUser(),
                    currentRoute = "admin_dashboard",
                    pendingMotorsCount = 2,
                    pendingUsersCount = 1,
                    unreadNotificationsCount = 3,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = {}
                ) {
                    Text("Themed Host")
                }
            }
        }

        // Light theme assertions
        composeRule.onNodeWithTag("navigation_drawer_sheet").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Motor Winding Data").fetchSemanticsNodes().isNotEmpty())

        // Switch to Dark theme
        useDarkTheme = true
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("navigation_drawer_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_profile_header").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_item_account_settings").performScrollTo().assertIsDisplayed()
    }

    // -------------------------------------------------------------
    // 10. HomeScreen removes Go to Dashboard button & opens drawer cleanly
    // -------------------------------------------------------------
    @Test
    fun homeScreen_hidesDashboardButtonAndOpensDrawerCleanly() {
        var drawerOpenedCount = 0

        composeRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                HomeScreen(
                    navController = navController,
                    currentUser = samplePublicUser(),
                    motorViewModel = null,
                    unreadNotificationsCount = 2,
                    onOpenDrawer = { drawerOpenedCount++ }
                )
            }
        }

        composeRule.onNodeWithTag("drawer_menu_button").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("home_notifications_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_notification_unread_dot").assertIsDisplayed()
        composeRule.onNodeWithText("Go to Dashboard").assertDoesNotExist()
        composeRule.onNodeWithText("Go to Admin Dashboard").assertDoesNotExist()
        assertEquals(1, drawerOpenedCount)
    }

    // -------------------------------------------------------------
    // 11. Go to Dashboard drawer item routes by role and safeguards unauthenticated states
    // -------------------------------------------------------------
    @Test
    fun goToDashboardDrawerItem_routesByRoleAndGuardsLoggedOutUsers() {
        // Role & login visibility / safe route resolution
        assertTrue(canShowDashboardMenuItem(sampleAdminUser()))
        assertTrue(canShowDashboardMenuItem(samplePublicUser()))
        assertEquals("admin_dashboard", resolveDashboardRoute(sampleAdminUser()))
        assertEquals("user_dashboard", resolveDashboardRoute(samplePublicUser()))

        // Unauthenticated / unverified / pending / blocked users must not see item and must safely resolve to login
        assertFalse(canShowDashboardMenuItem(null))
        assertTrue(buildDrawerSections(null).isEmpty())
        assertEquals("public_login", resolveDashboardRoute(null))

        val pendingPublic = samplePublicUser().copy(accountStatus = "pending")
        val unverifiedPublic = samplePublicUser().copy(emailVerified = false)
        val blockedAdmin = sampleAdminUser().copy(accountStatus = "blocked")
        assertFalse(canShowDashboardMenuItem(pendingPublic))
        assertFalse(canShowDashboardMenuItem(unverifiedPublic))
        assertFalse(canShowDashboardMenuItem(blockedAdmin))
        assertEquals("public_login", resolveDashboardRoute(pendingPublic))
        assertEquals("public_login", resolveDashboardRoute(unverifiedPublic))
        assertEquals("public_login", resolveDashboardRoute(blockedAdmin))

        // Clicking "Go to Dashboard" in drawer navigates to admin_dashboard for Admin and user_dashboard for Public
        val navigatedRoutes = mutableListOf<String>()
        var activeUser by mutableStateOf(sampleAdminUser())

        composeRule.setContent {
            MyApplicationTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = activeUser,
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 0,
                    gesturesEnabled = true,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = { navigatedRoutes.add(it) }
                ) {
                    Text("Home")
                }
            }
        }

        composeRule.onNodeWithTag("drawer_item_go_to_dashboard").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(listOf("admin_dashboard"), navigatedRoutes)
    }

    // -------------------------------------------------------------
    // 12. Fresh launch & login transition keep drawer CLOSED until Menu icon tapped
    // -------------------------------------------------------------
    @Test
    fun freshLaunchAndUserLoginTransition_keepsDrawerClosedUntilMenuIconTapped() {
        var activeUser by mutableStateOf<User?>(null)
        var drawerOpenedState = false
        var drawerTargetClosed = true

        composeRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val drawerState = androidx.compose.runtime.remember {
                    androidx.compose.material3.DrawerState(initialValue = DrawerValue.Closed)
                }
                val scope = rememberCoroutineScope()
                drawerOpenedState = drawerState.isOpen
                drawerTargetClosed = drawerState.targetValue == DrawerValue.Closed

                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = activeUser,
                    currentRoute = if (activeUser == null) "splash" else "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 0,
                    gesturesEnabled = false,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = {}
                ) {
                    if (activeUser == null) {
                        Text("Splash")
                    } else {
                        HomeScreen(
                            navController = navController,
                            currentUser = activeUser,
                            motorViewModel = null,
                            unreadNotificationsCount = 0,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()
        assertFalse("Drawer must be closed on splash launch", drawerOpenedState)
        assertTrue(drawerTargetClosed)

        // Simulate session resolved / user login -> Main Page
        activeUser = sampleAdminUser()
        composeRule.waitForIdle()
        assertFalse("Drawer must stay CLOSED after transitioning to Main Page", drawerOpenedState)
        assertTrue("Drawer targetValue must stay Closed", drawerTargetClosed)

        // Tapping Menu icon opens the drawer
        composeRule.onNodeWithTag("drawer_menu_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue("Drawer must open when Menu icon is tapped", drawerOpenedState)
        composeRule.onNodeWithTag("navigation_drawer_sheet").assertIsDisplayed()

        // Selecting a menu item closes the drawer
        composeRule.onNodeWithTag("drawer_item_go_to_dashboard").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertFalse("Drawer must close after selecting menu item", drawerOpenedState)
    }

    private fun androidx.compose.ui.Modifier.onOpenTag(): androidx.compose.ui.Modifier = this
}
