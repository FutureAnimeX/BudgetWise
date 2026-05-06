package com.budgetwise.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.data.models.User
import com.budgetwise.data.repository.CategoryRepository
import com.budgetwise.data.repository.UserRepository
import com.budgetwise.utils.SecurityUtils
import com.budgetwise.utils.Validators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val userRepo: UserRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun register(username: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        Validators.validateUsername(username)?.let { _state.value = AuthState.Error(it); return@launch }
        Validators.validatePassword(password)?.let { _state.value = AuthState.Error(it); return@launch }
        if (userRepo.login(username) != null) { _state.value = AuthState.Error("Username already taken"); return@launch }
        val hash = SecurityUtils.hashPassword(password)
        val id = userRepo.register(username, hash)
        val user = User(id = id.toInt(), username = username, passwordHash = hash)
        categoryRepo.seedDefaults(id.toInt())
        _currentUser.value = user
        _state.value = AuthState.Success(user)
    }

    fun login(username: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        if (username.isBlank()) { _state.value = AuthState.Error("Username is required"); return@launch }
        if (password.isBlank()) { _state.value = AuthState.Error("Password is required"); return@launch }
        val user = userRepo.login(username)
        if (user == null || !SecurityUtils.verifyPassword(password, user.passwordHash)) {
            _state.value = AuthState.Error("Invalid username or password"); return@launch
        }
        _currentUser.value = user
        _state.value = AuthState.Success(user)
    }

    fun logout() { _currentUser.value = null; _state.value = AuthState.Idle }
    fun clearError() { if (_state.value is AuthState.Error) _state.value = AuthState.Idle }
}
