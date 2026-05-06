package com.budgetwise.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetwise.data.models.CategorySpendingSummary
import com.budgetwise.viewmodels.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    expenseVm: ExpenseViewModel,
    monthlyBudget: Double,
    onAddExpense: () -> Unit,
    onViewAll: () -> Unit,
    onSetBudget: () -> Unit,
    onLogout: () -> Unit
) {
    val totalSpent      by expenseVm.totalSpent.collectAsState()
    val categorySummary by expenseVm.categorySpending.collectAsState()
    val remaining = monthlyBudget - totalSpent
    val progress  = if (monthlyBudget > 0) (totalSpent / monthlyBudget).coerceIn(0.0, 1.0).toFloat() else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BudgetWise") },
                actions = {
                    TextButton(onClick = onSetBudget) { Text("Set Budget") }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddExpense, text = { Text("Add Expense") }, icon = { Text("+", fontSize = 20.sp) })
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Spacer(Modifier.height(16.dp)); Text("Budget Overview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            item { BudgetSummaryCard(totalSpent, monthlyBudget, remaining, progress) }
            item { Text("Spending by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(categorySummary) { CategoryProgressRow(it) }
            item { TextButton(onClick = onViewAll, modifier = Modifier.fillMaxWidth()) { Text("View All Expenses →") }; Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BudgetSummaryCard(totalSpent: Double, monthlyBudget: Double, remaining: Double, progress: Float) {
    val overBudget = remaining < 0
    val progressColor = when { progress < 0.7f -> MaterialTheme.colorScheme.primary; progress < 0.9f -> Color(0xFFF9C234); else -> Color(0xFFF96167) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatLabel("Spent", "R %.2f".format(totalSpent))
                StatLabel("Budget", "R %.2f".format(monthlyBudget))
                StatLabel(if (overBudget) "Over by" else "Remaining", "R %.2f".format(Math.abs(remaining)), if (overBudget) Color(0xFFF96167) else MaterialTheme.colorScheme.primary)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp), color = progressColor, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            if (overBudget) Text("⚠️ You've exceeded your monthly budget!", color = Color(0xFFF96167), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun StatLabel(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = valueColor, fontSize = 16.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CategoryProgressRow(summary: CategorySpendingSummary) {
    val limit = summary.spendingLimit ?: 0.0
    val hasLimit = limit > 0
    val progress = if (hasLimit) (summary.totalSpent / limit).coerceIn(0.0, 1.0).toFloat() else 0f
    val overLimit = hasLimit && summary.totalSpent > limit
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(summary.categoryName, fontWeight = FontWeight.Medium)
                    Text("R %.2f".format(summary.totalSpent) + if (hasLimit) " / R %.2f".format(limit) else "", style = MaterialTheme.typography.bodySmall, color = if (overLimit) Color(0xFFF96167) else MaterialTheme.colorScheme.onSurface)
                }
                if (hasLimit) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = if (overLimit) Color(0xFFF96167) else MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}
