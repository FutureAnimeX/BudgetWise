package com.budgetwise.utils

import com.budgetwise.data.models.CategorySpendingSummary
import com.budgetwise.data.repository.BadgeRepository

object BadgeKeys {
    const val FIRST_ENTRY       = "FIRST_ENTRY"
    const val BUDGET_MASTER     = "BUDGET_MASTER"
    const val CATEGORY_CHAMP    = "CATEGORY_CHAMP"
    const val LOGGING_LEGEND    = "LOGGING_LEGEND"
    const val ON_A_ROLL         = "ON_A_ROLL"
    const val SUPER_SAVER       = "SUPER_SAVER"
}

class BadgeEngine(private val badgeRepo: BadgeRepository) {
    suspend fun checkFirstEntry(userId: Int, totalExpenses: Int) {
        if (totalExpenses >= 1) badgeRepo.award(userId, BadgeKeys.FIRST_ENTRY)
    }
    suspend fun checkBudgetBadges(userId: Int, totalSpent: Double, monthlyBudget: Double) {
        if (monthlyBudget <= 0) return
        if (totalSpent <= monthlyBudget) badgeRepo.award(userId, BadgeKeys.BUDGET_MASTER)
        if (totalSpent <= monthlyBudget * 0.80) badgeRepo.award(userId, BadgeKeys.SUPER_SAVER)
    }
    suspend fun checkCategoryChamp(userId: Int, summaries: List<CategorySpendingSummary>) {
        val allWithin = summaries
            .filter { it.spendingLimit != null && it.spendingLimit > 0 }
            .all { it.totalSpent <= it.spendingLimit!! }
        if (allWithin && summaries.isNotEmpty()) badgeRepo.award(userId, BadgeKeys.CATEGORY_CHAMP)
    }
    suspend fun checkStreakBadges(userId: Int, consecutiveDays: Int) {
        if (consecutiveDays >= 7)  badgeRepo.award(userId, BadgeKeys.LOGGING_LEGEND)
        if (consecutiveDays >= 30) badgeRepo.award(userId, BadgeKeys.ON_A_ROLL)
    }
}
