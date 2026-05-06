package com.budgetwise.data.repository

import com.budgetwise.data.db.*
import com.budgetwise.data.models.*
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {
    suspend fun register(username: String, passwordHash: String): Long =
        dao.insert(User(username = username, passwordHash = passwordHash))
    suspend fun login(username: String): User? = dao.findByUsername(username)
    suspend fun updateBudget(user: User, budget: Double) = dao.update(user.copy(monthlyBudget = budget))
}

class CategoryRepository(private val dao: CategoryDao) {
    fun getAll(userId: Int): Flow<List<Category>> = dao.getAll(userId)
    suspend fun insert(category: Category) = dao.insert(category)
    suspend fun update(category: Category) = dao.update(category)
    suspend fun delete(category: Category) = dao.delete(category)
    suspend fun seedDefaults(userId: Int) {
        listOf(
            Category(userId = userId, name = "Groceries",  colorHex = "#02C39A"),
            Category(userId = userId, name = "Transport",  colorHex = "#028090"),
            Category(userId = userId, name = "Dining Out", colorHex = "#F96167"),
            Category(userId = userId, name = "Housing",    colorHex = "#8B5CF6"),
            Category(userId = userId, name = "Health",     colorHex = "#F9C234"),
            Category(userId = userId, name = "Other",      colorHex = "#94A3B8"),
        ).forEach { dao.insert(it) }
    }
}

class ExpenseRepository(private val dao: ExpenseDao) {
    fun getAll(userId: Int): Flow<List<Expense>> = dao.getAll(userId)
    fun getByDateRange(userId: Int, startMs: Long, endMs: Long): Flow<List<Expense>> = dao.getByDateRange(userId, startMs, endMs)
    fun getCategorySpendingSummary(userId: Int, startMs: Long, endMs: Long): Flow<List<CategorySpendingSummary>> = dao.getCategorySpendingSummary(userId, startMs, endMs)
    fun getTotalSpent(userId: Int, startMs: Long, endMs: Long): Flow<Double> = dao.getTotalSpent(userId, startMs, endMs)
    suspend fun insert(expense: Expense): Long = dao.insert(expense)
    suspend fun update(expense: Expense) = dao.update(expense)
    suspend fun delete(expense: Expense) = dao.delete(expense)
}

class BadgeRepository(private val dao: BadgeDao) {
    fun getAll(userId: Int): Flow<List<Badge>> = dao.getAll(userId)
    suspend fun hasEarned(userId: Int, key: String): Boolean = dao.hasEarned(userId, key) > 0
    suspend fun award(userId: Int, key: String) {
        if (!hasEarned(userId, key)) dao.insert(Badge(userId = userId, badgeKey = key))
    }
}
