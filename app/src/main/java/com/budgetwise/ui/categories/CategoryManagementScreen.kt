package com.budgetwise.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetwise.data.models.Category
import com.budgetwise.viewmodels.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(vm: CategoryViewModel, onBack: () -> Unit) {
    val categories by vm.categories.collectAsState()
    var showDialog  by remember { mutableStateOf(false) }
    var editTarget  by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Categories") }, navigationIcon = { IconButton(onClick = onBack) { Text("←") } }) },
        floatingActionButton = { FloatingActionButton(onClick = { editTarget = null; showDialog = true }) { Icon(Icons.Default.Add, "Add") } }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Spacer(Modifier.height(8.dp)) }
            items(categories, key = { it.id }) { cat ->
                val color = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(MaterialTheme.colorScheme.primary)
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).background(color, CircleShape))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                            cat.spendingLimit?.let { Text("Limit: R %.2f".format(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        IconButton(onClick = { editTarget = cat; showDialog = true }) { Icon(Icons.Default.Edit, "Edit") }
                        IconButton(onClick = { deleteTarget = cat }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showDialog) {
        var name  by remember { mutableStateOf(editTarget?.name ?: "") }
        var limit by remember { mutableStateOf(editTarget?.spendingLimit?.toString() ?: "") }
        var color by remember { mutableStateOf(editTarget?.colorHex ?: "#028090") }
        var nameError by remember { mutableStateOf<String?>(null) }
        val colorOptions = listOf("#028090","#02C39A","#F96167","#F9C234","#8B5CF6","#94A3B8","#14B8A6","#FB923C")

        AlertDialog(onDismissRequest = { showDialog = false }, title = { Text(if (editTarget != null) "Edit Category" else "New Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it; nameError = null }, label = { Text("Category Name") }, isError = nameError != null, supportingText = nameError?.let { { Text(it) } }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Spending Limit (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Colour", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { hex ->
                            val c = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                            Box(Modifier.size(if (color == hex) 30.dp else 24.dp).background(c, CircleShape).clickable(MutableInteractionSource(), null) { color = hex })
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { if (name.isBlank()) { nameError = "Name is required"; return@TextButton }; if (editTarget != null) vm.updateCategory(editTarget!!, name, limit.ifBlank { null }, color) else vm.addCategory(name, limit.ifBlank { null }, color); showDialog = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }

    deleteTarget?.let { cat ->
        AlertDialog(onDismissRequest = { deleteTarget = null }, title = { Text("Delete Category") }, text = { Text("Delete \"${cat.name}\"?") },
            confirmButton = { TextButton(onClick = { vm.deleteCategory(cat); deleteTarget = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } }
        )
    }
}
