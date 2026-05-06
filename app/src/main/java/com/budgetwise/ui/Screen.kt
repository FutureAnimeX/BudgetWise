package com.budgetwise.ui

sealed class Screen(val route: String) {
    object Login        : Screen("login")
    object Register     : Screen("register")
    object Dashboard    : Screen("dashboard")
    object AddExpense   : Screen("add_expense")
    object EditExpense  : Screen("edit_expense/{expenseId}") {
        fun createRoute(id: Int) = "edit_expense/$id"
    }
    object ExpenseList   : Screen("expense_list")
    object Categories    : Screen("categories")
    object Graphs        : Screen("graphs")
    object Badges        : Screen("badges")
    object BudgetSetting : Screen("budget_setting")
}
