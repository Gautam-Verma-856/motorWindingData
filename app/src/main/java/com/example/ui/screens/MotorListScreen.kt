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

    // Filter for generic "approved" / "pending" / "my_submissions" views
    var selectedFilter by remember {
        mutableStateOf(
            if (type == "approved" || type == "pending" || type == "my_submissions") "All"
            else if (type == "single") "Single Phase"
            else "Three Phase"
        )
    }

    var fullScreenPhotos by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenInitialIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        motorViewModel.loadData()
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
    val singleCategories = listOf("Self Priming (Tullu Pump)", "Submersible Motor", "Pandubi")
    val threeCategories = listOf("Monoblock", "Submersible", "Pandubi")

    // CHECK IF CATEGORY SELECTION SHOULD BE DISPLAYED
    val isSingleOrThree = (type == "single" || type == "three")
    val showCategoryPicker = isSingleOrThree && selectedCategory == null

    if (showCategoryPicker) {
        // TOP LEVEL: Show 3 Categories view
        val categoriesList = if (type == "single") singleCategories else threeCategories
        val isSingle = type == "single"

        // Check if there are legacy motors with blank or other categories (must be explicitly approved)
        val legacyMotorsCount = if (isSingle) {
            singlePhaseMotors.count { it.isExplicitlyApproved() && (it.category.isBlank() || it.category !in singleCategories) }
        } else {
            threePhaseMotors.count { it.isExplicitlyApproved() && (it.category.isBlank() || it.category !in threeCategories) }
        }

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
                        val matching = singlePhaseMotors.filter { it.isExplicitlyApproved() && it.category.equals(cat, ignoreCase = true) }
                        val comps = matching.map { it.companyName.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().size
                        Pair(matching.size, comps)
                    } else {
                        val matching = threePhaseMotors.filter { it.isExplicitlyApproved() && it.category.equals(cat, ignoreCase = true) }
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
                            else -> Icons.Rounded.ElectricMeter
                        },
                        onClick = {
                            selectedCategory = cat
                            selectedCompany = "All"
                        }
                    )
                }

                // If legacy records exist, provide option to view them so no data is lost
                if (legacyMotorsCount > 0) {
                    item {
                        CategorySelectionCard(
                            title = "General / Other Motors",
                            motorCount = legacyMotorsCount,
                            companyCount = 0,
                            gradientColors = listOf(Color(0xFF64748B), Color(0xFF475569)),
                            icon = Icons.Rounded.Folder,
                            onClick = {
                                selectedCategory = "General / Other Motors"
                                selectedCompany = "All"
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        return
    }

    // MOTOR LIST SCREEN (Category selected or pending/approved/my_submissions)
    val screenTitle = when {
        selectedCategory != null -> selectedCategory!!
        type == "pending" -> "Pending Approval"
        type == "approved" -> "Approved Motor Data"
        type == "my_submissions" -> "My Submissions"
        type == "single" -> "Single Phase Motors"
        type == "three" -> "3 Phase Motors"
        else -> "Motor Data"
    }

    val screenSubtitle = when {
        selectedCategory != null -> "Company Wise Motor Data"
        type == "pending" -> "Review motor data submitted by public users"
        type == "approved" -> "Browse verified motor winding data"
        type == "my_submissions" -> "Track your submitted motor data"
        else -> "Browse specifications"
    }

    // Pre-calculate motors matching the selected category (if applicable)
    // For Single and Three Phase browsing, ONLY explicitly approved motors are included
    val singleInCategory = remember(singlePhaseMotors, selectedCategory, type) {
        val baseList = if (type == "single") {
            singlePhaseMotors.filter { it.isExplicitlyApproved() }
        } else {
            singlePhaseMotors
        }
        if (type != "single" || selectedCategory == null) baseList
        else if (selectedCategory == "General / Other Motors") {
            baseList.filter { it.category.isBlank() || it.category !in singleCategories }
        } else {
            baseList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val threeInCategory = remember(threePhaseMotors, selectedCategory, type) {
        val baseList = if (type == "three") {
            threePhaseMotors.filter { it.isExplicitlyApproved() }
        } else {
            threePhaseMotors
        }
        if (type != "three" || selectedCategory == null) baseList
        else if (selectedCategory == "General / Other Motors") {
            baseList.filter { it.category.isBlank() || it.category !in threeCategories }
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

                // TABS FOR APPROVED / PENDING / MY_SUBMISSIONS
                if (type == "approved" || type == "pending" || type == "my_submissions") {
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
            }

            // Resolved current user UID for My Submissions matching
            val authUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val targetUserId = currentUserId?.ifBlank { authUid } ?: authUid

            // FILTER LIST ITEMS
            val filteredSingle = singleInCategory.filter { motor ->
                val matchesType = when (type) {
                    "single" -> motor.isExplicitlyApproved()
                    "approved" -> motor.isExplicitlyApproved()
                    "pending" -> motor.isPending()
                    "my_submissions" -> (targetUserId.isNotEmpty() && motor.createdBy == targetUserId)
                    else -> motor.isExplicitlyApproved()
                }
                val matchesCompany = selectedCompany == "All" || motor.companyName.equals(selectedCompany, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        motor.companyName.contains(searchQuery, ignoreCase = true) ||
                        motor.hp.contains(searchQuery, ignoreCase = true) ||
                        motor.category.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Single Phase"
                matchesType && matchesCompany && matchesSearch && matchesFilterChip
            }

            val filteredThree = threeInCategory.filter { motor ->
                val matchesType = when (type) {
                    "three" -> motor.isExplicitlyApproved()
                    "approved" -> motor.isExplicitlyApproved()
                    "pending" -> motor.isPending()
                    "my_submissions" -> (targetUserId.isNotEmpty() && motor.createdBy == targetUserId)
                    else -> motor.isExplicitlyApproved()
                }
                val matchesCompany = selectedCompany == "All" || motor.name.equals(selectedCompany, ignoreCase = true)
                val matchesSearch = searchQuery.isBlank() ||
                        motor.name.contains(searchQuery, ignoreCase = true) ||
                        motor.hp.contains(searchQuery, ignoreCase = true) ||
                        motor.category.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Three Phase"
                matchesType && matchesCompany && matchesSearch && matchesFilterChip
            }

            val filteredCustom = customMotors.filter { motor ->
                val matchesType = when (type) {
                    "approved" -> motor.isExplicitlyApproved()
                    "pending" -> motor.isPending()
                    "my_submissions" -> (targetUserId.isNotEmpty() && motor.createdBy == targetUserId)
                    else -> false
                }
                val matchesSearch = searchQuery.isBlank() ||
                        motor.companyName.contains(searchQuery, ignoreCase = true) ||
                        motor.motorName.contains(searchQuery, ignoreCase = true) ||
                        motor.hp.contains(searchQuery, ignoreCase = true)
                val matchesFilterChip = selectedFilter == "All" || selectedFilter == "Custom Categories"
                matchesType && matchesSearch && matchesFilterChip
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
        "my_submissions" -> "No submissions yet"
        else -> if (selectedCategory != null) "No motors in $selectedCategory" else "No motors found"
    }
    val subtitle = when (type) {
        "pending" -> "There are no pending submissions to review."
        "my_submissions" -> "Submit motor winding data to see it here."
        else -> "Try adjusting your search or company filter."
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (type == "pending") Icons.Rounded.CheckCircle else Icons.Rounded.Inbox,
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

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == "admin" && motor.status == "pending") {
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

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (role == "admin" && motor.status == "pending") {
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
