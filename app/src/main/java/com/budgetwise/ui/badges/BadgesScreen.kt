package com.budgetwise.ui.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgetwise.viewmodels.BadgeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(vm: BadgeViewModel, onBack: () -> Unit) {
    val earnedBadges by vm.badges.collectAsState()
    val earnedKeys = earnedBadges.map { it.badgeKey }.toSet()
    val fmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("My Badges") }, navigationIcon = { IconButton(onClick = onBack) { Text("←") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${earnedKeys.size} / ${BadgeViewModel.ALL_BADGES.size} Badges Earned", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    LinearProgressIndicator(progress = { earnedKeys.size.toFloat() / BadgeViewModel.ALL_BADGES.size }, modifier = Modifier.fillMaxWidth().height(8.dp))
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(BadgeViewModel.ALL_BADGES.entries.toList()) { (key, info) ->
                    val isEarned = key in earnedKeys
                    val earnedAt = earnedBadges.find { it.badgeKey == key }?.earnedAt
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().alpha(if (isEarned) 1f else 0.45f),
                        colors = CardDefaults.cardColors(containerColor = if (isEarned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(60.dp).background(if (isEarned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape), contentAlignment = Alignment.Center) {
                                Text(if (isEarned) info.emoji else "🔒", fontSize = 26.sp)
                            }
                            Text(info.title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 14.sp)
                            Text(info.description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (isEarned && earnedAt != null) Text("Earned ${fmt.format(Date(earnedAt))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
