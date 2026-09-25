package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.model.CustomCategory
import com.example.model.CustomCategoryField
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ErrorDialog
import com.example.ui.components.LoadingDialog
import com.example.ui.components.SuccessAnimationDialog
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.MotorViewModel
import com.example.viewmodel.UiState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCustomCategoriesScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    motorViewModel: MotorViewModel
) {
    val context = LocalContext.current
    val customCategories by motorViewModel.customCategories.collectAsState()
    val customMotors by motorViewModel.customMotors.collectAsState()
    val uiState by motorViewModel.uiState.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CustomCategory?>(null) }
    var categoryToDelete by remember { mutableStateOf<CustomCategory?>(null) }

    LaunchedEffect(Unit) {
        motorViewModel.loadData()
    }

    if (uiState is UiState.Loading) {
        LoadingDialog("Saving...")
    } else if (uiState is UiState.Success) {
        val action = (uiState as UiState.Success).message
        val title = when (action) {
            "save_category" -> "Category Saved Successfully"
            "delete_category" -> "Category Deleted Successfully"
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

    if (categoryToDelete != null) {
        val cat = categoryToDelete!!
        val motorCount = customMotors.count { it.categoryId == cat.id }
        ConfirmationDialog(
            title = "Delete '${cat.name}'?",
            message = if (motorCount > 0) {
                "This category has $motorCount motor records. Are you sure you want to delete this category?"
            } else {
                "Are you sure you want to delete this custom category? This action cannot be undone."
            },
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                motorViewModel.deleteCustomCategory(cat.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }

    if (showAddEditDialog) {
        AddEditCategoryDialog(
            category = categoryToEdit,
            onDismiss = {
                showAddEditDialog = false
                categoryToEdit = null
            },
            onSave = { updatedCategory ->
                motorViewModel.saveCustomCategory(updatedCategory)
                showAddEditDialog = false
                categoryToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Manage Custom Categories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Create & configure categories & custom fields", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    categoryToEdit = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Category", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (customCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Category,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Custom Categories Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create categories like Cooler Motor, Farata Motor, Exhaust Fan Motor with flexible custom fields.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            categoryToEdit = null
                            showAddEditDialog = true
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create First Category")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(customCategories, key = { it.id }) { category ->
                    val categoryMotors = customMotors.filter { it.categoryId == category.id }
                    val companiesCount = categoryMotors.map { it.companyName.trim().lowercase() }.filter { it.isNotEmpty() }.distinct().size
                    val totalRecords = categoryMotors.size
                    val pendingRecords = categoryMotors.count { it.status == "pending" }
                    val approvedRecords = categoryMotors.count { it.status == "approved" }

                    CustomCategoryAdminCard(
                        category = category,
                        companiesCount = companiesCount,
                        totalRecords = totalRecords,
                        pendingRecords = pendingRecords,
                        approvedRecords = approvedRecords,
                        onOpen = { navController.navigate("custom_motor_list/${category.id}") },
                        onEdit = {
                            categoryToEdit = category
                            showAddEditDialog = true
                        },
                        onDelete = { categoryToDelete = category },
                        onToggleActive = { isActive ->
                            motorViewModel.saveCustomCategory(category.copy(isActive = isActive))
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

@Composable
fun CustomCategoryAdminCard(
    category: CustomCategory,
    companiesCount: Int,
    totalRecords: Int,
    pendingRecords: Int,
    approvedRecords: Int,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                ),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.ElectricBolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (category.isActive) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    if (category.isActive) "Active" else "Inactive",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (category.isActive) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (category.allowPublicSubmissions) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF06B6D4).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "Public Allowed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF0891B2),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // STATS ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CategoryStatItem("Companies", companiesCount.toString(), Icons.Rounded.Business)
                CategoryStatItem("Motors", totalRecords.toString(), Icons.Rounded.Build)
                CategoryStatItem("Approved", approvedRecords.toString(), Icons.Rounded.CheckCircle, Color(0xFF10B981))
                if (pendingRecords > 0) {
                    CategoryStatItem("Pending", pendingRecords.toString(), Icons.Rounded.HourglassEmpty, Color(0xFFF59E0B))
                }
                CategoryStatItem("Fields", category.fields.size.toString(), Icons.Rounded.Tune)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = category.isActive,
                        onCheckedChange = onToggleActive,
                        modifier = Modifier.scale(0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (category.isActive) "Visible on Home" else "Hidden from Home",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("View Motors", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryDialog(
    category: CustomCategory?,
    onDismiss: () -> Unit,
    onSave: (CustomCategory) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var isActive by remember { mutableStateOf(category?.isActive ?: true) }
    var allowPublicSubmissions by remember { mutableStateOf(category?.allowPublicSubmissions ?: true) }
    var fields by remember { mutableStateOf<List<CustomCategoryField>>(category?.fields ?: emptyList()) }

    var newFieldName by remember { mutableStateOf("") }
    var newFieldType by remember { mutableStateOf("text") }
    var newFieldRequired by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // DIALOG HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (category == null) "New Custom Category" else "Edit Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name *") },
                        placeholder = { Text("e.g. Cooler Motor, Farata Motor") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    // Switches
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Active Status", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Show this category on Main Page", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = isActive, onCheckedChange = { isActive = it })
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Allow Public Submissions", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Public users can submit data (pending admin approval)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = allowPublicSubmissions, onCheckedChange = { allowPublicSubmissions = it })
                            }
                        }
                    }

                    // CUSTOM FIELDS BUILDER
                    Text(
                        "Custom Fields Specification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Define specific fields for this motor type (e.g. RPM, Wire Gauge, Turns, Weight, Capacitor). Company Name, Motor/Model & HP are included by default.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick suggestion chips
                    val suggestions = listOf("RPM", "Wire Gauge", "Turns", "Weight", "Capacitor", "Core Length", "Resistance")
                    Text("Quick Add Suggestions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.take(4).forEach { sug ->
                            SuggestionChip(
                                onClick = {
                                    if (fields.none { it.fieldName.equals(sug, ignoreCase = true) }) {
                                        fields = fields + CustomCategoryField(
                                            id = UUID.randomUUID().toString(),
                                            fieldName = sug,
                                            fieldType = if (sug in listOf("RPM", "Turns", "Core Length")) "number" else "text",
                                            isRequired = false,
                                            order = fields.size
                                        )
                                    }
                                },
                                label = { Text("+ $sug", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    // Defined fields list
                    if (fields.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            fields.forEachIndexed { index, field ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(field.fieldName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                if (field.isRequired) {
                                                    Text(" *", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text("Type: ${field.fieldType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(
                                            onClick = {
                                                fields = fields.filterIndexed { i, _ -> i != index }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ADD NEW FIELD ROW
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Add Custom Field", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = newFieldName,
                                onValueChange = { newFieldName = it },
                                label = { Text("Field Name") },
                                placeholder = { Text("e.g. RPM, Wire Gauge") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = newFieldType == "text",
                                        onClick = { newFieldType = "text" },
                                        label = { Text("Text") }
                                    )
                                    FilterChip(
                                        selected = newFieldType == "number",
                                        onClick = { newFieldType = "number" },
                                        label = { Text("Number") }
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = newFieldRequired,
                                        onCheckedChange = { newFieldRequired = it }
                                    )
                                    Text("Required", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Button(
                                onClick = {
                                    if (newFieldName.isNotBlank()) {
                                        fields = fields + CustomCategoryField(
                                            id = UUID.randomUUID().toString(),
                                            fieldName = newFieldName.trim(),
                                            fieldType = newFieldType,
                                            isRequired = newFieldRequired,
                                            order = fields.size
                                        )
                                        newFieldName = ""
                                        newFieldRequired = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                enabled = newFieldName.isNotBlank()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Field to Category")
                            }
                        }
                    }
                }

                // DIALOG ACTIONS
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val updated = (category ?: CustomCategory()).copy(
                                    name = name.trim(),
                                    isActive = isActive,
                                    allowPublicSubmissions = allowPublicSubmissions,
                                    fields = fields
                                )
                                onSave(updated)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save Category", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
