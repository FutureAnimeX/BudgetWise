package com.budgetwise.ui.expenses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.budgetwise.data.models.Category
import com.budgetwise.data.models.Expense
import com.budgetwise.viewmodels.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(existingExpense: Expense? = null, categories: List<Category>, expenseVm: ExpenseViewModel, onBack: () -> Unit) {
    val isEdit = existingExpense != null
    var amount      by remember { mutableStateOf(existingExpense?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(existingExpense?.description ?: "") }
    var selectedCat by remember { mutableIntStateOf(existingExpense?.categoryId ?: categories.firstOrNull()?.id ?: 0) }
    var dateMs      by remember { mutableLongStateOf(existingExpense?.date ?: System.currentTimeMillis()) }
    var receiptUri  by remember { mutableStateOf<Uri?>(existingExpense?.receiptPath?.let { Uri.parse(it) }) }
    var showDatePicker by remember { mutableStateOf(false) }
    var catExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> receiptUri = uri }

    LaunchedEffect(Unit) {
        expenseVm.operationResult.collect { result ->
            if (result.isSuccess) onBack() else amountError = result.exceptionOrNull()?.message
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (isEdit) "Edit Expense" else "Add Expense") }, navigationIcon = { IconButton(onClick = onBack) { Text("←") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(value = amount, onValueChange = { amount = it; amountError = null }, label = { Text("Amount (R)") }, isError = amountError != null, supportingText = amountError?.let { { Text(it) } }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = fmt.format(Date(dateMs)), onValueChange = {}, label = { Text("Date") }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true })
            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(value = categories.find { it.id == selectedCat }?.name ?: "Select category", onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categories.forEach { cat -> DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCat = cat.id; catExpanded = false }) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { imageLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (receiptUri != null) "Change Receipt" else "Attach Receipt 📷") }
                if (receiptUri != null) TextButton(onClick = { receiptUri = null }) { Text("Remove") }
            }
            receiptUri?.let { AsyncImage(model = it, contentDescription = "Receipt", modifier = Modifier.fillMaxWidth().height(180.dp)) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                if (isEdit) expenseVm.updateExpense(existingExpense!!.copy(amount = amount.toDoubleOrNull() ?: 0.0, description = description, categoryId = selectedCat, date = dateMs, receiptPath = receiptUri?.toString()))
                else expenseVm.addExpense(amount, dateMs, description, selectedCat, receiptUri?.toString())
            }, modifier = Modifier.fillMaxWidth()) { Text(if (isEdit) "Save Changes" else "Add Expense") }
        }
    }
    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMs)
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { dateMs = it }; showDatePicker = false }) { Text("OK") } }) { DatePicker(state) }
    }
}
