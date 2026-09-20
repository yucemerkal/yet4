package com.dijitalkalkan.app.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dijitalkalkan.app.admin.AppDeviceAdminReceiver
import com.dijitalkalkan.app.data.InstalledAppsHelper
import com.dijitalkalkan.app.data.PrefsManager
import com.dijitalkalkan.app.data.UsageStatsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(onBack: () -> Unit, onOpenActivityLog: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    var limits by remember { mutableStateOf(prefs.getLimits()) }
    val apps = remember { InstalledAppsHelper.getLaunchableApps(context) }

    val hasUsagePermission = UsageStatsHelper.hasUsagePermission(context)
    val accessibilityEnabled = remember { isAccessibilityServiceEnabled(context) }
    val isDeviceAdmin = remember { isDeviceAdminActive(context) }

    var codeCount by remember { mutableStateOf("3") }
    var codeMinutes by remember { mutableStateOf("15") }
    var generatedCodes by remember { mutableStateOf(prefs.getSCodes()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ebeveyn Paneli") }) }
    ) { padding: PaddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            item {
                Text(text = "Gerekli İzinler", modifier = Modifier.padding(bottom = 8.dp))

                if (!hasUsagePermission) {
                    PermissionRow(
                        text = "Kullanım Erişimi izni verilmemiş",
                        buttonText = "Aç",
                        onClick = { UsageStatsHelper.openUsageAccessSettings(context) }
                    )
                }
                if (!accessibilityEnabled) {
                    PermissionRow(
                        text = "Erişilebilirlik Servisi etkin değil (engelleme çalışmaz)",
                        buttonText = "Aç",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }
                if (!isDeviceAdmin) {
                    PermissionRow(
                        text = "Cihaz Yöneticisi etkin değil (silinme koruması yok)",
                        buttonText = "Aç",
                        onClick = {
                            val compName = ComponentName(context, AppDeviceAdminReceiver::class.java)
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "Çocuğun Dijital Koruyucu'yu kaldırmasını zorlaştırmak için gereklidir."
                                )
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                if (hasUsagePermission && accessibilityEnabled && isDeviceAdmin) {
                    Text(text = "Tüm izinler tamam ✓")
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Button(onClick = onOpenActivityLog, modifier = Modifier.fillMaxWidth()) {
                    Text("Etkinlik Geçmişini Gör (bugün, saat saat)")
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "Ayarlar")
                var oldCode by remember { mutableStateOf("") }
                var newCode by remember { mutableStateOf("") }
                var pwMessage by remember { mutableStateOf<String?>(null) }

                OutlinedTextField(
                    value = oldCode,
                    onValueChange = { oldCode = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Mevcut Şifre") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newCode,
                    onValueChange = { newCode = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Yeni Şifre") },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    singleLine = true
                )
                Button(
                    onClick = {
                        pwMessage = when {
                            !prefs.verifyPCode(oldCode) -> "Mevcut şifre yanlış."
                            newCode.length < 4 -> "Yeni şifre en az 4 haneli olmalı."
                            else -> {
                                prefs.setPCode(newCode)
                                oldCode = ""
                                newCode = ""
                                "Şifre güncellendi."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Şifreyi Değiştir") }
                pwMessage?.let { Text(text = it, modifier = Modifier.padding(top = 4.dp)) }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "Uygulama Süre Limitleri (dakika/gün, 0 = sınırsız)")
            }

            items(apps) { app ->
                var value by remember(app.packageName) {
                    mutableStateOf((limits[app.packageName] ?: 0).let { if (it == 0) "" else it.toString() })
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = app.label, modifier = Modifier.padding(top = 14.dp))
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            value = it.filter { c -> c.isDigit() }
                            val minutes = value.toIntOrNull() ?: 0
                            prefs.setLimit(app.packageName, minutes)
                            limits = prefs.getLimits()
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                }
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(text = "C-kod Üret (çocuğa ek süre vermek için tek kullanımlık kod)")

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = codeCount,
                        onValueChange = { codeCount = it.filter { c -> c.isDigit() } },
                        label = { Text("Kaç kod") },
                        modifier = Modifier.width(150.dp)
                    )
                    OutlinedTextField(
                        value = codeMinutes,
                        onValueChange = { codeMinutes = it.filter { c -> c.isDigit() } },
                        label = { Text("Kaç dakika") },
                        modifier = Modifier.width(150.dp).padding(start = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        val count = codeCount.toIntOrNull() ?: 0
                        val minutes = codeMinutes.toIntOrNull() ?: 0
                        if (count > 0 && minutes > 0) {
                            prefs.generateSCodes(count, minutes)
                            generatedCodes = prefs.getSCodes()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Kod Üret")
                }

                val unused = generatedCodes.filter { !it.used }
                if (unused.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Kullanılmamış kodlar:")
                            unused.forEach { c ->
                                Text(text = "${c.code}  (+${c.minutes} dk)")
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        prefs.deleteAllUsedSCodes()
                        generatedCodes = prefs.getSCodes()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Kullanılmış Kodları Temizle")
                }
            }

            item {
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Text("Geri")
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(text: String, buttonText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, modifier = Modifier.padding(top = 10.dp))
        Button(onClick = onClick) { Text(buttonText) }
    }
}

private fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean {
    val expectedComponent = "${context.packageName}/${context.packageName}.service.AppBlockAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
}

private fun isDeviceAdminActive(context: android.content.Context): Boolean {
    val dpm = context.getSystemService(android.content.Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val compName = ComponentName(context, AppDeviceAdminReceiver::class.java)
    return dpm.isAdminActive(compName)
}
