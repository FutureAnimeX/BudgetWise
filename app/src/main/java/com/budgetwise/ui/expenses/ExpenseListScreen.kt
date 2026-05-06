package com.budgetwise.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budgetwise.data.models.Category
import com.budgetwise.data.models.Expense
import com.budgetwise.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(vm: ExpenseViewModel, categories: List<Category>, onEdit: (Expense) -> Unit, onBack: () -> Unit) {
    val expenses by vm.expenses.collectAsState()
    val total    by vm.totalSpent.collectAsState()
    val startMs  by vm.startMs.collectAsState()
    val endMs    by vm.endMs.collectAsState()
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }
    var deleteTarget    by remember { mutableStateOf<Expense?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("All Expenses") }, navigationIcon = { IconButton(onClick = onBack) { Text("←") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = { showStartPicker = true }, label = { Text("From: ${fmt.format(Date(startMs))}") }, modifier = Modifier.weight(1f))
                FilterChip(selected = false, onClick = { showEndPicker = true }, label = { Text("To: ${fmt.format(Date(endMs))}") }, modifier = Modifier.weight(1f))
            }
            Text("Total: R %.2f".format(total), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            if (expenses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No expenses for this period.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseListItem(expense, categories.find { it.id == expense.categoryId }?.name ?: "Unknown", { onEdit(expense) }, { deleteTarget = expense })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
    if (showStartPicker) { val state = rememberDatePickerState(initialSelectedDateMillis = startMs); DatePickerDialog(onDismissRequest = { showStartPicker = false }, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { vm.setDateRange(it, endMs) }; showStartPicker = false }) { Text("OK") } }) { DatePicker(state) } }
    if (showEndPicker)   { val state = rememberDatePickerState(initialSelectedDateMillis = endMs);   DatePickerDialog(onDismissRequest = { showEndPicker = false },   confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { vm.setDateRange(startMs, it) }; showEndPicker = false }) { Text("OK") } }) { DatePicker(state) } }
    deleteTarget?.let { expense ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text("Delete Expense") }, text = { Text("Remove \"${expense.description}\"?") },
            confirmButton = { TextButton(onClick = { vm.deleteExpense(expense); deleteTarget = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } })
    }
}

@Composable
fun ExpenseListItem(expense: Expense, categoryName: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { onEdit() }) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                Text("$categoryName  ·  ${fmt.format(Date(expense.date))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!expense.receiptPath.isNullOrBlank()) Text("📎 Receipt attached", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("R %.2f".format(expense.amount), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}
