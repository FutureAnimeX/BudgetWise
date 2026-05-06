package com.budgetwise.data.db

import androidx.room.*
import com.budgetwise.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getAll(userId: Int): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAll(userId: Int): Flow<List<Expense>>

    @Query("""
        SELECT * FROM expenses
        WHERE userId = :userId AND date BETWEEN :startMs AND :endMs
        ORDER BY date DESC
    """)
    fun getByDateRange(userId: Int, startMs: Long, endMs: Long): Flow<List<Expense>>

    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName,
               COALESCE(SUM(e.amount), 0.0) AS totalSpent, c.spendingLimit
        FROM categories c
        LEFT JOIN expenses e ON e.categoryId = c.id
          AND e.userId = :userId AND e.date BETWEEN :startMs AND :endMs
        WHERE c.userId = :userId
        GROUP BY c.id
    """)
    fun getCategorySpendingSummary(userId: Int, startMs: Long, endMs: Long): Flow<List<CategorySpendingSummary>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE userId = :userId AND date BETWEEN :startMs AND :endMs")
    fun getTotalSpent(userId: Int, startMs: Long, endMs: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getAll(userId: Int): Flow<List<Badge>>

    @Query("SELECT COUNT(*) FROM badges WHERE userId = :userId AND badgeKey = :key")
    suspend fun hasEarned(userId: Int, key: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: Badge)
}
