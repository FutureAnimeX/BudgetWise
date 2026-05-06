package com.budgetwise.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.data.models.Category
import com.budgetwise.data.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val repo: CategoryRepository, private val userId: Int) : ViewModel() {
    val categories: StateFlow<List<Category>> = repo.getAll(userId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addCategory(name: String, limitStr: String?, colorHex: String) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        repo.insert(Category(userId = userId, name = name.trim(), spendingLimit = limitStr?.toDoubleOrNull(), colorHex = colorHex))
    }

    fun updateCategory(category: Category, newName: String, newLimit: String?, newColor: String) = viewModelScope.launch {
        if (newName.isBlank()) return@launch
        repo.update(category.copy(name = newName.trim(), spendingLimit = newLimit?.toDoubleOrNull(), colorHex = newColor))
    }

    fun deleteCategory(category: Category) = viewModelScope.launch { repo.delete(category) }
}
