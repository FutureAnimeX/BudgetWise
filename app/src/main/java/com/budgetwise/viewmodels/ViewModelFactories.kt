package com.budgetwise.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.budgetwise.data.repository.*

class AuthViewModelFactory(private val userRepo: UserRepository, private val categoryRepo: CategoryRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(userRepo, categoryRepo) as T
}

class ExpenseViewModelFactory(private val repo: ExpenseRepository, private val userId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ExpenseViewModel(repo, userId) as T
}

class CategoryViewModelFactory(private val repo: CategoryRepository, private val userId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CategoryViewModel(repo, userId) as T
}

class BadgeViewModelFactory(private val repo: BadgeRepository, private val userId: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BadgeViewModel(repo, userId) as T
}
