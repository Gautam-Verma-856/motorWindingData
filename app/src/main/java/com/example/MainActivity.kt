package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.model.ThemePreferences
import com.example.ui.components.AppUpdateWrapper
import com.example.ui.navigation.NavGraph
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkThemeOverride by remember {
                mutableStateOf(ThemePreferences.loadDarkThemeOverride(this))
            }
            val isDarkTheme = darkThemeOverride ?: systemDark
            val onToggleTheme: () -> Unit = {
                val nextDark = !isDarkTheme
                darkThemeOverride = nextDark
                ThemePreferences.saveDarkThemeOverride(this, nextDark)
            }

            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? Activity)?.window
                    if (window != null) {
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        insetsController.isAppearanceLightStatusBars = !isDarkTheme
                        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                    }
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val firebaseApps = FirebaseApp.getApps(this)
                    android.util.Log.d("MainActivity", "Firebase apps count: ${firebaseApps.size}")
                    if (firebaseApps.isEmpty()) {
                        android.util.Log.e("MainActivity", "Firebase is not initialized!")
                        android.widget.Toast.makeText(this, "Firebase is NOT initialized!", android.widget.Toast.LENGTH_LONG).show()
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Firebase is not initialized.",
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Please add your google-services.json file to the app/ directory using the file explorer, then rebuild the app to use the cloud database features.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    } else {
                        android.util.Log.d("MainActivity", "Firebase is initialized. Rendering AppUpdateWrapper.")
                        android.widget.Toast.makeText(this, "Firebase OK. Starting AppUpdateWrapper.", android.widget.Toast.LENGTH_SHORT).show()
                        AppUpdateWrapper {
                            NavGraph(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = onToggleTheme
                            )
                        }
                    }
                }
            }
        }
    }
}
