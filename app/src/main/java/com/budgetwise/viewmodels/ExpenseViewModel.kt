package com.budgetwise.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.data.models.Expense
import com.budgetwise.data.repository.ExpenseRepository
import com.budgetwise.utils.Validators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class ExpenseViewModel(
    private val repo: ExpenseRepository,
    private val userId: Int
) : ViewModel() {

    private val _startMs = MutableStateFlow(currentMonthStart())
    private val _endMs   = MutableStateFlow(currentMonthEnd())
    val startMs: StateFlow<Long> = _startMs
    val endMs:   StateFlow<Long> = _endMs

    val expenses: StateFlow<List<Expense>> = combine(_startMs, _endMs) { s, e -> s to e }
        .flatMapLatest { (s, e) -> repo.getByDateRange(userId, s, e) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalSpent: StateFlow<Double> = combine(_startMs, _endMs) { s, e -> s to e }
        .flatMapLatest { (s, e) -> repo.getTotalSpent(userId, s, e) }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val categorySpending = combine(_startMs, _endMs) { s, e -> s to e }
        .flatMapLatest { (s, e) -> repo.getCategorySpendingSummary(userId, s, e) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _operationResult = MutableSharedFlow<Result<Unit>>()
    val operationResult = _operationResult.asSharedFlow()

    fun addExpense(amountStr: String, dateMs: Long, description: String, categoryId: Int, receiptPath: String? = null) =
        viewModelScope.launch {
            Validators.validateAmount(amountStr)?.let { _operationResult.emit(Result.failure(Exception(it))); return@launch }
            Validators.validateDescription(description)?.let { _operationResult.emit(Result.failure(Exception(it))); return@launch }
            repo.insert(Expense(userId = userId, amount = amountStr.toDouble(), date = dateMs, description = description, categoryId = categoryId, receiptPath = receiptPath))
            _operationResult.emit(Result.success(Unit))
        }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repo.update(expense); _operationResult.emit(Result.success(Unit))
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch { repo.delete(expense) }

    fun setDateRange(startMs: Long, endMs: Long) { _startMs.value = startMs; _endMs.value = endMs }

    private fun currentMonthStart(): Long =
        LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun currentMonthEnd(): Long =
        LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
