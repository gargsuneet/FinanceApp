package com.financeapp.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.financeapp.FinanceApplication
import com.financeapp.domain.model.Category
import com.financeapp.domain.model.CategoryType
import com.financeapp.domain.usecase.AddCategoryUseCase
import com.financeapp.domain.usecase.DeleteCategoryUseCase
import com.financeapp.domain.usecase.GetCategoriesUseCase
import com.financeapp.domain.usecase.UpdateCategoryUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoryGroup(
    val parent: Category,
    val children: List<Category>,
    val isExpanded: Boolean = true
)

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedTab: CategoryType = CategoryType.EXPENSE,
    val isLoading: Boolean = true,
    val editingCategory: Category? = null,
    val editName: String = "",
    val editType: CategoryType = CategoryType.EXPENSE,
    val editIcon: String = "category",
    val editColor: String = "#9C27B0",
    val editParentId: Long? = null,
    val collapsedParents: Set<Long> = emptySet(),
    val isSaved: Boolean = false,
    val error: String? = null
)

class CategoryViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            }
        }
    }

    val filteredCategories: StateFlow<List<Category>> = uiState
        .map { state ->
            state.categories.filter { it.type == state.selectedTab || it.type == CategoryType.BOTH }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val groupedCategories: StateFlow<List<CategoryGroup>> = uiState
        .map { state ->
            val typeFiltered = state.categories.filter {
                it.type == state.selectedTab || it.type == CategoryType.BOTH
            }
            val parents = typeFiltered.filter { it.parentId == null }
            val childMap = typeFiltered.filter { it.parentId != null }.groupBy { it.parentId }
            parents.map { parent ->
                CategoryGroup(
                    parent = parent,
                    children = childMap[parent.id] ?: emptyList(),
                    isExpanded = parent.id !in state.collapsedParents
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectTab(type: CategoryType) = _uiState.update { it.copy(selectedTab = type) }

    fun toggleExpand(parentId: Long) {
        _uiState.update { state ->
            val collapsed = state.collapsedParents.toMutableSet()
            if (parentId in collapsed) collapsed.remove(parentId) else collapsed.add(parentId)
            state.copy(collapsedParents = collapsed)
        }
    }

    fun startAdd(parentId: Long? = null) {
        _uiState.update {
            it.copy(
                editingCategory = null,
                editName = "",
                editType = if (parentId != null) {
                    // Inherit parent type
                    it.categories.find { c -> c.id == parentId }?.type ?: it.selectedTab
                } else it.selectedTab,
                editIcon = "category",
                editColor = "#9C27B0",
                editParentId = parentId,
                isSaved = false,
                error = null
            )
        }
    }

    fun startEdit(category: Category) {
        _uiState.update {
            it.copy(
                editingCategory = category,
                editName = category.name,
                editType = category.type,
                editIcon = category.icon,
                editColor = category.color,
                editParentId = category.parentId,
                isSaved = false,
                error = null
            )
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(editName = name) }
    fun setType(type: CategoryType) = _uiState.update { it.copy(editType = type) }
    fun setIcon(icon: String) = _uiState.update { it.copy(editIcon = icon) }
    fun setColor(color: String) = _uiState.update { it.copy(editColor = color) }
    fun setParentId(id: Long?) = _uiState.update { it.copy(editParentId = id) }

    fun save() {
        val state = _uiState.value
        if (state.editName.isBlank()) {
            _uiState.update { it.copy(error = "Category name is required") }
            return
        }
        viewModelScope.launch {
            val category = Category(
                id = state.editingCategory?.id ?: 0L,
                name = state.editName,
                type = state.editType,
                icon = state.editIcon,
                color = state.editColor,
                parentId = state.editParentId
            )
            if (state.editingCategory == null) addCategoryUseCase(category)
            else updateCategoryUseCase(category)
            _uiState.update { it.copy(isSaved = true, error = null) }
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch { deleteCategoryUseCase(category) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    class Factory(private val app: FinanceApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CategoryViewModel(
                app.getCategoriesUseCase,
                app.addCategoryUseCase,
                app.updateCategoryUseCase,
                app.deleteCategoryUseCase
            ) as T
    }
}
