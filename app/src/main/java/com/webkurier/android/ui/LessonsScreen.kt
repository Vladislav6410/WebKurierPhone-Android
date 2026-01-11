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

/**
 * LessonsScreen
 *
 * Minimal lessons UI:
 * - choose language + level
 * - query PhoneCore lessons endpoint
 * - display raw response
 *
 * Server remains authoritative for course structure.
 */
@Composable
fun LessonsScreen(deps: AppDependencies) {
    val lang = remember { mutableStateOf(deps.i18n.getCurrentLanguage()) }
    val level = remember { mutableStateOf("A1") }

    val status = remember { mutableStateOf("Idle") }
    val output = remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Lessons (A1–C1)")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lang.value,
            onValueChange = { lang.value = it },
            label = { Text("Language (e.g. de/en/pl/ru/uk)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = level.value,
            onValueChange = { level.value = it },
            label = { Text("Level (A1/A2/B1/B2/C1)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                status.value = "Loading..."
                output.value = ""

                // Example path: /lessons/{lang}/{level}
                val path = "${lang.value}/${level.value}"

                CoroutineScope(Dispatchers.Main).launch {
                    val result = deps.phoneCoreApi.getLessons(path)
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
            Text("Load lessons (raw)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Status: ${status.value}")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = output.value,
            onValueChange = { },
            label = { Text("Result (raw JSON/text from PhoneCore)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 10,
            readOnly = true
        )
    }
}