package com.dijitalkalkan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
 * Ebeveyn paneline girmeden önce gösterilen P-code (şifre) ekranı.
 * İlk kullanımda P-code belirlenir; sonraki girişlerde doğrulanır.
 */
@Composable
fun PCodeScreen(onUnlocked: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }
    val isFirstTime = !prefs.isPCodeSet()

    var code by remember { mutableStateOf("") }
    var confirmCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isFirstTime) "Ebeveyn P-code Belirle" else "Ebeveyn P-code Gir",
                fontSize = 22.sp
            )

            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                label = { Text("P-code (4-6 hane)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            )

            if (isFirstTime) {
                OutlinedTextField(
                    value = confirmCode,
                    onValueChange = { confirmCode = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("P-code (tekrar)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            error?.let {
                Text(text = it, modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = {
                    if (isFirstTime) {
                        when {
                            code.length < 4 -> error = "P-code en az 4 haneli olmalı."
                            code != confirmCode -> error = "Kodlar eşleşmiyor."
                            else -> {
                                prefs.setPCode(code)
                                onUnlocked()
                            }
                        }
                    } else {
                        if (prefs.verifyPCode(code)) {
                            onUnlocked()
                        } else {
                            error = "P-code yanlış."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text(if (isFirstTime) "P-code Belirle" else "Giriş Yap")
            }

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text("Geri")
            }
        }
    }
}
