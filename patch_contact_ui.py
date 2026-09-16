import sys

new_content = """package com.example.ui.screens

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.path
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.BuildConfig

object ContactConfig {
    const val ADMIN_NAME = "Gautam Verma"
    const val ADMIN_PHONE = "9098708284"
    const val INSTAGRAM_URL = "https://www.instagram.com/gautam_verma_856?stkn=MTIzdncyMXUybmM5Zw=="
}

val WhatsAppIcon: ImageVector
    get() = ImageVector.Builder(
        name = "WhatsApp", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
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
            curveTo(10.12f, 9.449f, 10.22f, 9.301f, 10.418f, 9.102f)
            curveTo(10.616f, 8.904f, 10.814f, 8.904f, 10.914f, 8.904f)
            curveTo(11.013f, 8.904f, 11.112f, 8.904f, 11.162f, 8.929f)
            curveTo(11.285f, 8.954f, 11.459f, 9.351f, 11.483f, 9.401f)
            curveTo(11.533f, 9.5f, 11.632f, 9.723f, 11.756f, 10.021f)
            curveTo(11.855f, 10.269f, 11.979f, 10.517f, 12.004f, 10.591f)
            curveTo(12.053f, 10.74f, 12.029f, 10.888f, 11.954f, 11.012f)
            curveTo(11.88f, 11.136f, 11.806f, 11.211f, 11.706f, 11.335f)
            curveTo(11.607f, 11.459f, 11.483f, 11.607f, 11.409f, 11.682f)
            curveTo(11.285f, 11.83f, 11.161f, 11.979f, 11.31f, 12.252f)
            curveTo(11.458f, 12.525f, 11.979f, 13.368f, 12.748f, 14.038f)
            curveTo(13.739f, 14.906f, 14.507f, 15.178f, 14.78f, 15.277f)
            curveTo(15.028f, 15.376f, 15.201f, 15.352f, 15.375f, 15.178f)
            curveTo(15.548f, 15.005f, 16.094f, 14.36f, 16.292f, 14.062f)
            curveTo(16.49f, 13.765f, 16.689f, 13.814f, 16.961f, 13.914f)
            curveTo(17.234f, 14.013f, 18.919f, 14.831f, 19.266f, 15.005f)
            curveTo(19.613f, 15.178f, 19.836f, 15.277f, 19.935f, 15.426f)
            curveTo(20.034f, 15.575f, 20.034f, 16.22f, 19.737f, 16.964f)
            curveTo(19.439f, 17.707f, 17.977f, 18.352f, 17.283f, 18.427f)
            curveTo(16.738f, 18.476f, 15.895f, 18.476f, 14.507f, 18.005f)
            curveTo(12.748f, 17.41f, 10.418f, 15.823f, 8.683f, 13.592f)
            curveTo(8.583f, 13.443f, 6.774f, 11.063f, 6.774f, 8.584f)
            curveTo(6.774f, 6.105f, 8.038f, 4.915f, 8.459f, 4.469f)
            curveTo(8.831f, 4.072f, 9.401f, 3.849f, 9.946f, 3.849f)
            curveTo(10.12f, 3.849f, 10.268f, 3.874f, 10.392f, 3.874f)
            curveTo(10.665f, 3.899f, 10.789f, 3.899f, 10.962f, 4.345f)
            curveTo(11.16f, 4.841f, 11.656f, 6.055f, 11.706f, 6.179f)
            curveTo(11.78f, 6.303f, 11.83f, 6.477f, 11.731f, 6.65f)
            curveTo(11.632f, 6.824f, 11.557f, 6.873f, 11.409f, 7.047f)
            curveTo(11.26f, 7.22f, 11.112f, 7.419f, 10.988f, 7.543f)
            curveTo(10.839f, 7.691f, 10.69f, 7.865f, 10.888f, 8.212f)
            curveTo(11.087f, 8.559f, 11.656f, 9.476f, 12.524f, 10.244f)
            curveTo(13.639f, 11.235f, 14.507f, 11.533f, 14.829f, 11.682f)
            curveTo(15.151f, 11.83f, 15.35f, 11.806f, 15.523f, 11.632f)
            curveTo(15.697f, 11.459f, 16.341f, 10.665f, 16.589f, 10.269f)
            curveTo(16.837f, 9.872f, 17.085f, 9.946f, 17.407f, 10.07f)
            curveTo(17.729f, 10.194f, 19.414f, 11.012f, 19.563f, 11.086f)
            curveTo(19.712f, 11.161f, 19.811f, 11.185f, 19.836f, 11.334f)
            curveTo(19.885f, 11.483f, 19.885f, 12.152f, 19.588f, 13.045f)
            close()
            moveTo(12.004f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.004f)
            curveTo(2.0f, 13.764f, 2.458f, 15.425f, 3.264f, 16.862f)
            lineTo(2.0f, 21.464f)
            lineTo(6.711f, 20.225f)
            curveTo(8.074f, 21.192f, 9.771f, 21.761f, 11.583f, 21.761f)
            curveTo(17.107f, 21.761f, 21.587f, 17.281f, 21.587f, 11.757f)
            curveTo(21.587f, 6.233f, 17.107f, 1.753f, 11.583f, 1.753f)
            close()
        }
    }

val InstagramIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Instagram", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12.0f, 2.163f)
            curveTo(15.204f, 2.163f, 15.584f, 2.175f, 16.85f, 2.233f)
            curveTo(18.026f, 2.287f, 18.623f, 2.47f, 19.018f, 2.624f)
            curveTo(19.54f, 2.827f, 19.912f, 3.076f, 20.305f, 3.469f)
            curveTo(20.697f, 3.861f, 20.947f, 4.234f, 21.149f, 4.757f)
            curveTo(21.304f, 5.152f, 21.487f, 5.748f, 21.541f, 6.924f)
            curveTo(21.599f, 8.19f, 21.611f, 8.57f, 21.611f, 12.0f)
            curveTo(21.611f, 15.43f, 21.599f, 15.81f, 21.541f, 17.076f)
            curveTo(21.487f, 18.252f, 21.304f, 18.848f, 21.149f, 19.243f)
            curveTo(20.946f, 19.765f, 20.697f, 20.138f, 20.305f, 20.531f)
            curveTo(19.912f, 20.923f, 19.539f, 21.173f, 19.018f, 21.376f)
            curveTo(18.623f, 21.53f, 18.026f, 21.713f, 16.85f, 21.767f)
            curveTo(15.584f, 21.825f, 15.204f, 21.837f, 12.0f, 21.837f)
            curveTo(8.796f, 21.837f, 8.416f, 21.825f, 7.15f, 21.767f)
            curveTo(5.974f, 21.713f, 5.377f, 21.53f, 4.982f, 21.376f)
            curveTo(4.46f, 21.173f, 4.088f, 20.924f, 3.695f, 20.531f)
            curveTo(3.303f, 20.138f, 3.053f, 19.765f, 2.851f, 19.243f)
            curveTo(2.696f, 18.848f, 2.513f, 18.252f, 2.459f, 17.076f)
            curveTo(2.401f, 15.81f, 2.389f, 15.43f, 2.389f, 12.0f)
            curveTo(2.389f, 8.57f, 2.401f, 8.19f, 2.459f, 6.924f)
            curveTo(2.513f, 5.748f, 2.696f, 5.152f, 2.851f, 4.757f)
            curveTo(3.054f, 4.234f, 3.303f, 3.861f, 3.695f, 3.469f)
            curveTo(4.088f, 3.076f, 4.46f, 2.827f, 4.982f, 2.624f)
            curveTo(5.377f, 2.47f, 5.974f, 2.287f, 7.15f, 2.233f)
            curveTo(8.416f, 2.175f, 8.796f, 2.163f, 12.0f, 2.163f)
            moveTo(12.0f, 0.0f)
            curveTo(8.741f, 0.0f, 8.333f, 0.014f, 7.053f, 0.072f)
            curveTo(5.775f, 0.13f, 4.902f, 0.334f, 4.14f, 0.63f)
            curveTo(3.356f, 0.935f, 2.694f, 1.336f, 2.032f, 1.998f)
            curveTo(1.37f, 2.66f, 0.969f, 3.322f, 0.665f, 4.106f)
            curveTo(0.369f, 4.868f, 0.165f, 5.741f, 0.107f, 7.019f)
            curveTo(0.049f, 8.299f, 0.035f, 8.707f, 0.035f, 11.966f)
            curveTo(0.035f, 15.225f, 0.049f, 15.633f, 0.107f, 16.913f)
            curveTo(0.165f, 18.191f, 0.369f, 19.064f, 0.665f, 19.826f)
            curveTo(0.969f, 20.61f, 1.37f, 21.272f, 2.032f, 21.934f)
            curveTo(2.694f, 22.596f, 3.356f, 22.997f, 4.14f, 23.302f)
            curveTo(4.902f, 23.598f, 5.775f, 23.802f, 7.053f, 23.86f)
            curveTo(8.333f, 23.918f, 8.741f, 23.932f, 12.0f, 23.932f)
            curveTo(15.259f, 23.932f, 15.667f, 23.918f, 16.947f, 23.86f)
            curveTo(18.225f, 23.802f, 19.098f, 23.598f, 19.86f, 23.302f)
            curveTo(20.644f, 22.997f, 21.306f, 22.596f, 21.968f, 21.934f)
            curveTo(22.63f, 21.272f, 23.031f, 20.61f, 23.335f, 19.826f)
            curveTo(23.631f, 19.064f, 23.835f, 18.191f, 23.893f, 16.913f)
            curveTo(23.951f, 15.633f, 23.965f, 15.225f, 23.965f, 11.966f)
            curveTo(23.965f, 8.707f, 23.951f, 8.299f, 23.893f, 7.019f)
            curveTo(23.835f, 5.741f, 23.631f, 4.868f, 23.335f, 4.106f)
            curveTo(23.031f, 3.322f, 22.63f, 2.66f, 21.968f, 1.998f)
            curveTo(21.306f, 1.336f, 20.644f, 0.935f, 19.86f, 0.63f)
            curveTo(19.098f, 0.334f, 18.225f, 0.13f, 16.947f, 0.072f)
            curveTo(15.667f, 0.014f, 15.259f, 0.0f, 12.0f, 0.0f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(12.0f, 5.838f)
            curveTo(8.6f, 5.838f, 5.838f, 8.6f, 5.838f, 12.0f)
            curveTo(5.838f, 15.4f, 8.6f, 18.162f, 12.0f, 18.162f)
            curveTo(15.4f, 18.162f, 18.162f, 15.4f, 18.162f, 12.0f)
            curveTo(18.162f, 8.6f, 15.4f, 5.838f, 12.0f, 5.838f)
            close()
            moveTo(12.0f, 15.999f)
            curveTo(9.791f, 15.999f, 8.0f, 14.208f, 8.0f, 11.999f)
            curveTo(8.0f, 9.79f, 9.791f, 7.999f, 12.0f, 7.999f)
            curveTo(14.209f, 7.999f, 16.0f, 9.79f, 16.0f, 11.999f)
            curveTo(16.0f, 14.208f, 14.209f, 15.999f, 12.0f, 15.999f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(18.406f, 7.034f)
            curveTo(19.201f, 7.034f, 19.845f, 6.39f, 19.845f, 5.595f)
            curveTo(19.845f, 4.8f, 19.201f, 4.156f, 18.406f, 4.156f)
            curveTo(17.611f, 4.156f, 16.967f, 4.8f, 16.967f, 5.595f)
            curveTo(16.967f, 6.39f, 17.611f, 7.034f, 18.406f, 7.034f)
            close()
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(navController: NavController) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Info", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 6 }, animationSpec = tween(500))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // TOP AREA: Circular profile
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                ),
                                CircleShape
                            )
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Admin Avatar",
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title & Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ContactConfig.ADMIN_NAME,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified Admin",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "Admin & Founder",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // CONTACT OPTIONS (WhatsApp, Instagram, Phone)
                    
                    ModernActionCard(
                        title = "WhatsApp",
                        subtitle = ContactConfig.ADMIN_PHONE,
                        icon = WhatsAppIcon,
                        iconBgColor = Color(0xFF25D366),
                        delayIndex = 0,
                        isVisible = isVisible,
                        onClick = { openWhatsApp(context, ContactConfig.ADMIN_PHONE) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ModernActionCard(
                        title = "Instagram",
                        subtitle = "@gautam_verma_856",
                        icon = InstagramIcon,
                        iconBgColor = Color(0xFFE1306C),
                        delayIndex = 1,
                        isVisible = isVisible,
                        onClick = { openUrl(context, ContactConfig.INSTAGRAM_URL) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ModernActionCard(
                        title = "Contact",
                        subtitle = ContactConfig.ADMIN_PHONE,
                        icon = Icons.Rounded.Phone,
                        iconBgColor = MaterialTheme.colorScheme.primary,
                        delayIndex = 2,
                        isVisible = isVisible,
                        onClick = { openDialer(context, ContactConfig.ADMIN_PHONE) }
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // BOTTOM AREA: Connect with us
                    Text(
                        text = "Connect with us",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "For inquiries, support or feedback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ModernActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    delayIndex: Int,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )

    var cardVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(100L + (delayIndex * 100L))
            cardVisible = true
        }
    }

    AnimatedVisibility(
        visible = cardVisible,
        enter = fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(400))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .graphicsLayer {
                    shadowElevation = if (isPressed) 2.dp.toPx() else 10.dp.toPx()
                    shape = RoundedCornerShape(24.dp)
                    clip = true
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                iconBgColor.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(iconBgColor, CircleShape)
                        .graphicsLayer {
                            shadowElevation = 6.dp.toPx()
                            shape = CircleShape
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Text Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action arrow
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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
        try {
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/91$phone")
            }
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            openDialer(context, phone)
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

private fun openDialer(context: Context, phone: String) {
    try {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        context.startActivity(dialIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/ContactScreen.kt", "w") as f:
    f.write(new_content)
print("Updated successfully")
