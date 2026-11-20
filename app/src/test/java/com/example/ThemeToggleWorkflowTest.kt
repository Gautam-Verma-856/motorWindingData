package com.example

import android.content.Context
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.model.ThemePreferences
import com.example.model.User
import com.example.ui.components.AppNavigationDrawerContainer
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.md_theme_dark_background
import com.example.ui.theme.md_theme_light_background
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemeToggleWorkflowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    private fun sampleUser() = User(
        id = "user_theme_1",
        name = "Aarav Patel",
        email = "aarav@example.com",
        role = "public",
        accountStatus = "active",
        emailVerified = true
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        ThemePreferences.clearThemeOverride(context)
    }

    @Test
    fun themePreferences_persistsLightAndDarkSelectionAcrossReads() {
        assertNull(ThemePreferences.loadDarkThemeOverride(context))

        ThemePreferences.saveDarkThemeOverride(context, true)
        assertEquals(true, ThemePreferences.loadDarkThemeOverride(context))

        ThemePreferences.saveDarkThemeOverride(context, false)
        assertEquals(false, ThemePreferences.loadDarkThemeOverride(context))
    }

    @Test
    fun homeScreen_themeToggleSwitchesLightAndDarkIconsColorsAndPersistsPreference() {
        var observedIsDark = false
        var observedBgColor = md_theme_light_background
        var drawerIsDark = false

        composeRule.setContent {
            var darkThemeOverride by remember {
                mutableStateOf(ThemePreferences.loadDarkThemeOverride(context) ?: false)
            }
            val toggleTheme: () -> Unit = {
                val next = !darkThemeOverride
                darkThemeOverride = next
                ThemePreferences.saveDarkThemeOverride(context, next)
            }

            MyApplicationTheme(darkTheme = darkThemeOverride) {
                observedIsDark = LocalIsDarkTheme.current
                observedBgColor = MaterialTheme.colorScheme.background

                val navController = rememberNavController()
                val drawerState = remember { androidx.compose.material3.DrawerState(DrawerValue.Closed) }

                AppNavigationDrawerContainer(
                    drawerState = drawerState,
                    user = sampleUser(),
                    currentRoute = "home",
                    pendingMotorsCount = 0,
                    pendingUsersCount = 0,
                    unreadNotificationsCount = 2,
                    gesturesEnabled = false,
                    showLogoutDialog = false,
                    onRequestLogoutDialog = {},
                    onDismissLogoutDialog = {},
                    onConfirmLogout = {},
                    onNavigate = {}
                ) {
                    drawerIsDark = LocalIsDarkTheme.current
                    HomeScreen(
                        navController = navController,
                        currentUser = sampleUser(),
                        motorViewModel = null,
                        unreadNotificationsCount = 2,
                        isDarkTheme = LocalIsDarkTheme.current,
                        onToggleTheme = toggleTheme
                    )
                }
            }
        }

        // Initial Light Mode -> Moon icon ("Switch to Dark Mode") + Notification icon untouched
        composeRule.waitForIdle()
        assertFalse(observedIsDark)
        assertFalse(drawerIsDark)
        assertEquals(md_theme_light_background, observedBgColor)
        composeRule.onNodeWithTag("theme_toggle_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_notifications_button").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Switch to Dark Mode").assertIsDisplayed()

        // Tap toggle -> switches to Dark Mode -> Sun icon ("Switch to Light Mode") + Dark background
        composeRule.onNodeWithTag("theme_toggle_button").performClick()
        composeRule.waitForIdle()

        assertTrue(observedIsDark)
        assertTrue(drawerIsDark)
        assertEquals(md_theme_dark_background, observedBgColor)
        assertEquals(true, ThemePreferences.loadDarkThemeOverride(context))
        composeRule.onNodeWithContentDescription("Switch to Light Mode").assertIsDisplayed()

        // Tap toggle again -> switches back to Light Mode -> Moon icon + Light background
        composeRule.onNodeWithTag("theme_toggle_button").performClick()
        composeRule.waitForIdle()

        assertFalse(observedIsDark)
        assertFalse(drawerIsDark)
        assertEquals(md_theme_light_background, observedBgColor)
        assertEquals(false, ThemePreferences.loadDarkThemeOverride(context))
        composeRule.onNodeWithContentDescription("Switch to Dark Mode").assertIsDisplayed()
    }
}
