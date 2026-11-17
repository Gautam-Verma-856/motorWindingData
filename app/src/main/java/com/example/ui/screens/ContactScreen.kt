package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

object ContactConfig {
    const val ADMIN_NAME = "Gautam Verma"
    const val INSTAGRAM_HANDLE = "@gautam_verma_856"
    const val INSTAGRAM_URL = "https://www.instagram.com/gautam_verma_856?stkn=MTIzdncyMXUybmM5Zw=="
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
    }.build()

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
                title = { Text("Contact", fontWeight = FontWeight.SemiBold) },
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

                    // Instagram-only contact card using existing configured Instagram ID
                    ModernActionCard(
                        title = "Instagram",
                        subtitle = ContactConfig.INSTAGRAM_HANDLE,
                        icon = InstagramIcon,
                        iconBgColor = Color(0xFFE1306C),
                        delayIndex = 0,
                        isVisible = isVisible,
                        modifier = Modifier.testTag("contact_instagram_card"),
                        onClick = { openInstagramProfile(context, ContactConfig.INSTAGRAM_URL) }
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Connect on Instagram",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tap the Instagram card above to visit our profile.",
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
    modifier: Modifier = Modifier,
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
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
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
                                iconBgColor.copy(alpha = 0.06f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFF58529),
                                    Color(0xFFDD2A7B),
                                    Color(0xFF8134AF)
                                )
                            ),
                            CircleShape
                        )
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

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Open Instagram",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun openInstagramProfile(context: Context, url: String) {
    if (url.isBlank()) {
        Toast.makeText(context, "Instagram link not available", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.instagram.android")
        }
        context.startActivity(appIntent)
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open Instagram profile", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open Instagram profile", Toast.LENGTH_SHORT).show()
    }
}
