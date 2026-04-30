package com.financeapp.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.financeapp.domain.model.Category
import com.financeapp.domain.model.CategoryType
import com.financeapp.presentation.components.ACCOUNT_COLORS
import com.financeapp.presentation.components.CATEGORY_ICONS
import com.financeapp.presentation.components.categoryIconVector
import com.financeapp.presentation.components.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val filtered by viewModel.filteredCategories.collectAsState()
    var showAddEditSheet by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) showAddEditSheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.startAdd()
                showAddEditSheet = true
            }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color.White)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = if (state.selectedTab == CategoryType.EXPENSE) 0 else 1,
                containerColor = Color.White
            ) {
                Tab(
                    selected = state.selectedTab == CategoryType.EXPENSE,
                    onClick = { viewModel.selectTab(CategoryType.EXPENSE) },
                    text = { Text("Expense") }
                )
                Tab(
                    selected = state.selectedTab == CategoryType.INCOME,
                    onClick = { viewModel.selectTab(CategoryType.INCOME) },
                    text = { Text("Income") }
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { category ->
                    CategoryItem(
                        category = category,
                        onClick = {
                            viewModel.startEdit(category)
                            showAddEditSheet = true
                        },
                        onDelete = { if (!category.isDefault) categoryToDelete = category }
                    )
                }
            }
        }
    }

    if (showAddEditSheet) {
        ModalBottomSheet(onDismissRequest = { showAddEditSheet = false }) {
            AddEditCategoryContent(
                state = state,
                viewModel = viewModel,
                onDismiss = { showAddEditSheet = false }
            )
        }
    }

    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category") },
            text = { Text("Delete '${cat.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(cat)
                    categoryToDelete = null
                }) { Text("Delete", color = Color(0xFFF44336)) }
            },
            dismissButton = { TextButton(onClick = { categoryToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit, onDelete: () -> Unit) {
    val color = parseColor(category.color)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIconVector(category.icon), contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(category.type.name, fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }
            if (!category.isDefault) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditCategoryContent(
    state: CategoryUiState,
    viewModel: CategoryViewModel,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            if (state.editingCategory == null) "Add Category" else "Edit Category",
            fontWeight = FontWeight.Bold, fontSize = 18.sp
        )

        OutlinedTextField(
            value = state.editName,
            onValueChange = viewModel::setName,
            label = { Text("Category Name *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(CategoryType.EXPENSE, CategoryType.INCOME, CategoryType.BOTH).forEach { type ->
                FilterChip(
                    selected = state.editType == type,
                    onClick = { viewModel.setType(type) },
                    label = { Text(type.name, fontSize = 12.sp) }
                )
            }
        }

        Text("Icon", fontSize = 12.sp, color = Color(0xFF757575))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CATEGORY_ICONS.take(20)) { iconName ->
                val isSelected = state.editIcon == iconName
                val catColor = parseColor(state.editColor)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) catColor else catColor.copy(alpha = 0.1f))
                        .clickable { viewModel.setIcon(iconName) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        categoryIconVector(iconName),
                        contentDescription = null,
                        tint = if (isSelected) Color.White else catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Text("Color", fontSize = 12.sp, color = Color(0xFF757575))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ACCOUNT_COLORS.take(9).forEach { colorHex ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                        .clickable { viewModel.setColor(colorHex) }
                        .then(if (state.editColor == colorHex) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier)
                )
            }
        }

        state.error?.let { Text(it, color = Color(0xFFF44336), fontSize = 12.sp) }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = viewModel::save, modifier = Modifier.weight(1f)) { Text("Save") }
        }
    }
}
