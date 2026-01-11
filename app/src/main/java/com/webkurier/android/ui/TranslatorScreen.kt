package com.webkurier.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webkurier.android.core.AppDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * TranslatorScreen
 *
 * Minimal translation UI:
 * - input text
 * - target language
 * - call PhoneCore translate endpoint
 *
 * Server is authoritative for language detection and translation.
 */
@Composable
fun TranslatorScreen(deps: AppDependencies) {
    val input = remember { mutableStateOf("") }
    val targetLang = remember { mutableStateOf(deps.i18n.getCurrentLanguage()) }
    val output = remember { mutableStateOf("") }
    val status = remember { mutableStateOf("Idle") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Translator")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = targetLang.value,
            onValueChange = { targetLang.value = it },
            label = { Text("Target language (e.g. de/en/pl/ru/uk)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = input.value,
            onValueChange = { input.value = it },
            label = { Text("Text to translate") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                status.value = "Sending..."
                output.value = ""

                val payload = JSONObject().apply {
                    put("text", input.value)
                    put("to", targetLang.value)
                    put("from", "auto")
                }.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    val result = deps.phoneCoreApi.translateText(payload)
                    result.onSuccess { body ->
                        status.value = "OK"
                        output.value = body
                    }.onFailure { err ->
                        status.value = "Error: ${err.message ?: "unknown"}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Translate")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Status: ${status.value}")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = output.value,
            onValueChange = { },
            label = { Text("Result (raw JSON/text from PhoneCore)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 6,
            readOnly = true
        )
    }
}