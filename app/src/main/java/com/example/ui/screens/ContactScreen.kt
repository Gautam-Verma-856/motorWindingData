package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(navController: NavController) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "Admin",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = "Admin & Founder",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Gautam Verma",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phone,
                                    contentDescription = "Phone",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WhatsApp / Contact: 9098708284",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")

                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://wa.me/919098708284")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:9098708284")
                                        }
                                        context.startActivity(dialIntent)
                                    }
                                },
                                modifier = Modifier
                                    .scale(scale)
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                interactionSource = interactionSource
                            ) {
                                Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Contact on WhatsApp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
