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
 * DreamMakerScreen
 *
 * Minimal UI for media generation requests.
 * This is a client shell; actual generation is handled by Core/PhoneCore.
 */
@Composable
fun DreamMakerScreen(deps: AppDependencies) {
    val prompt = remember { mutableStateOf("") }
    val mode = remember { mutableStateOf("image") } // image/audio/video (future)
    val status = remember { mutableStateOf("Idle") }
    val output = remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "DreamMaker")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = mode.value,
            onValueChange = { mode.value = it },
            label = { Text("Mode (image/audio/video)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = prompt.value,
            onValueChange = { prompt.value = it },
            label = { Text("Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                status.value = "Sending..."
                output.value = ""

                val payload = JSONObject().apply {
                    put("mode", mode.value)
                    put("prompt", prompt.value)
                    put("client", "android")
                }.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    // Placeholder endpoint: Core routes to DreamAgent / media backend
                    val result = deps.coreGateway.post("/dreammaker/generate", payload)
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
            Text("Generate (raw)")
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
        Text(text = "Note: /dreammaker/generate is a placeholder until Core contract is fixed.")
    }
}