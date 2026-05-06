package com.budgetwise.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// ─── User ────────────────────────────────────────────────
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val monthlyBudget: Double = 0.0
)

// ─── Category ────────────────────────────────────────────
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val spendingLimit: Double? = null,
    val colorHex: String = "#028090"
)

// ─── Expense (Parcelable for nav savedStateHandle) ───────
@Parcelize
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val amount: Double,
    val date: Long,
    val description: String,
    val categoryId: Int,
    val receiptPath: String? = null
) : Parcelable

// ─── Badge ───────────────────────────────────────────────
@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val badgeKey: String,
    val earnedAt: Long = System.currentTimeMillis()
)

// ─── Summary projection (not a Room table) ───────────────
data class CategorySpendingSummary(
    val categoryId: Int,
    val categoryName: String,
    val totalSpent: Double,
    val spendingLimit: Double?
)
