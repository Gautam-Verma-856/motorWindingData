package com.example.model

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "motor_winding_theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val MODE_DARK = "dark"
    private const val MODE_LIGHT = "light"

    fun loadDarkThemeOverride(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getString(KEY_THEME_MODE, null)) {
            MODE_DARK -> true
            MODE_LIGHT -> false
            else -> null
        }
    }

    fun saveDarkThemeOverride(context: Context, isDark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, if (isDark) MODE_DARK else MODE_LIGHT)
            .apply()
    }

    fun clearThemeOverride(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_THEME_MODE)
            .apply()
    }
}
