package com.dijitalkalkan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dijitalkalkan.app.data.UsageEventsHelper

/**
 * Sadece Ebeveyn Paneli'nden erişilebilen ekran: bugün hangi uygulama saat
 * kaçta açıldı / kaçta kapandı listesini gösterir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val events = remember { UsageEventsHelper.getTodayEvents(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Etkinlik Geçmişi (Bugün)") }) }
    ) { padding: PaddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (events.isEmpty()) {
                Text("Bugün için henüz kayıt yok.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(events) { e ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${e.appLabel} ${if (e.opened) "açıldı" else "kapandı"}")
                            Text(text = e.timeText())
                        }
                    }
                }
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text("Geri")
            }
        }
    }
}
