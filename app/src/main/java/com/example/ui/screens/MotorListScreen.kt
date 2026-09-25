package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.model.CustomMotor
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ErrorDialog
import com.example.ui.components.FullScreenImageViewer
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorListScreen(
    navController: NavController,
    motorViewModel: MotorViewModel,
    type: String,
    role: String,
    currentUserId: String? = null
) {
    val uiState by motorViewModel.uiState.collectAsState()
    val isLoadingData by motorViewModel.isLoadingData.collectAsState()
    val singlePhaseMotors by motorViewModel.singlePhaseMotors.collectAsState()
    val threePhaseMotors by motorViewModel.threePhaseMotors.collectAsState()
    val customMotors by motorViewModel.customMotors.collectAsState()

    var motorToDelete by remember { mutableStateOf<String?>(null) }
    var deleteType by remember { mutableStateOf<String?>(null) }
    var motorToReject by remember { mutableStateOf<String?>(null) }
    var rejectType by remember { mutableStateOf<String?>(null) }

    // Category selection for Single and 3 Phase
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCompany by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Filter for generic "approved" / "pending" / "rejected" / "all" / "my_submissions" views
    var selectedFilter by remember {
        mutableStateOf(
            if (type == "approved" || type == "pending" || type == "rejected" || type == "all" || type == "my_submissions") "All"
            else if (type == "single") "Single Phase"
            else "Three Phase"
        )
    }

    var selectedStatus by remember(type) {
        mutableStateOf(
            when (type) {
                "pending" -> "Pending"
                "rejected" -> "Rejected"
                "approved" -> "Approved"
                else -> "All Status"
            }
        )
    }

    var fullScreenPhotos by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenInitialIndex by remember { mutableIntStateOf(0) }

    val authUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val targetUserId = currentUserId?.ifBlank { authUid } ?: authUid

    LaunchedEffect(type, role, targetUserId) {
        motorViewModel.loadData(isAdmin = (role == "admin"), userId = targetUserId)
    }

    if (uiState is UiState.Loading) {
        LoadingDialog("Processing...")
    } else if (uiState is UiState.Success) {
        val action = (uiState as UiState.Success).message
        val title = when (action) {
            "delete", "delete_custom_motor" -> "Deleted Successfully"
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

    if (motorToDelete != null) {
        ConfirmationDialog(
            title = "Delete Motor Data?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                when (deleteType) {
                    "single" -> motorViewModel.deleteSinglePhase(motorToDelete!!)
                    "three" -> motorViewModel.deleteThreePhase(motorToDelete!!)
                    "custom" -> motorViewModel.deleteCustomMotor(motorToDelete!!)
                }
                motorToDelete = null
                deleteType = null
            },
            onDismiss = {
                motorToDelete = null
                deleteType = null
            }
        )
    }

    if (motorToReject != null) {
        ConfirmationDialog(
            title = "Reject Submission?",
            message = "Are you sure you want to reject this motor data?",
            confirmText = "Reject",
            isDestructive = true,
            onConfirm = {
                when (rejectType) {
                    "single" -> motorViewModel.updateSinglePhaseStatus(motorToReject!!, "rejected")
                    "three" -> motorViewModel.updateThreePhaseStatus(motorToReject!!, "rejected")
                    "custom" -> motorViewModel.updateCustomMotorStatus(motorToReject!!, "rejected")
                }
                motorToReject = null
                rejectType = null
            },
            onDismiss = {
                motorToReject = null
                rejectType = null
            }
        )
    }

    fullScreenPhotos?.let { photos ->
        FullScreenImageViewer(
            photos = photos,
            initialIndex = fullScreenInitialIndex,
            onDismiss = { fullScreenPhotos = null }
        )
    }

    // CATEGORY DEFINITIONS
    val singleCategories = listOf("Self Priming (Tullu Pump)", "Submersible Motor", "Pandubi", "General / Other Motors")
    val threeCategories = listOf("Monoblock", "Submersible", "Pandubi", "General / Other Motors")

    // CHECK IF CATEGORY SELECTION SHOULD BE DISPLAYED
    val isSingleOrThree = (type == "single" || type == "three")
    val showCategoryPicker = isSingleOrThree && selectedCategory == null

    if (showCategoryPicker) {
        // TOP LEVEL: Show Categories view
        val categoriesList = if (type == "single") singleCategories else threeCategories
        val isSingle = type == "single"

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                if (isSingle) "Single Phase Motors" else "3 Phase Motors",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Select a category to view motor data",
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
                        IconButton(onClick = {
                            motorViewModel.loadData(isAdmin = (role == "admin"), userId = targetUserId)
                        }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Data")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Motor Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Select any category below to browse motor winding data organized company-wise.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(categoriesList) { cat ->
                    val (count, compCount) = if (isSingle) {
                        val matching = singlePhaseMotors.filter { motor ->
                            val statusOk = if (role == "admin") true else motor.isExplicitlyApproved()
                            val catOk = if (cat == "General / Other Motors") {
                                motor.category.isBlank() || motor.category.equals("General / Other Motors", ignoreCase = true) || motor.category !in singleCategories.take(3)
                            } else {
                                motor.category.equals(cat, ignoreCase = true)
                            }
                            statusOk && catOk
                        }
                        val comps = matching.map { it.companyName.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().size
                        Pair(matching.size, comps)
                    } else {
                        val matching = threePhaseMotors.filter { motor ->
                            val statusOk = if (role == "admin") true else motor.isExplicitlyApproved()
                            val catOk = if (cat == "General / Other Motors") {
                                motor.category.isBlank() || motor.category.equals("General / Other Motors", ignoreCase = true) || motor.category !in threeCategories.take(3)
                            } else {
                                motor.category.equals(cat, ignoreCase = true)
                            }
                            statusOk && catOk
                        }
                        val comps = matching.map { it.name.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().size
                        Pair(matching.size, comps)
                    }

                    CategorySelectionCard(
                        title = cat,
                        motorCount = count,
                        companyCount = compCount,
                        gradientColors = if (isSingle) {
                            listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                        } else {
                            listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                        },
                        icon = when {
                            cat.contains("Submersible", ignoreCase = true) -> Icons.Rounded.Waves
                            cat.contains("Pandubi", ignoreCase = true) -> Icons.Rounded.Water
                            cat.contains("Monoblock", ignoreCase = true) -> Icons.Rounded.Build
                            cat.contains("General", ignoreCase = true) -> Icons.Rounded.Folder
                            else -> Icons.Rounded.ElectricMeter
                        },
                        onClick = {
                            selectedCategory = cat
                            selectedCompany = "All"
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        return
    }

    // MOTOR LIST SCREEN (Category selected or pending/approved/rejected/all/my_submissions)
    val screenTitle = when {
        selectedCategory != null -> selectedCategory!!
        type == "pending" -> "Pending Approval"
        type == "approved" -> "Approved Motor Data"
        type == "rejected" -> "Rejected Motor Data"
        type == "all" -> "All Motor Data"
        type == "my_submissions" -> "My Submissions"
        type == "single" -> "Single Phase Motors"
        type == "three" -> "3 Phase Motors"
        else -> "Motor Data"
    }

    val screenSubtitle = when {
        selectedCategory != null -> "Company Wise Motor Data"
        type == "pending" -> "Review motor data submitted by public users"
        type == "approved" -> "Browse verified motor winding data"
        type == "rejected" -> "Review rejected motor submissions"
        type == "all" -> "Browse all approved, pending, and rejected records"
        type == "my_submissions" -> "Track your submitted motor data"
        else -> "Browse specifications"
    }

    // Pre-calculate motors matching the selected category (if applicable)
    // For non-admin public browsing, ONLY explicitly approved motors are included
    val singleInCategory = remember(singlePhaseMotors, selectedCategory, type, role) {
        val baseList = if (role != "admin" && type != "my_submissions") {
            singlePhaseMotors.filter { it.isExplicitlyApproved() }
        } else {
            singlePhaseMotors
        }
        if (type != "single" || selectedCategory == null) baseList
        else if (selectedCategory == "General / Other Motors") {
            baseList.filter { it.category.isBlank() || it.category.equals("General / Other Motors", ignoreCase = true) || it.category !in singleCategories.take(3) }
        } else {
            baseList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val threeInCategory = remember(threePhaseMotors, selectedCategory, type, role) {
        val baseList = if (role != "admin" && type != "my_submissions") {
            threePhaseMotors.filter { it.isExplicitlyApproved() }
        } else {
            threePhaseMotors
        }
        if (type != "three" || selectedCategory == null) baseList
        else if (selectedCategory == "General / Other Motors") {
            baseList.filter { it.category.isBlank() || it.category.equals("General / Other Motors", ignoreCase = true) || it.category !in threeCategories.take(3) }
        } else {
            baseList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    // Dynamic company list for company filtering in this category
    val companiesInCurrentView = remember(singleInCategory, threeInCategory, type) {
        val names = if (type == "single") {
            singleInCategory.map { it.companyName.trim() }
        } else if (type == "three") {
            threeInCategory.map { it.name.trim() }
        } else {
            emptyList()
        }
        val unique = names.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }.sortedWith(String.CASE_INSENSITIVE_ORDER)
        listOf("All") + unique
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(screenTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(screenSubtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCategory != null) {
                            selectedCategory = null
                            selectedCompany = "All"
                            searchQuery = ""
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        motorViewModel.loadData(isAdmin = (role == "admin"), userId = targetUserId)
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh Data")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // SEARCH & FILTERS SECTION
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by company, HP...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                // COMPANY FILTER CHIPS (PART 1 & 2: Company-wise organization)
                if (companiesInCurrentView.size > 1 && (type == "single" || type == "three")) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Filter by Company:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        companiesInCurrentView.forEach { company ->
                            val count = if (company == "All") {
                                if (type == "single") singleInCategory.size else threeInCategory.size
                            } else {
                                if (type == "single") singleInCategory.count { it.companyName.equals(company, ignoreCase = true) }
                                else threeInCategory.count { it.name.equals(company, ignoreCase = true) }
                            }
                            FilterChip(
                                selected = selectedCompany == company,
                                onClick = { selectedCompany = company },
                                label = { Text("$company ($count)") },
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }

                // TABS FOR APPROVED / PENDING / REJECTED / ALL / MY_SUBMISSIONS
                if (type == "approved" || type == "pending" || type == "rejected" || type == "all" || type == "my_submissions") {
                    Spacer(modifier = Modifier.height(10.dp))
                    val filters = listOf("All", "Single Phase", "Three Phase", "Custom Categories")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }

                // STATUS TABS FOR ADMIN AND MY SUBMISSIONS
                val showStatusChips = role == "admin" || type == "my_submissions"
                if (showStatusChips) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Filter by Status:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val statusChips = listOf("All Status", "Pending", "Approved", "Rejected")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statusChips.forEach { statusOption ->
                            FilterChip(
                                selected = selectedStatus == statusOption,
                                onClick = { selectedStatus = statusOption },
                                label = { Text(statusOption) },
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            // FILTER LIST ITEMS
            val filteredSingle = singleInCategory.filter { motor ->
                val matchesType = when (type) {
                    "single" -> true
                    "approved" -> motor.isExplicitlyApproved()
                    "pending" -> motor.isPending()
                    "rejected" -> motor.isRejected()
                    "all" -> true
                    "my_submissions" -> (targetUserId.isNotEmpty() && motor.createdBy == targetUserId)
                    else -> true
                }
                val matchesStatus = when (selectedStatus) {
                    "Pending" -> motor.isPending()
                    "Approved" -> motor.isExplicitlyApproved()
                    "Rejected" -> motor.isRejected()
                    else -> true
                }
                val matchesCompany = selectedCompany == "All" || motor.companyName.equals(selectedCompany, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        motor.companyName.contains(searchQuery, ignoreCase = true) ||
                        motor.hp.contains(searchQuery, ignoreCase = true) ||
                        motor.category.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Single Phase"
                val publicBrowseAllowed = if (role != "admin" && type != "my_submissions") motor.isExplicitlyApproved() else true
                matchesType && matchesStatus && matchesCompany && matchesSearch && matchesFilterChip && publicBrowseAllowed
            }

            val filteredThree = threeInCategory.filter { motor ->
                val matchesType = when (type) {
                    "three" -> true
                    "approved" -> motor.isExplicitlyApproved()
                    "pending" -> motor.isPending()
                    "rejected" -> motor.isRejected()
                    "all" -> true
                    "my_submissions" -> (targetUserId.isNotEmpty() && motor.createdBy == targetUserId)
                    else -> true
                }
                val matchesStatus = when (selectedStatus) {
                    "Pending" -> motor.isPending()
                    "Approved" -> motor.isExplicitlyApproved()
                    "Rejected" -> motor.isRejected()
                    else -> true
                }
                val matchesCompany = selectedCompany == "All" || motor.name.equals(selectedCompany, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        motor.name.contains(searchQuery, ignoreCase = true) ||
                        motor.hp.contains(searchQuery, ignoreCase = true) ||
                        motor.category.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Three Phase"
                val publicBrowseAllowed = if (role != "admin" && type != "my_submissions") motor.isExplicitlyApproved() else true
                matchesType && matchesStatus && matchesCompany && matchesSearch && matchesFilterChip && publicBrowseAllowed
            }

            val filteredCustom = customMotors.filter { motor ->
                val matchesType = when (type) {
                    "approved" -> motor.isExplicitlyApproved()
                    "pending" -> motor.isPending()
                    "rejected" -> motor.isRejected()
                    "all" -> true
                    "my_submissions" -> (targetUserId.isNotEmpty() && motor.createdBy == targetUserId)
                    else -> if (role == "admin") true else false
                }
                val matchesStatus = when (selectedStatus) {
                    "Pending" -> motor.isPending()
                    "Approved" -> motor.isExplicitlyApproved()
                    "Rejected" -> motor.isRejected()
                    else -> true
                }
                val matchesSearch = searchQuery.isBlank() ||
                        motor.companyName.contains(searchQuery, ignoreCase = true) ||
                        motor.motorName.contains(searchQuery, ignoreCase = true) ||
                        motor.hp.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Custom Categories"
                val publicBrowseAllowed = if (role != "admin" && type != "my_submissions") motor.isExplicitlyApproved() else true
                matchesType && matchesStatus && matchesSearch && matchesFilterChip && publicBrowseAllowed
            }

            if (isLoadingData) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredSingle.isEmpty() && filteredThree.isEmpty() && filteredCustom.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView(type = type, selectedCategory = selectedCategory)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredSingle, key = { "single_${it.id}" }) { motor ->
                        PremiumSinglePhaseMotorCard(
                            motor = motor,
                            role = role,
                            viewType = type,
                            onView = { navController.navigate("motor_detail/single/${motor.id}") },
                            onEdit = { navController.navigate("add_edit_motor/single/${motor.id}") },
                            onDelete = { motorToDelete = motor.id; deleteType = "single" },
                            onApprove = { motorViewModel.updateSinglePhaseStatus(motor.id, "approved") },
                            onReject = { motorToReject = motor.id; rejectType = "single" },
                            onPhotoClick = { urls, idx ->
                                fullScreenPhotos = urls
                                fullScreenInitialIndex = idx
                            }
                        )
                    }
                    items(filteredThree, key = { "three_${it.id}" }) { motor ->
                        PremiumThreePhaseMotorCard(
                            motor = motor,
                            role = role,
                            viewType = type,
                            onView = { navController.navigate("motor_detail/three/${motor.id}") },
                            onEdit = { navController.navigate("add_edit_motor/three/${motor.id}") },
                            onDelete = { motorToDelete = motor.id; deleteType = "three" },
                            onApprove = { motorViewModel.updateThreePhaseStatus(motor.id, "approved") },
                            onReject = { motorToReject = motor.id; rejectType = "three" },
                            onPhotoClick = { urls, idx ->
                                fullScreenPhotos = urls
                                fullScreenInitialIndex = idx
                            }
                        )
                    }
                    items(filteredCustom, key = { "custom_${it.id}" }) { motor ->
                        CustomMotorCard(
                            motor = motor,
                            category = com.example.model.CustomCategory(id = motor.categoryId, name = motor.categoryName),
                            role = role,
                            currentUserId = currentUserId,
                            onView = { navController.navigate("custom_motor_detail/${motor.id}") },
                            onEdit = { navController.navigate("add_edit_custom_motor/${motor.categoryId}/${motor.id}") },
                            onDelete = { motorToDelete = motor.id; deleteType = "custom" },
                            onApprove = { motorViewModel.updateCustomMotorStatus(motor.id, "approved") },
                            onReject = { motorToReject = motor.id; rejectType = "custom" },
                            onPhotoClick = { urls, idx ->
                                fullScreenPhotos = urls
                                fullScreenInitialIndex = idx
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySelectionCard(
    title: String,
    motorCount: Int,
    companyCount: Int,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Brush.linearGradient(gradientColors), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            "$motorCount Motors",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (companyCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                "$companyCount Companies",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateView(type: String, selectedCategory: String? = null) {
    val title = when (type) {
        "pending" -> "All caught up!"
        "rejected" -> "No rejected submissions"
        "my_submissions" -> "No submissions yet"
        else -> if (selectedCategory != null) "No motors in $selectedCategory" else "No motors found"
    }
    val subtitle = when (type) {
        "pending" -> "There are no pending submissions to review."
        "rejected" -> "There are no rejected records."
        "my_submissions" -> "Submit motor winding data to track approval status here."
        else -> "Try adjusting your search, status, or company filter."
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (type) {
                    "pending" -> Icons.Rounded.CheckCircle
                    "rejected" -> Icons.Rounded.ThumbUp
                    else -> Icons.Rounded.Inbox
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun PremiumSinglePhaseMotorCard(
    motor: SinglePhaseMotor,
    role: String,
    viewType: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPhotoClick: (List<String>, Int) -> Unit
) {
    val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onView() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Company Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            motor.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (motor.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                motor.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                StatusBadge(motor.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Photo thumbnail & Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(displayUrls, 0) }
                    ) {
                        AsyncImage(
                            model = displayUrls.first(),
                            contentDescription = "Motor Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (displayUrls.size > 1) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    "+${displayUrls.size - 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip("HP", motor.hp.ifEmpty { "-" })
                        InfoChip("Capacitor", motor.capacitor.ifEmpty { "-" })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip("Run SWG", motor.runningSwg.ifEmpty { "-" })
                        InfoChip("Start SWG", motor.startingSwg.ifEmpty { "-" })
                    }
                }
            }

            if (motor.createdByName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Added by: ${motor.createdByName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (motor.createdAt > 0L) {
                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(motor.createdAt))
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submitted: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == "admin" && motor.isPending()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onApprove,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF047857)),
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
                } else if (role == "admin" && motor.isRejected()) {
                    FilledTonalButton(
                        onClick = onApprove,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF047857)),
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
                    if (role == "admin") {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "View", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumThreePhaseMotorCard(
    motor: ThreePhaseMotor,
    role: String,
    viewType: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onPhotoClick: (List<String>, Int) -> Unit
) {
    val displayUrls = if (motor.photoUrls.isNotEmpty()) motor.photoUrls else if (motor.photoUrl.isNotEmpty()) listOf(motor.photoUrl) else emptyList()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onView() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            motor.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (motor.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                motor.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                StatusBadge(motor.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Photo thumbnail & Specs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPhotoClick(displayUrls, 0) }
                    ) {
                        AsyncImage(
                            model = displayUrls.first(),
                            contentDescription = "Motor Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (displayUrls.size > 1) {
                            Surface(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    "+${displayUrls.size - 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip("HP", motor.hp.ifEmpty { "-" })
                        InfoChip("Slot", motor.slot.ifEmpty { "-" })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip("SWG", motor.swg.ifEmpty { "-" })
                        InfoChip("Turns", motor.turn.ifEmpty { "-" })
                    }
                }
            }

            if (motor.createdByName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Added by: ${motor.createdByName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (motor.createdAt > 0L) {
                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(motor.createdAt))
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submitted: $dateStr", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == "admin" && motor.isPending()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onApprove,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF047857)),
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
                } else if (role == "admin" && motor.isRejected()) {
                    FilledTonalButton(
                        onClick = onApprove,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f), contentColor = Color(0xFF047857)),
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
                    if (role == "admin") {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = onView, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "View", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    com.example.ui.components.StatusBadge(status = status)
}

@Composable
fun InfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
