package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.UpdateConfig
import com.example.ui.components.isUpdateAvailable
import com.example.ui.components.isUpdateMandatory
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.DownloadState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppUpdateCheckerOptionalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun updateAvailableAndOptional_evenWhenForceUpdateIsTrue() {
        val config = UpdateConfig(
            latestVersionCode = 105,
            latestVersionName = "105.0",
            minimumSupportedVersionCode = 105,
            updateMessage = "New features and fixes available.",
            updateUrl = "https://example.com/app-release.apk",
            forceUpdate = true
        )

        assertTrue(isUpdateAvailable(currentVersionCode = 101, config = config))
        assertFalse(isUpdateMandatory(currentVersionCode = 101, config = config))
        assertFalse(isUpdateAvailable(currentVersionCode = 105, config = config))
        assertFalse(isUpdateAvailable(currentVersionCode = 106, config = config))
    }

    @Test
    fun appUpdateDialog_showsInstallLatestVersionAndLater_evenWithForceUpdateTrue() {
        val config = UpdateConfig(
            latestVersionCode = 105,
            latestVersionName = "105.0",
            minimumSupportedVersionCode = 105,
            updateMessage = "Optional update available.",
            updateUrl = "https://example.com/update.apk",
            forceUpdate = true
        )
        var dismissed = false
        var updateClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppUpdateDialog(
                    config = config,
                    isMandatory = isUpdateMandatory(101, config),
                    downloadState = DownloadState.Idle,
                    onDismiss = { dismissed = true },
                    onUpdate = { updateClicked = true },
                    onInstall = {},
                    onOpenBrowser = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Update Available").assertIsDisplayed()
        composeTestRule.onNodeWithText("Install Latest Version").assertIsDisplayed()
        composeTestRule.onNodeWithText("Later").assertIsDisplayed()

        composeTestRule.onNodeWithTag("update_later_button").performClick()
        assertTrue(dismissed)
        assertFalse(updateClicked)
        // Ensure config version codes are untouched
        assertEquals(105, config.latestVersionCode)
        assertEquals(105, config.minimumSupportedVersionCode)
    }

    @Test
    fun appUpdateDialog_installLatestVersionTriggersUpdateAndInstall() {
        val config = UpdateConfig(
            latestVersionCode = 105,
            latestVersionName = "105.0",
            minimumSupportedVersionCode = 105,
            updateMessage = "Update ready.",
            updateUrl = "https://example.com/update.apk",
            forceUpdate = true
        )
        var updateClicked = false
        var installClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppUpdateDialog(
                    config = config,
                    isMandatory = isUpdateMandatory(101, config),
                    downloadState = DownloadState.Idle,
                    onDismiss = {},
                    onUpdate = { updateClicked = true },
                    onInstall = { installClicked = true },
                    onOpenBrowser = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("update_install_button").performClick()
        assertTrue(updateClicked)
        assertFalse(installClicked)
    }

    @Test
    fun appUpdateDialog_readyToInstall_keepsBothInstallLatestVersionAndLater() {
        val config = UpdateConfig(
            latestVersionCode = 105,
            latestVersionName = "105.0",
            minimumSupportedVersionCode = 105,
            updateMessage = "Update ready.",
            updateUrl = "https://example.com/update.apk",
            forceUpdate = true
        )
        var installClicked = false
        var dismissed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                AppUpdateDialog(
                    config = config,
                    isMandatory = isUpdateMandatory(101, config),
                    downloadState = DownloadState.Success(File("update.apk")),
                    onDismiss = { dismissed = true },
                    onUpdate = {},
                    onInstall = { installClicked = true },
                    onOpenBrowser = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Ready to Install").assertIsDisplayed()
        composeTestRule.onNodeWithText("Install Latest Version").assertIsDisplayed()
        composeTestRule.onNodeWithText("Later").assertIsDisplayed()

        composeTestRule.onNodeWithTag("update_install_button").performClick()
        assertTrue(installClicked)

        composeTestRule.onNodeWithTag("update_later_button").performClick()
        assertTrue(dismissed)
    }
}
