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
 * RomanticScreen
 *
 * Thin UI wrapper for RomanticAgent.
 * Core is authoritative and routes the request to the correct agent.
 */
@Composable
fun RomanticScreen(deps: AppDependencies) {
    val message = remember { mutableStateOf("") }
    val status = remember { mutableStateOf("Idle") }
    val output = remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Romantic Agent")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = message.value,
            onValueChange = { message.value = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                status.value = "Sending..."
                output.value = ""

                val payload = JSONObject().apply {
                    put("agent", "romantic")
                    put("message", message.value)
                    put("lang", deps.i18n.getCurrentLanguage())
                    put("client", "android")
                }.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    // Placeholder endpoint: Core routes to RomanticAgent
                    val result = deps.coreGateway.post("/agents/chat", payload)
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
            Text("Send (raw)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Status: ${status.value}")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = output.value,
            onValueChange = { },
            label = { Text("Result (raw JSON/text from Core)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 10,
            readOnly = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Note: /agents/chat is a placeholder until Core agent contract is fixed.")
    }
}