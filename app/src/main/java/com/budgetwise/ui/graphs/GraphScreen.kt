package com.budgetwise.ui.graphs

import android.graphics.Color as AColor
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.budgetwise.data.models.CategorySpendingSummary
import com.budgetwise.viewmodels.ExpenseViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(vm: ExpenseViewModel, onBack: () -> Unit) {
    val categorySpending by vm.categorySpending.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(topBar = { TopAppBar(title = { Text("Spending Graphs") }, navigationIcon = { IconButton(onClick = onBack) { Text("←") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("By Category", Modifier.padding(12.dp)) }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Pie Chart", Modifier.padding(12.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            when (selectedTab) {
                0 -> CategoryBarChart(categorySpending)
                1 -> CategoryPieChart(categorySpending)
            }
        }
    }
}

@Composable
fun CategoryBarChart(data: List<CategorySpendingSummary>) {
    if (data.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available") }; return }
    AndroidView(factory = { context ->
        BarChart(context).apply {
            description.isEnabled = false; setDrawGridBackground(false); setFitBars(true); animateY(600)
            legend.textColor = AColor.WHITE
            xAxis.apply { position = XAxis.XAxisPosition.BOTTOM; granularity = 1f; textColor = AColor.WHITE; setDrawGridLines(false); labelRotationAngle = -30f }
            axisLeft.apply { textColor = AColor.WHITE; axisMinimum = 0f }
            axisRight.isEnabled = false
            setBackgroundColor(AColor.parseColor("#0A1628"))
        }
    }, update = { chart ->
        val entries = data.mapIndexed { i, s -> BarEntry(i.toFloat(), s.totalSpent.toFloat()) }
        val dataSet = BarDataSet(entries, "Spending").apply { colors = ColorTemplate.MATERIAL_COLORS.toList(); valueTextColor = AColor.WHITE; valueTextSize = 11f }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.categoryName })
        chart.data = BarData(dataSet); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(380.dp).padding(horizontal = 8.dp))
}

@Composable
fun CategoryPieChart(data: List<CategorySpendingSummary>) {
    if (data.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data available") }; return }
    AndroidView(factory = { context ->
        PieChart(context).apply {
            description.isEnabled = false; isDrawHoleEnabled = true; setHoleColor(AColor.parseColor("#0A1628"))
            holeRadius = 45f; setUsePercentValues(true); setEntryLabelColor(AColor.WHITE); setEntryLabelTextSize(11f)
            legend.textColor = AColor.WHITE; animateY(800); setBackgroundColor(AColor.parseColor("#0A1628"))
        }
    }, update = { chart ->
        val entries = data.filter { it.totalSpent > 0 }.map { PieEntry(it.totalSpent.toFloat(), it.categoryName) }
        val dataSet = PieDataSet(entries, "Categories").apply { colors = ColorTemplate.MATERIAL_COLORS.toList(); valueTextColor = AColor.WHITE; valueTextSize = 12f; sliceSpace = 3f }
        chart.data = PieData(dataSet); chart.invalidate()
    }, modifier = Modifier.fillMaxWidth().height(380.dp))
}
