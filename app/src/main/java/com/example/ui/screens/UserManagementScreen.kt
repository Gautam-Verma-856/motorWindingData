package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.model.User
import com.example.model.UserAdminAction
import com.example.model.UserManagementEngine
import com.example.model.UserSortOption
import com.example.model.UserStatusFilter
import com.example.ui.components.PremiumPublicUserCard
import com.example.ui.components.UserEmptyStateCard
import com.example.ui.components.UserErrorStateCard
import com.example.ui.components.UserLoadingSkeletonList
import com.example.ui.components.UserOverviewSummaryGrid
import com.example.ui.components.UserStatusActionDialog
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel

private const val USER_PAGE_SIZE = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    motorViewModel: MotorViewModel? = null,
    initialFilter: String? = null
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val usersList by authViewModel.usersList.collectAsState()
    val isFetchingUsers by authViewModel.isFetchingUsers.collectAsState()
    val usersError by authViewModel.usersError.collectAsState()
    val userActionMessage by authViewModel.userActionMessage.collectAsState()
    val singlePhaseMotors = motorViewModel?.singlePhaseMotors?.collectAsState()?.value ?: emptyList()
    val threePhaseMotors = motorViewModel?.threePhaseMotors?.collectAsState()?.value ?: emptyList()

    // Strict Admin-only role guard
    if (currentUser == null || !currentUser!!.isAdmin()) {
        LaunchedEffect(currentUser) {
            navController.navigate(if (currentUser == null) "public_login" else "home") {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable {
        mutableStateOf(UserStatusFilter.fromInitialFilter(initialFilter))
    }
    var sortOption by rememberSaveable { mutableStateOf(UserSortOption.NEWEST) }
    var visibleCount by remember(searchQuery, statusFilter, sortOption) {
        mutableIntStateOf(USER_PAGE_SIZE)
    }
    var pendingAction by remember { mutableStateOf<Pair<User, UserAdminAction>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.fetchUsers()
        motorViewModel?.loadData(isAdmin = true, userId = currentUser?.id)
    }

    LaunchedEffect(userActionMessage) {
        val message = userActionMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            authViewModel.clearUserActionMessage()
        }
    }

    val overviewStats = remember(usersList) {
        UserManagementEngine.computeOverviewStats(usersList)
    }

    val filteredUsers = remember(usersList, searchQuery, statusFilter, sortOption) {
        UserManagementEngine.filterAndSortUsers(
            users = usersList,
            query = searchQuery,
            statusFilter = statusFilter,
            sortOption = sortOption
        )
    }

    val paginatedUsers = remember(filteredUsers, visibleCount) {
        filteredUsers.take(visibleCount)
    }

    val hasActiveFilters = searchQuery.isNotBlank() ||
        statusFilter != UserStatusFilter.ALL ||
        sortOption != UserSortOption.NEWEST

    val clearFilters: () -> Unit = {
        searchQuery = ""
        statusFilter = UserStatusFilter.ALL
        sortOption = UserSortOption.NEWEST
    }

    if (pendingAction != null) {
        val (targetUser, action) = pendingAction!!
        UserStatusActionDialog(
            user = targetUser,
            action = action,
            onConfirm = { reason ->
                val targetStatus = when (action) {
                    UserAdminAction.APPROVE, UserAdminAction.ACTIVATE -> "active"
                    UserAdminAction.REJECT -> "rejected"
                    UserAdminAction.SUSPEND -> "blocked"
                }
                authViewModel.updateUserStatus(targetUser.id, targetStatus, reason)
                pendingAction = null
            },
            onDismiss = { pendingAction = null }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Manage Public Users",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${overviewStats.totalPublicUsers} registered public accounts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("user_management_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.fetchUsers(forceRefresh = true)
                            motorViewModel?.loadData(isAdmin = true, userId = currentUser?.id)
                        },
                        enabled = !isFetchingUsers,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("refresh_users_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh users")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .testTag("user_management_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Dashboard Overview Summary Cards
                item(key = "overview") {
                    UserOverviewSummaryGrid(
                        stats = overviewStats,
                        selectedFilter = statusFilter,
                        onSelectFilter = { selected ->
                            statusFilter = if (statusFilter == selected && selected != UserStatusFilter.ALL) {
                                UserStatusFilter.ALL
                            } else {
                                selected
                            }
                        }
                    )
                }

                // 2. Search Input & Filters
                item(key = "search_and_filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_search_input"),
                            placeholder = { Text("Search by Name, Email, Mobile or User ID...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.testTag("clear_user_search_button")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            singleLine = true
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(UserStatusFilter.entries) { filter ->
                                FilterChip(
                                    selected = statusFilter == filter,
                                    onClick = { statusFilter = filter },
                                    label = { Text(filter.label) },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 40.dp)
                                        .testTag("filter_chip_${filter.name.lowercase()}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapVert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                UserSortOption.entries.forEach { option ->
                                    FilterChip(
                                        selected = sortOption == option,
                                        onClick = { sortOption = option },
                                        label = { Text(option.label) },
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 40.dp)
                                            .testTag("sort_chip_${option.name.lowercase()}")
                                    )
                                }
                            }

                            if (hasActiveFilters) {
                                TextButton(
                                    onClick = clearFilters,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("user_clear_filters_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ClearAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear Filters")
                                }
                            }
                        }

                        if (!isFetchingUsers && usersError == null) {
                            Text(
                                text = "Showing ${paginatedUsers.size} of ${filteredUsers.size} users",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Loading, Error, Empty, or User Cards
                if (isFetchingUsers && usersList.isEmpty()) {
                    item(key = "loading_skeleton") {
                        UserLoadingSkeletonList()
                    }
                } else if (usersError != null && usersList.isEmpty()) {
                    item(key = "error_state") {
                        UserErrorStateCard(
                            message = usersError ?: "Unable to fetch users.",
                            onRetry = { authViewModel.fetchUsers(forceRefresh = true) }
                        )
                    }
                } else if (filteredUsers.isEmpty()) {
                    item(key = "empty_state") {
                        UserEmptyStateCard(
                            hasFilters = hasActiveFilters,
                            onClearFilters = clearFilters
                        )
                    }
                } else {
                    items(
                        items = paginatedUsers,
                        key = { it.id.ifBlank { it.email } }
                    ) { user ->
                        val submissionStats = remember(user.id, user.name, singlePhaseMotors, threePhaseMotors) {
                            UserManagementEngine.computeUserSubmissions(
                                user = user,
                                singlePhaseMotors = singlePhaseMotors,
                                threePhaseMotors = threePhaseMotors
                            )
                        }
                        PremiumPublicUserCard(
                            user = user,
                            submissionStats = submissionStats,
                            onViewDetails = {
                                if (user.isUnseen()) {
                                    authViewModel.markUserAsSeen(user.id)
                                }
                                navController.navigate("user_detail/${user.id}")
                            },
                            onActionClick = { action ->
                                pendingAction = user to action
                            }
                        )
                    }

                    if (paginatedUsers.size < filteredUsers.size) {
                        item(key = "load_more_footer") {
                            val remaining = filteredUsers.size - paginatedUsers.size
                            OutlinedButton(
                                onClick = { visibleCount += USER_PAGE_SIZE },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("load_more_users_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.ExpandMore, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Load More Users ($remaining remaining)")
                            }
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
