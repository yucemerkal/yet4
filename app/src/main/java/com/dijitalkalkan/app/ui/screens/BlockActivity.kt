package com.dijitalkalkan.app.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dijitalkalkan.app.data.PrefsManager
import com.dijitalkalkan.app.ui.theme.DijitalKalkanTheme

/**
 * Bir uygulamanın günlük süre limiti dolduğunda tam ekran gösterilen ekran.
 * Erişilebilirlik Servisi tarafından yeni bir "task" olarak başlatılır, böylece
 * kısıtlanan uygulamanın önüne geçer.
 */
class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        val appLabel = try {
            val pm = packageManager
            val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }

        setContent {
            DijitalKalkanTheme {
                BlockScreen(
                    appLabel = appLabel,
                    onRedeemed = {
                        finish() // Süre başarıyla artırıldı, kaldığı yere dön
                    },
                    onGoHome = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                        startActivity(homeIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Geri tuşuyla kısıtlanan uygulamaya dönülmesini engelle, ana ekrana yönlendir
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        startActivity(homeIntent)
        finish()
    }
}

@androidx.compose.runtime.Composable
private fun BlockScreen(
    appLabel: String,
    onRedeemed: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { PrefsManager(context) }
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.padding(bottom = 16.dp))
            Text(text = "Günlük süre limitine ulaşıldı", fontSize = 22.sp)
            Text(
                text = "$appLabel için bugünkü kullanım süren doldu.",
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            Text(text = "Ebeveyninden bir C-kod aldıysan buraya gir:")
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                singleLine = true,
                label = { Text("C-kod") }
            )
            Button(
                onClick = {
                    if (prefs.redeemSCode(code)) {
                        message = "Ek süre eklendi!"
                        onRedeemed()
                    } else {
                        message = "Kod geçersiz veya daha önce kullanılmış."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kodu Kullan")
            }

            message?.let {
                Text(text = it, modifier = Modifier.padding(top = 12.dp))
            }

            Button(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text("Ana Ekrana Dön")
            }
        }
    }
}
