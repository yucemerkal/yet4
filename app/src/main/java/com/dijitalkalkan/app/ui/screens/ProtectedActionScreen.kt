package com.dijitalkalkan.app.ui.screens

import android.content.Intent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dijitalkalkan.app.data.PrefsManager

/**
 * Çocuk, Ayarlar'da bu uygulamayı kaldırma / Cihaz Yöneticisi'ni kapatma /
 * Erişilebilirlik Servisi'ni kapatma ekranına her girdiğinde Erişilebilirlik
 * Servisi tarafından bu ekran üzerine açılır. Doğru ebeveyn şifresi girilmezse
 * doğrudan ana ekrana yönlendirilir.
 *
 * GERÇEK SINIRLAMA: Bu tam bir kilit değil, bir caydırıcıdır — çocuk bu ekranı
 * atlatıp (örn. hızlıca "Kaldır"a basarak) yine de işlemi tamamlayabilir.
 * Android, normal (Device Owner olmayan) bir uygulamanın kendi kaldırılmasını
 * %100 engellemesine izin vermez.
 */
class ProtectedActionScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.dijitalkalkan.app.ui.theme.DijitalKalkanTheme {
                ProtectedActionContent(
                    onCorrect = { finish() },
                    onCancelOrWrong = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                        startActivity(homeIntent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        startActivity(homeIntent)
        finish()
    }
}

@Composable
private fun ProtectedActionContent(onCorrect: () -> Unit, onCancelOrWrong: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null)
            Text(text = "Bu işlem için ebeveyn şifresi gerekiyor", fontSize = 20.sp, modifier = Modifier.padding(top = 12.dp, bottom = 24.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                label = { Text("Ebeveyn Şifresi") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(text = it, modifier = Modifier.padding(top = 8.dp)) }

            Button(
                onClick = {
                    if (prefs.verifyPCode(code)) onCorrect() else error = "Şifre yanlış."
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) { Text("Onayla") }

            Button(onClick = onCancelOrWrong, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Vazgeç")
            }
        }
    }
}
