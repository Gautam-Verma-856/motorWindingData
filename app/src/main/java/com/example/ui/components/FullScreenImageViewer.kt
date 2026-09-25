package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch

@Composable
fun FullScreenImageViewer(
    photos: List<String>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    if (photos.isEmpty()) return

    val validInitialIndex = initialIndex.coerceIn(0, photos.lastIndex)
    val pagerState = rememberPagerState(initialPage = validInitialIndex, pageCount = { photos.size })
    var areControlsVisible by remember { mutableStateOf(true) }
    var isCurrentZoomed by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Horizontal Pager for multiple photos
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isCurrentZoomed,
                key = { page -> if (page in photos.indices) photos[page] else page.toString() }
            ) { page ->
                val isActivePage = page == pagerState.currentPage
                ZoomableImagePage(
                    url = photos[page],
                    isActivePage = isActivePage,
                    resetTrigger = if (isActivePage) resetTrigger else 0,
                    onZoomChanged = { zoomed ->
                        if (isActivePage) {
                            isCurrentZoomed = zoomed
                        }
                    },
                    onToggleControls = {
                        areControlsVisible = !areControlsVisible
                    }
                )
            }

            // Top Bar Controls (Close button, photo counter, reset zoom button)
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Close / Back button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Viewer",
                                tint = Color.White
                            )
                        }
                    }

                    // Photo Counter Pill
                    if (photos.size > 1) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${photos.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Reset Zoom Button (visible when zoomed in)
                    if (isCurrentZoomed) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            IconButton(
                                onClick = { resetTrigger++ },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = "Reset Zoom",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }

            // Bottom Indicators (Reset hint and Dots indicator if multiple photos)
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isCurrentZoomed) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.clickable { resetTrigger++ }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RestartAlt,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Tap to reset zoom (1x)",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    if (photos.size > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(photos.size) { pageIndex ->
                                val isSelected = pageIndex == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .size(if (isSelected) 10.dp else 6.dp)
                                        .background(
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pageIndex)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableImagePage(
    url: String,
    isActivePage: Boolean,
    resetTrigger: Int,
    onZoomChanged: (Boolean) -> Unit,
    onToggleControls: () -> Unit
) {
    val scaleAnimatable = remember { Animatable(1f) }
    val offsetXAnimatable = remember { Animatable(0f) }
    val offsetYAnimatable = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val isZoomed by remember { derivedStateOf { scaleAnimatable.value > 1.05f } }
    LaunchedEffect(isZoomed) {
        onZoomChanged(isZoomed)
    }

    // Reset when page is switched away
    LaunchedEffect(isActivePage) {
        if (!isActivePage && scaleAnimatable.value != 1f) {
            scaleAnimatable.snapTo(1f)
            offsetXAnimatable.snapTo(0f)
            offsetYAnimatable.snapTo(0f)
            onZoomChanged(false)
        }
    }

    // Reset when resetTrigger triggered
    LaunchedEffect(resetTrigger) {
        if (resetTrigger > 0 && scaleAnimatable.value != 1f) {
            scaleAnimatable.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
            offsetXAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            offsetYAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Motor Photo Full View",
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 3.dp
                    )
                }
            },
            error = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BrokenImage,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Failed to load photo",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scaleAnimatable.value
                    scaleY = scaleAnimatable.value
                    translationX = offsetXAnimatable.value
                    translationY = offsetYAnimatable.value
                }
                .pointerInput(containerWidthPx, containerHeightPx) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            coroutineScope.launch {
                                if (scaleAnimatable.value > 1.05f) {
                                    scaleAnimatable.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                                    offsetXAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    offsetYAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                } else {
                                    val targetScale = 2.5f
                                    val center = Offset(containerWidthPx / 2f, containerHeightPx / 2f)
                                    val targetOffset = (center - tapOffset) * (targetScale - 1f)
                                    val maxX = (containerWidthPx * (targetScale - 1f)) / 2f
                                    val maxY = (containerHeightPx * (targetScale - 1f)) / 2f

                                    scaleAnimatable.animateTo(targetScale, spring(stiffness = Spring.StiffnessMediumLow))
                                    offsetXAnimatable.animateTo(
                                        targetOffset.x.coerceIn(-maxX, maxX),
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                    offsetYAnimatable.animateTo(
                                        targetOffset.y.coerceIn(-maxY, maxY),
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                }
                            }
                        },
                        onTap = {
                            onToggleControls()
                        }
                    )
                }
                .pointerInput(containerWidthPx, containerHeightPx) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled) {
                                val pointerCount = event.changes.size
                                val currentScale = scaleAnimatable.value

                                if (pointerCount > 1) {
                                    // Pinch-to-zoom with 2 fingers
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val newScale = (currentScale * zoomChange).coerceIn(0.75f, 5f)
                                    val maxX = if (newScale > 1f) (containerWidthPx * (newScale - 1f)) / 2f else 0f
                                    val maxY = if (newScale > 1f) (containerHeightPx * (newScale - 1f)) / 2f else 0f

                                    coroutineScope.launch {
                                        scaleAnimatable.snapTo(newScale)
                                        val newOffsetX = (offsetXAnimatable.value + panChange.x).coerceIn(-maxX, maxX)
                                        val newOffsetY = (offsetYAnimatable.value + panChange.y).coerceIn(-maxY, maxY)
                                        offsetXAnimatable.snapTo(newOffsetX)
                                        offsetYAnimatable.snapTo(newOffsetY)
                                    }
                                    event.changes.forEach { it.consume() }
                                } else if (pointerCount == 1 && currentScale > 1.05f) {
                                    // Pan / drag when zoomed in
                                    val panChange = event.calculatePan()
                                    val maxX = (containerWidthPx * (currentScale - 1f)) / 2f
                                    val maxY = (containerHeightPx * (currentScale - 1f)) / 2f

                                    coroutineScope.launch {
                                        val newOffsetX = (offsetXAnimatable.value + panChange.x).coerceIn(-maxX, maxX)
                                        val newOffsetY = (offsetYAnimatable.value + panChange.y).coerceIn(-maxY, maxY)
                                        offsetXAnimatable.snapTo(newOffsetX)
                                        offsetYAnimatable.snapTo(newOffsetY)
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                                // When pointerCount == 1 and currentScale <= 1.05f:
                                // Not consumed, so parent HorizontalPager handles the swipe!
                            }
                        } while (event.changes.any { it.pressed })

                        // When user lifts fingers after pinching below 1f, spring back to 1f
                        if (scaleAnimatable.value < 1f) {
                            coroutineScope.launch {
                                scaleAnimatable.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                                offsetXAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                offsetYAnimatable.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    }
                }
        )
    }
}
