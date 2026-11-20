package com.example.model

import android.content.Context

data class NotificationSettings(
    val submissionAlertsEnabled: Boolean = true,
    val userApprovalAlertsEnabled: Boolean = true,
    val appUpdateAlertsEnabled: Boolean = true
)

object NotificationPreferences {
    private const val PREFS_NAME = "motor_winding_notification_prefs"
    private const val KEY_SUBMISSION_ALERTS = "submission_alerts_enabled"
    private const val KEY_USER_APPROVAL_ALERTS = "user_approval_alerts_enabled"
    private const val KEY_APP_UPDATE_ALERTS = "app_update_alerts_enabled"
    private const val KEY_SEEN_PUBLIC_PREFIX = "seen_public_notifications_"

    fun loadSettings(context: Context): NotificationSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return NotificationSettings(
            submissionAlertsEnabled = prefs.getBoolean(KEY_SUBMISSION_ALERTS, true),
            userApprovalAlertsEnabled = prefs.getBoolean(KEY_USER_APPROVAL_ALERTS, true),
            appUpdateAlertsEnabled = prefs.getBoolean(KEY_APP_UPDATE_ALERTS, true)
        )
    }

    fun saveSettings(context: Context, settings: NotificationSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SUBMISSION_ALERTS, settings.submissionAlertsEnabled)
            .putBoolean(KEY_USER_APPROVAL_ALERTS, settings.userApprovalAlertsEnabled)
            .putBoolean(KEY_APP_UPDATE_ALERTS, settings.appUpdateAlertsEnabled)
            .apply()
    }

    fun loadSeenPublicKeys(context: Context, userId: String): Set<String> {
        if (userId.isBlank()) return emptySet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_SEEN_PUBLIC_PREFIX + userId, emptySet())?.toSet() ?: emptySet()
    }

    fun saveSeenPublicKeys(context: Context, userId: String, keys: Set<String>) {
        if (userId.isBlank()) return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_SEEN_PUBLIC_PREFIX + userId, keys.toSet())
            .apply()
    }
}

object NotificationCounter {

    fun publicNotificationKey(type: String, motorId: String, status: String): String {
        return "${type.trim().lowercase()}:${motorId.trim()}:${status.trim().lowercase()}"
    }

    fun countAdminUnread(
        singlePhaseMotors: List<SinglePhaseMotor>,
        threePhaseMotors: List<ThreePhaseMotor>,
        usersList: List<User>
    ): Int {
        val unseenSingle = singlePhaseMotors.count { it.isUnseen() }
        val unseenThree = threePhaseMotors.count { it.isUnseen() }
        val unseenUsers = usersList.count { it.isUnseen() }
        return unseenSingle + unseenThree + unseenUsers
    }

    fun countPublicUnread(
        userId: String,
        singlePhaseMotors: List<SinglePhaseMotor>,
        threePhaseMotors: List<ThreePhaseMotor>,
        seenKeys: Set<String>
    ): Int {
        if (userId.isBlank()) return 0
        val singleUpdates = singlePhaseMotors.count { motor ->
            motor.createdBy == userId &&
                (motor.isExplicitlyApproved() || motor.isRejected()) &&
                !seenKeys.contains(publicNotificationKey("single", motor.id, motor.status))
        }
        val threeUpdates = threePhaseMotors.count { motor ->
            motor.createdBy == userId &&
                (motor.isExplicitlyApproved() || motor.isRejected()) &&
                !seenKeys.contains(publicNotificationKey("three", motor.id, motor.status))
        }
        return singleUpdates + threeUpdates
    }
}
