import os

new_code = """package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.SolidColor
import com.example.BuildConfig

object ContactConfig {
    const val ADMIN_NAME = "Gautam Verma"
    const val ADMIN_PHONE = "9098708284"
    const val INSTAGRAM_URL = "https://instagram.com/" // Replace with actual Instagram URL
    const val APP_NAME = "Motor Winding Data"
    const val APP_DESC = "Motor winding data management and reference platform."
}

val InstagramIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Instagram",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.White),
            pathBuilder = {
                moveTo(12f, 2.163f)
                curveTo(15.204f, 2.163f, 15.584f, 2.175f, 16.85f, 2.233f)
                curveTo(20.102f, 2.381f, 21.621f, 3.924f, 21.769f, 7.152f)
                curveTo(21.827f, 8.417f, 21.838f, 8.797f, 21.838f, 12.001f)
                curveTo(21.838f, 15.206f, 21.826f, 15.585f, 21.769f, 16.85f)
                curveTo(21.62f, 20.075f, 20.105f, 21.622f, 16.85f, 21.77f)
                curveTo(15.584f, 21.828f, 15.206f, 21.84f, 12f, 21.84f)
                curveTo(8.796f, 21.84f, 8.416f, 21.828f, 7.151f, 21.77f)
                curveTo(3.891f, 21.621f, 2.38f, 20.071f, 2.232f, 16.85f)
                curveTo(2.174f, 15.585f, 2.162f, 15.206f, 2.162f, 12.001f)
                curveTo(2.162f, 8.797f, 2.175f, 8.418f, 2.232f, 7.152f)
                curveTo(2.381f, 3.925f, 3.896f, 2.381f, 7.151f, 2.233f)
                curveTo(8.417f, 2.176f, 8.796f, 2.164f, 12.001f, 2.164f)
                close()
                moveTo(12f, 0f)
                curveTo(8.741f, 0f, 8.333f, 0.014f, 7.053f, 0.072f)
                curveTo(2.695f, 0.272f, 0.273f, 2.69f, 0.073f, 7.052f)
                curveTo(0.014f, 8.333f, 0f, 8.741f, 0f, 12f)
                curveTo(0f, 15.259f, 0.014f, 15.668f, 0.072f, 16.948f)
                curveTo(0.272f, 21.306f, 2.69f, 23.728f, 7.052f, 23.928f)
                curveTo(8.333f, 23.986f, 8.741f, 24f, 12f, 24f)
                curveTo(15.259f, 24f, 15.668f, 23.986f, 16.948f, 23.928f)
                curveTo(21.302f, 23.728f, 23.73f, 21.31f, 23.927f, 16.948f)
                curveTo(23.986f, 15.668f, 24f, 15.259f, 24f, 12f)
                curveTo(24f, 8.741f, 23.986f, 8.333f, 23.928f, 7.053f)
                curveTo(23.732f, 2.699f, 21.311f, 0.273f, 16.949f, 0.073f)
                curveTo(15.668f, 0.014f, 15.259f, 0f, 12f, 0f)
                close()
                moveTo(12f, 5.838f)
                curveTo(8.597f, 5.838f, 5.838f, 8.597f, 5.838f, 12f)
                curveTo(5.838f, 15.403f, 8.597f, 18.162f, 12f, 18.162f)
                curveTo(15.403f, 18.162f, 18.162f, 15.403f, 18.162f, 12f)
                curveTo(18.162f, 8.597f, 15.403f, 5.838f, 12f, 5.838f)
                close()
                moveTo(12f, 16f)
                curveTo(9.791f, 16f, 8f, 14.209f, 8f, 12f)
                curveTo(8f, 9.791f, 9.791f, 8f, 12f, 8f)
                curveTo(14.209f, 8f, 16f, 9.791f, 16f, 12f)
                curveTo(16f, 14.209f, 14.209f, 16f, 12f, 16f)
                close()
                moveTo(18.406f, 4.155f)
                curveTo(17.61f, 4.155f, 16.966f, 4.799f, 16.966f, 5.595f)
                curveTo(16.966f, 6.391f, 17.61f, 7.035f, 18.406f, 7.035f)
                curveTo(19.202f, 7.035f, 19.846f, 6.391f, 19.846f, 5.595f)
                curveTo(19.846f, 4.799f, 19.202f, 4.155f, 18.406f, 4.155f)
                close()
            }
        )
    }.build()

val WhatsAppIcon: ImageVector
    get() = ImageVector.Builder(
        name = "WhatsApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.White),
            pathBuilder = {
                moveTo(17.472f, 14.382f)
                curveTo(17.175f, 14.233f, 15.714f, 13.515f, 15.442f, 13.415f)
                curveTo(15.169f, 13.316f, 14.971f, 13.267f, 14.772f, 13.565f)
                curveTo(14.575f, 13.862f, 14.005f, 14.531f, 13.832f, 14.729f)
                curveTo(13.659f, 14.928f, 13.485f, 14.952f, 13.188f, 14.804f)
                curveTo(12.891f, 14.654f, 11.933f, 14.341f, 10.798f, 13.329f)
                curveTo(9.915f, 12.541f, 9.32f, 11.568f, 9.147f, 11.27f)
                curveTo(8.974f, 10.973f, 9.129f, 10.812f, 9.277f, 10.664f)
                curveTo(9.411f, 10.531f, 9.575f, 10.317f, 9.723f, 10.144f)
                curveTo(9.872f, 9.97f, 9.921f, 9.846f, 10.021f, 9.647f)
                curveTo(10.12f, 9.449f, 10.071f, 9.276f, 9.996f, 9.127f)
                curveTo(9.921f, 8.978f, 9.327f, 7.515f, 9.08f, 6.92f)
                curveTo(8.838f, 6.341f, 8.593f, 6.42f, 8.411f, 6.41f)
                curveTo(8.238f, 6.402f, 8.04f, 6.4f, 7.841f, 6.4f)
                curveTo(7.643f, 6.4f, 7.321f, 6.474f, 7.049f, 6.772f)
                curveTo(6.777f, 7.069f, 6.009f, 7.788f, 6.009f, 9.251f)
                curveTo(6.009f, 10.713f, 7.074f, 12.126f, 7.222f, 12.325f)
                curveTo(7.371f, 12.523f, 9.318f, 15.525f, 12.299f, 16.812f)
                curveTo(13.008f, 17.118f, 13.561f, 17.301f, 13.993f, 17.437f)
                curveTo(14.705f, 17.664f, 15.353f, 17.632f, 15.864f, 17.555f)
                curveTo(16.435f, 17.47f, 17.622f, 16.836f, 17.87f, 16.142f)
                curveTo(18.118f, 15.448f, 18.118f, 14.853f, 18.043f, 14.729f)
                curveTo(17.969f, 14.605f, 17.771f, 14.531f, 17.473f, 14.382f)
                close()
                moveTo(12.051f, 21.785f)
                lineTo(12.047f, 21.785f)
                curveTo(10.366f, 21.785f, 8.761f, 21.332f, 7.33f, 20.528f)
                lineTo(6.969f, 20.314f)
                lineTo(3.228f, 21.296f)
                lineTo(4.226f, 17.648f)
                lineTo(3.991f, 17.274f)
                curveTo(3.111f, 15.875f, 2.639f, 14.223f, 2.639f, 12.495f)
                curveTo(2.64f, 7.045f, 7.075f, 2.611f, 12.527f, 2.611f)
                curveTo(15.167f, 2.611f, 17.649f, 3.641f, 19.515f, 5.509f)
                curveTo(21.381f, 7.377f, 22.411f, 9.859f, 22.408f, 12.499f)
                curveTo(22.405f, 17.949f, 17.971f, 22.383f, 12.523f, 22.383f)
                close()
                moveTo(20.464f, 4.086f)
                curveTo(18.22f, 1.842f, 15.228f, 0.606f, 12.05f, 0.606f)
                curveTo(5.495f, 0.606f, 0.16f, 5.941f, 0.157f, 12.498f)
                curveTo(0.157f, 14.594f, 0.704f, 16.64f, 1.745f, 18.443f)
                lineTo(0.057f, 24.606f)
                lineTo(6.362f, 22.952f)
                curveTo(8.118f, 23.9f, 10.054f, 24.394f, 12.043f, 24.394f)
                lineTo(12.048f, 24.394f)
                curveTo(18.602f, 24.394f, 23.938f, 19.059f, 23.941f, 12.501f)
                curveTo(23.943f, 9.324f, 22.709f, 6.332f, 20.464f, 4.086f)
                close()
            }
        )
    }.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(navController: NavController) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Us", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connect with the Motor Winding Data team",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Profile Header Card
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "Admin",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "ADMIN & FOUNDER",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ContactConfig.ADMIN_NAME,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Social Contact Section
                    Text(
                        text = "Connect With Us",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 12.dp, start = 4.dp)
                    )

                    SocialContactCard(
                        title = "WhatsApp",
                        subtitle = "Chat with us directly",
                        icon = WhatsAppIcon,
                        iconBgColor = Color(0xFF25D366),
                        onClick = {
                            openWhatsApp(context, ContactConfig.ADMIN_PHONE)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    SocialContactCard(
                        title = "Instagram",
                        subtitle = "Follow us for updates",
                        icon = InstagramIcon,
                        iconBgColor = Color(0xFFE1306C),
                        onClick = {
                            openUrl(context, ContactConfig.INSTAGRAM_URL)
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // App Information Section
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = ContactConfig.APP_NAME,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ContactConfig.APP_DESC,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                val version = try { BuildConfig.VERSION_NAME } catch (e: Exception) { "1.0.0" }
                                Text(
                                    text = "Version $version",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(32.dp))

                    // Footer / Copyright
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Built for motor winding professionals",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "© 2026 Motor Winding Data. All rights reserved.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SocialContactCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "scale")

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconBgColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowForwardIos,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun openWhatsApp(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/91$phone")
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback to browser or generic intent if WhatsApp is not installed
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/91$phone")
            }
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            context.startActivity(dialIntent)
            Toast.makeText(context, "Opening dialer", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

private fun openUrl(context: Context, url: String) {
    if (url.isBlank()) {
        Toast.makeText(context, "Link not available", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "w") as f:
    f.write(new_code)
