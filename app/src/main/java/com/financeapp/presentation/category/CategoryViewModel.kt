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

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedTab: CategoryType = CategoryType.EXPENSE,
    val isLoading: Boolean = true,
    val editingCategory: Category? = null,
    val editName: String = "",
    val editType: CategoryType = CategoryType.EXPENSE,
    val editIcon: String = "category",
    val editColor: String = "#9C27B0",
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
        .map { state -> state.categories.filter { it.type == state.selectedTab || it.type == CategoryType.BOTH } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectTab(type: CategoryType) = _uiState.update { it.copy(selectedTab = type) }

    fun startAdd() {
        _uiState.update {
            it.copy(editingCategory = null, editName = "", editType = it.selectedTab, editIcon = "category", editColor = "#9C27B0", isSaved = false)
        }
    }

    fun startEdit(category: Category) {
        _uiState.update {
            it.copy(editingCategory = category, editName = category.name, editType = category.type, editIcon = category.icon, editColor = category.color, isSaved = false)
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(editName = name) }
    fun setType(type: CategoryType) = _uiState.update { it.copy(editType = type) }
    fun setIcon(icon: String) = _uiState.update { it.copy(editIcon = icon) }
    fun setColor(color: String) = _uiState.update { it.copy(editColor = color) }

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
                color = state.editColor
            )
            if (state.editingCategory == null) addCategoryUseCase(category)
            else updateCategoryUseCase(category)
            _uiState.update { it.copy(isSaved = true) }
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
