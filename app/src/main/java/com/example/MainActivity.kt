package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.navigation.NavGraph
import com.example.ui.components.AppUpdateWrapper
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
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
                            NavGraph()
                        }
                    }
                }
            }
        }
    }
}
