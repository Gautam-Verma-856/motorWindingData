package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.MotorConnectionOptions
import com.example.model.MotorSearchEngine
import com.example.model.MotorSearchFilter
import com.example.model.MotorSearchItem
import com.example.model.NumericFilterSpec
import com.example.model.SearchApprovalStatus
import com.example.model.SearchMotorType
import com.example.model.SearchRecentlyAdded
import com.example.ui.components.StatusBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedFilterPanel(
    draftFilter: MotorSearchFilter,
    isAdmin: Boolean,
    onUpdateFilter: ((MotorSearchFilter) -> MotorSearchFilter) -> Unit,
    onApplyFilters: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("advanced_filter_panel"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Motor Type
            FilterSectionLabel("Motor Type")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchMotorType.entries.forEach { typeOption ->
                    val tag = when (typeOption) {
                        SearchMotorType.ALL -> "filter_type_all"
                        SearchMotorType.SINGLE_PHASE -> "filter_type_single"
                        SearchMotorType.THREE_PHASE -> "filter_type_three"
                    }
                    FilterChip(
                        selected = draftFilter.motorType == typeOption,
                        onClick = { onUpdateFilter { it.copy(motorType = typeOption) } },
                        label = { Text(typeOption.label, fontWeight = if (draftFilter.motorType == typeOption) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag(tag)
                    )
                }
            }

            // Motor Category
            FilterSectionLabel("Motor Category")
            val availableCategories = remember(draftFilter.motorType) {
                MotorSearchEngine.categoriesForMotorType(draftFilter.motorType)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { cat ->
                    val slug = cat.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                    FilterChip(
                        selected = draftFilter.category.equals(cat, ignoreCase = true),
                        onClick = { onUpdateFilter { it.copy(category = cat) } },
                        label = { Text(cat, maxLines = 1) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_category_$slug")
                    )
                }
            }

            // Numeric Filters (HP, RPM, Amps, Slot)
            FilterSectionLabel("Electrical & Mechanical Specs")
            NumericSpecInputBlock(
                title = "HP",
                unitPlaceholder = "e.g. 1.5",
                spec = draftFilter.hp,
                keyboardType = KeyboardType.Decimal,
                tagPrefix = "hp",
                onSpecChange = { updated -> onUpdateFilter { it.copy(hp = updated) } }
            )

            NumericSpecInputBlock(
                title = "RPM",
                unitPlaceholder = "e.g. 1440",
                spec = draftFilter.rpm,
                keyboardType = KeyboardType.Number,
                tagPrefix = "rpm",
                onSpecChange = { updated -> onUpdateFilter { it.copy(rpm = updated) } }
            )

            NumericSpecInputBlock(
                title = "Amps",
                unitPlaceholder = "e.g. 4.5",
                spec = draftFilter.amps,
                keyboardType = KeyboardType.Decimal,
                tagPrefix = "amps",
                onSpecChange = { updated -> onUpdateFilter { it.copy(amps = updated) } }
            )

            AnimatedVisibility(
                visible = draftFilter.motorType != SearchMotorType.SINGLE_PHASE,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                NumericSpecInputBlock(
                    title = "Slot (3 Phase)",
                    unitPlaceholder = "e.g. 24",
                    spec = draftFilter.slot,
                    keyboardType = KeyboardType.Number,
                    tagPrefix = "slot",
                    onSpecChange = { updated -> onUpdateFilter { it.copy(slot = updated) } }
                )
            }

            // Wire & Coil Inputs
            FilterSectionLabel("Winding Details")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = draftFilter.swg,
                    onValueChange = { value -> onUpdateFilter { it.copy(swg = value) } },
                    label = { Text("SWG / Wire Size") },
                    placeholder = { Text("e.g. 21") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("swg_filter_input")
                )
                OutlinedTextField(
                    value = draftFilter.coilSize,
                    onValueChange = { value -> onUpdateFilter { it.copy(coilSize = value) } },
                    label = { Text("Coil Size") },
                    placeholder = { Text("e.g. 22 SWG") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("coil_size_filter_input")
                )
            }

            // Connection
            FilterSectionLabel("Connection")
            val connectionOptions = listOf("All") + MotorConnectionOptions.DROPDOWN_OPTIONS
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                connectionOptions.forEach { conn ->
                    val slug = conn.lowercase().replace("-", "_").replace(" ", "_")
                    FilterChip(
                        selected = draftFilter.connection == conn,
                        onClick = { onUpdateFilter { it.copy(connection = conn) } },
                        label = { Text(conn) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_connection_$slug")
                    )
                }
            }

            // Recently Added
            FilterSectionLabel("Recently Added")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchRecentlyAdded.entries.forEach { preset ->
                    val slug = preset.name.lowercase()
                    FilterChip(
                        selected = draftFilter.recentlyAdded == preset,
                        onClick = { onUpdateFilter { it.copy(recentlyAdded = preset) } },
                        label = { Text(preset.label) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_recent_$slug")
                    )
                }
            }

            // Approval Status (STRICTLY ADMIN ONLY)
            if (isAdmin) {
                FilterSectionLabel("Approval Status (Admin)")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchApprovalStatus.entries.forEach { statusOpt ->
                        val slug = statusOpt.name.lowercase()
                        FilterChip(
                            selected = draftFilter.approvalStatus == statusOpt,
                            onClick = { onUpdateFilter { it.copy(approvalStatus = statusOpt) } },
                            label = { Text(statusOpt.label) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_status_$slug")
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Apply Filters & Clear All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("clear_filters_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("apply_filters_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.FilterAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun NumericSpecInputBlock(
    title: String,
    unitPlaceholder: String,
    spec: NumericFilterSpec,
    keyboardType: KeyboardType,
    tagPrefix: String,
    onSpecChange: (NumericFilterSpec) -> Unit
) {
    var isRangeMode by remember(tagPrefix, spec.min, spec.max, spec.exact) {
        mutableStateOf(spec.min.isNotBlank() || spec.max.isNotBlank())
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !isRangeMode,
                        onClick = {
                            isRangeMode = false
                            if (spec.min.isNotBlank() || spec.max.isNotBlank()) {
                                onSpecChange(spec.copy(min = "", max = ""))
                            }
                        },
                        label = { Text("Exact", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("${tagPrefix}_mode_exact")
                    )
                    FilterChip(
                        selected = isRangeMode,
                        onClick = {
                            isRangeMode = true
                            if (spec.exact.isNotBlank()) {
                                onSpecChange(spec.copy(exact = ""))
                            }
                        },
                        label = { Text("Range", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("${tagPrefix}_mode_range")
                    )
                }
            }

            if (!isRangeMode) {
                OutlinedTextField(
                    value = spec.exact,
                    onValueChange = { onSpecChange(NumericFilterSpec(exact = it)) },
                    label = { Text("Exact $title") },
                    placeholder = { Text(unitPlaceholder) },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${tagPrefix}_exact_input")
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = spec.min,
                        onValueChange = { onSpecChange(NumericFilterSpec(min = it, max = spec.max)) },
                        label = { Text("Min $title") },
                        placeholder = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${tagPrefix}_min_input")
                    )
                    OutlinedTextField(
                        value = spec.max,
                        onValueChange = { onSpecChange(NumericFilterSpec(min = spec.min, max = it)) },
                        label = { Text("Max $title") },
                        placeholder = { Text("Max") },
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${tagPrefix}_max_input")
                    )
                }
            }
        }
    }
}

@Composable
fun SearchMotorResultCard(
    item: MotorSearchItem,
    isAdmin: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPhotoClick: (List<String>, Int) -> Unit
) {
    val isSingle = item is MotorSearchItem.SinglePhase
    val typeContainerColor = if (isSingle) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val typeContentColor = if (isSingle) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("search_result_card_${item.typeKey}_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Name + Type + Status (Admin Only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName.ifBlank { "Unnamed Motor" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = typeContainerColor
                        ) {
                            Text(
                                text = item.typeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = typeContentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (item.category.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
                if (isAdmin) {
                    StatusBadge(
                        status = item.status,
                        modifier = Modifier.testTag("search_result_status_${item.typeKey}_${item.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Photo + Key Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.photoUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(item.photoUrls, 0) }
                    ) {
                        AsyncImage(
                            model = item.photoUrls.first(),
                            contentDescription = "Motor Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (item.photoUrls.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = "+${item.photoUrls.size - 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isSingle) {
                                        listOf(Color(0xFF1E3A8A).copy(alpha = 0.14f), Color(0xFF06B6D4).copy(alpha = 0.14f))
                                    } else {
                                        listOf(Color(0xFF06B6D4).copy(alpha = 0.16f), Color(0xFF10B981).copy(alpha = 0.14f))
                                    }
                                ),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSingle) Icons.Rounded.FlashOn else Icons.Rounded.Build,
                            contentDescription = null,
                            tint = if (isSingle) MaterialTheme.colorScheme.primary else Color(0xFF0891B2),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip("HP", item.hp.ifBlank { "-" })
                        InfoChip("RPM", item.rpm.ifBlank { "-" })
                        InfoChip("Amps", item.amps.ifBlank { "-" })
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.slot.isNotBlank()) InfoChip("Slot", item.slot)
                        if (item.connection.isNotBlank()) InfoChip("Conn", item.connection)
                        if (item.swgSummary.isNotBlank()) InfoChip("SWG", item.swgSummary)
                        if (item.coilSize.isNotBlank()) InfoChip("Coil", item.coilSize)
                    }
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isPending) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = onApprove,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                    contentColor = Color(0xFF047857)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = onReject,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else if (item.isRejected) {
                        FilledTonalButton(
                            onClick = onApprove,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                contentColor = Color(0xFF047857)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Approve", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Motor", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete Motor", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = onView, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "View Details", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchLoadingSkeletonList(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "search_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("search_loading_state"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Searching motor database...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        repeat(3) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
            ) {}
        }
    }
}
