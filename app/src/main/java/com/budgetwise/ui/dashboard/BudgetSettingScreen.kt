package com.budgetwise.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingScreen(currentBudget: Double, onSave: (Double) -> Unit, onBack: () -> Unit) {
    var budgetInput by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text("Set Monthly Budget") }, navigationIcon = { IconButton(onClick = onBack) { Text("←") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Monthly Budget", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Set the total amount you plan to spend this month.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(value = budgetInput, onValueChange = { budgetInput = it; error = null }, label = { Text("Amount (R)") }, prefix = { Text("R ") }, singleLine = true, isError = error != null, supportingText = error?.let { { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(onClick = {
                val value = budgetInput.toDoubleOrNull()
                when { budgetInput.isBlank() -> error = "Please enter an amount"; value == null -> error = "Enter a valid number"; value <= 0 -> error = "Budget must be greater than zero"; else -> { onSave(value); onBack() } }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save Budget") }
            if (currentBudget > 0) { Spacer(Modifier.height(12.dp)); Text("Current budget: R %.2f".format(currentBudget), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
