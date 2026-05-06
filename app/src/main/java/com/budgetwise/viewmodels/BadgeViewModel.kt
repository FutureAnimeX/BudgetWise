package com.budgetwise.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetwise.data.models.Badge
import com.budgetwise.data.repository.BadgeRepository
import com.budgetwise.utils.BadgeKeys
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BadgeViewModel(private val repo: BadgeRepository, private val userId: Int) : ViewModel() {
    val badges: StateFlow<List<Badge>> = repo.getAll(userId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    companion object {
        data class BadgeInfo(val emoji: String, val title: String, val description: String)
        val ALL_BADGES = linkedMapOf(
            BadgeKeys.FIRST_ENTRY    to BadgeInfo("🌟", "First Entry",     "Added your very first expense"),
            BadgeKeys.BUDGET_MASTER  to BadgeInfo("🏆", "Budget Master",   "Stayed within your monthly budget"),
            BadgeKeys.SUPER_SAVER   to BadgeInfo("💰", "Super Saver",     "Spent less than 80% of your budget"),
            BadgeKeys.CATEGORY_CHAMP to BadgeInfo("📊", "Category Champ", "All categories stayed under their limits"),
            BadgeKeys.LOGGING_LEGEND to BadgeInfo("📝", "Logging Legend", "Logged expenses 7 days in a row"),
            BadgeKeys.ON_A_ROLL      to BadgeInfo("🔥", "On a Roll",      "Logged expenses 30 days in a row"),
        )
    }
}
