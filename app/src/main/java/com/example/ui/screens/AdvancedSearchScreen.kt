package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.model.MotorSearchFilter
import com.example.model.SearchApprovalStatus
import com.example.model.SearchMotorType
import com.example.model.SearchRecentlyAdded
import com.example.model.SearchSortOption
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ErrorDialog
import com.example.ui.components.FullScreenImageViewer
import com.example.ui.components.LoadingDialog
import com.example.ui.components.RejectionReasonDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdvancedSearchScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    role: String
) {
    val isAdmin = role.trim().equals("admin", ignoreCase = true)
    val focusManager = LocalFocusManager.current

    val draftFilter by motorViewModel.draftSearchFilter.collectAsState()
    val appliedFilter by motorViewModel.appliedSearchFilter.collectAsState()
    val results by motorViewModel.searchResults.collectAsState()
    val isLoading by motorViewModel.isSearchLoading.collectAsState()
    val isLoadingMore by motorViewModel.isSearchLoadingMore.collectAsState()
    val searchError by motorViewModel.searchError.collectAsState()
    val hasMore by motorViewModel.hasMoreSearchResults.collectAsState()
    val loadedCount by motorViewModel.loadedSearchCount.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()

    var isFilterPanelExpanded by remember { mutableStateOf(false) }
    var fullScreenPhotos by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenInitialIndex by remember { mutableIntStateOf(0) }

    var motorToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
    var motorToReject by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(isAdmin) {
        motorViewModel.initSearch(isAdmin)
    }

    if (uiState is UiState.Loading) {
        LoadingDialog("Processing...")
    } else if (uiState is UiState.Success) {
        val action = (uiState as UiState.Success).message
        val title = when (action) {
            "delete" -> "Deleted Successfully"
            "approve" -> "Approved Successfully"
            "reject" -> "Submission Rejected"
            else -> "Success"
        }
        SuccessAnimationDialog(
            title = title,
            onDismiss = { motorViewModel.resetUiState() }
        )
    } else if (uiState is UiState.Error) {
        ErrorDialog(
            message = (uiState as UiState.Error).message,
            onDismiss = { motorViewModel.resetUiState() }
        )
    }

    motorToDelete?.let { (mType, mId) ->
        ConfirmationDialog(
            title = "Delete Motor Data?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                if (mType == "single") motorViewModel.deleteSinglePhase(mId)
                else motorViewModel.deleteThreePhase(mId)
                motorToDelete = null
            },
            onDismiss = { motorToDelete = null }
        )
    }

    motorToReject?.let { (mType, mId) ->
        RejectionReasonDialog(
            onConfirm = { reason ->
                if (mType == "single") motorViewModel.updateSinglePhaseStatus(mId, "rejected", reason)
                else motorViewModel.updateThreePhaseStatus(mId, "rejected", reason)
                motorToReject = null
            },
            onDismiss = { motorToReject = null }
        )
    }

    fullScreenPhotos?.let { photos ->
        FullScreenImageViewer(
            photos = photos,
            initialIndex = fullScreenInitialIndex,
            onDismiss = { fullScreenPhotos = null }
        )
    }

    val activeFilterCount = remember(appliedFilter, isAdmin) {
        appliedFilter.activeCriteriaCount(isAdmin)
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Advanced Search",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAdmin) "All Records • Admin Search" else "Verified Motor Database",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { motorViewModel.refreshSearch(isAdmin) },
                        modifier = Modifier.testTag("refresh_search_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                // Pinned Fast Search Bar + Filter Toggle + Match Count
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = draftFilter.query,
                        onValueChange = { motorViewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("advanced_search_input"),
                        placeholder = { Text("Search company or motor name...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (draftFilter.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { motorViewModel.updateSearchQuery("") },
                                    modifier = Modifier.testTag("clear_query_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                motorViewModel.applyFilters()
                                focusManager.clearFocus()
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FlowRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val next = !isFilterPanelExpanded
                                    isFilterPanelExpanded = next
                                    if (next) {
                                        coroutineScope.launch { listState.scrollToItem(0) }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("toggle_filters_button")
                            ) {
                                Icon(
                                    imageVector = if (isFilterPanelExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFilterPanelExpanded) "Hide Filters" else "Filters",
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                if (activeFilterCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = activeFilterCount.toString(),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (appliedFilter.hasAnyActiveFilter(isAdmin) || draftFilter != MotorSearchFilter()) {
                                OutlinedButton(
                                    onClick = { motorViewModel.clearAllFilters() },
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("quick_clear_filters_button")
                                ) {
                                    Icon(Icons.Rounded.ClearAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Clear All",
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "${results.size} ${if (results.size == 1) "Match" else "Matches"}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("search_results_count")
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("search_results_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Expandable Advanced Filter Panel
                    item(key = "filter_panel") {
                        AnimatedVisibility(
                            visible = isFilterPanelExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            AdvancedFilterPanel(
                                draftFilter = draftFilter,
                                isAdmin = isAdmin,
                                onUpdateFilter = { motorViewModel.updateDraftFilter(it) },
                                onApplyFilters = {
                                    motorViewModel.applyFilters()
                                    focusManager.clearFocus()
                                    isFilterPanelExpanded = false
                                    coroutineScope.launch { listState.scrollToItem(0) }
                                },
                                onClearAll = {
                                    motorViewModel.clearAllFilters()
                                    focusManager.clearFocus()
                                    coroutineScope.launch { listState.scrollToItem(0) }
                                }
                            )
                        }
                    }

                // Active Filter Chips Summary
                if (activeFilterCount > 0) {
                    item(key = "active_filter_summary") {
                        ActiveFilterSummaryRow(
                            filter = appliedFilter,
                            isAdmin = isAdmin
                        )
                    }
                }

                // Sort Options Bar
                item(key = "sort_bar") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sort By",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (loadedCount > 0) {
                                Text(
                                    text = "Loaded $loadedCount records" + if (hasMore) " (more available)" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SearchSortOption.entries.forEach { sortOpt ->
                                val tag = when (sortOpt) {
                                    SearchSortOption.RECENTLY_ADDED -> "sort_recently_added"
                                    SearchSortOption.COMPANY_NAME -> "sort_company_name"
                                    SearchSortOption.HP -> "sort_hp"
                                    SearchSortOption.RPM -> "sort_rpm"
                                }
                                FilterChip(
                                    selected = appliedFilter.sortBy == sortOpt,
                                    onClick = { motorViewModel.updateSortOption(sortOpt) },
                                    label = {
                                        Text(
                                            text = sortOpt.label,
                                            fontWeight = if (appliedFilter.sortBy == sortOpt) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (appliedFilter.sortBy == sortOpt) {
                                        { Icon(Icons.Rounded.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag(tag)
                                )
                            }
                        }
                    }
                }

                // Body States: Loading / Error / Empty / Results
                if (isLoading && results.isEmpty()) {
                    item(key = "loading_state") {
                        SearchLoadingSkeletonList()
                    }
                } else if (searchError != null && results.isEmpty()) {
                    item(key = "error_state") {
                        SearchErrorCard(
                            message = searchError ?: "Unable to load search results.",
                            onRetry = { motorViewModel.refreshSearch(isAdmin) }
                        )
                    }
                } else if (results.isEmpty()) {
                    item(key = "empty_state") {
                        SearchEmptyResultsCard(
                            hasActiveFilters = appliedFilter.hasAnyActiveFilter(isAdmin),
                            hasMore = hasMore,
                            isLoadingMore = isLoadingMore,
                            onClearFilters = { motorViewModel.clearAllFilters() },
                            onLoadMore = { motorViewModel.loadMoreSearchResults() }
                        )
                    }
                } else {
                    items(results, key = { it.uniqueKey }) { item ->
                        SearchMotorResultCard(
                            item = item,
                            isAdmin = isAdmin,
                            onView = { navController.navigate("motor_detail/${item.typeKey}/${item.id}") },
                            onEdit = { navController.navigate("add_edit_motor/${item.typeKey}/${item.id}") },
                            onDelete = { motorToDelete = item.typeKey to item.id },
                            onApprove = {
                                if (item.typeKey == "single") motorViewModel.updateSinglePhaseStatus(item.id, "approved")
                                else motorViewModel.updateThreePhaseStatus(item.id, "approved")
                            },
                            onReject = { motorToReject = item.typeKey to item.id },
                            onPhotoClick = { urls, idx ->
                                fullScreenPhotos = urls
                                fullScreenInitialIndex = idx
                            }
                        )
                    }

                    if (hasMore) {
                        item(key = "load_more_footer") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                OutlinedButton(
                                    onClick = { motorViewModel.loadMoreSearchResults() },
                                    enabled = !isLoadingMore,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("load_more_search_button")
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading More...")
                                    } else {
                                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Load More Motors", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ActiveFilterSummaryRow(
    filter: MotorSearchFilter,
    isAdmin: Boolean
) {
    val tags = buildList {
        if (filter.motorType != SearchMotorType.ALL) add("Type: ${filter.motorType.label}")
        if (filter.category != "All" && filter.category.isNotBlank()) add("Cat: ${filter.category}")
        filter.hp.activeSummary("HP")?.let { add(it) }
        filter.rpm.activeSummary("RPM")?.let { add(it) }
        filter.amps.activeSummary("Amps")?.let { add(it) }
        filter.slot.activeSummary("Slot")?.let { add(it) }
        if (filter.swg.isNotBlank()) add("SWG: ${filter.swg.trim()}")
        if (filter.coilSize.isNotBlank()) add("Coil: ${filter.coilSize.trim()}")
        if (filter.connection != "All") add("Conn: ${filter.connection}")
        if (filter.recentlyAdded != SearchRecentlyAdded.ANY_TIME) add(filter.recentlyAdded.label)
        if (isAdmin && filter.approvalStatus != SearchApprovalStatus.ALL) add("Status: ${filter.approvalStatus.label}")
    }
    if (tags.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { label ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchEmptyResultsCard(
    hasActiveFilters: Boolean,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("search_empty_state"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "No matching motors found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (hasActiveFilters) {
                    "Try adjusting or clearing some filters, or load more records if available."
                } else {
                    "No motor records are currently available."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasActiveFilters) {
                    Button(
                        onClick = onClearFilters,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Clear Filters", fontWeight = FontWeight.Bold)
                    }
                }
                if (hasMore) {
                    OutlinedButton(
                        onClick = onLoadMore,
                        enabled = !isLoadingMore,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(if (isLoadingMore) "Loading..." else "Load More", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("search_error_state"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Search Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry", fontWeight = FontWeight.Bold)
            }
        }
    }
}
