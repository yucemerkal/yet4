package com.dijitalkalkan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dijitalkalkan.app.data.AppUsage
import com.dijitalkalkan.app.data.PrefsManager
import com.dijitalkalkan.app.data.UsageStatsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHomeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val hasPermission = UsageStatsHelper.hasUsagePermission(context)
    var usage by remember { mutableStateOf<List<AppUsage>>(emptyList()) }
    val limits = remember { prefs.getLimits() }
    val extraToday = prefs.getExtraMinutesToday()

    if (hasPermission && usage.isEmpty()) {
        usage = UsageStatsHelper.getTodayUsage(context)
    }

    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bugünkü Kullanımım") }) }
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            if (!hasPermission) {
                item {
                    Text("Kullanım verilerini göstermek için izin gerekiyor.")
                    Button(
                        onClick = { UsageStatsHelper.openUsageAccessSettings(context) },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text("İzin Ver")
                    }
                }
            } else {
                if (extraToday > 0) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Text(
                                text = "Bugün kazanılan ek süre: +$extraToday dk",
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                items(usage) { app ->
                    val baseLimit = limits[app.packageName]
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = app.appLabel)
                        val limitText = if (baseLimit != null) " / ${baseLimit + extraToday} dk limit" else ""
                        Text(text = "${app.minutesToday} dk$limitText")
                    }
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "Ebeveyninden bir C-kod aldıysan buraya gir:")
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter { c -> c.isDigit() } },
                    label = { Text("C-kod") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        message = if (prefs.redeemSCode(code)) {
                            code = ""
                            "Ek süre eklendi!"
                        } else {
                            "Kod geçersiz veya daha önce kullanılmış."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Kodu Kullan")
                }
                message?.let { Text(text = it, modifier = Modifier.padding(top = 8.dp)) }

                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text("Geri")
                }
            }
        }
    }
}
