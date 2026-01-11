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
 * WalletScreen
 *
 * Minimal wallet UI:
 * - balance (via Core proxy)
 * - history (via Core proxy)
 *
 * NOTE:
 * Chain is authoritative; Android only displays server responses.
 */
@Composable
fun WalletScreen(deps: AppDependencies) {
    val status = remember { mutableStateOf("Idle") }
    val output = remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Wallet (WebCoin)")

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                status.value = "Loading balance..."
                output.value = ""
                CoroutineScope(Dispatchers.Main).launch {
                    // Example path; server contract may change:
                    // Core -> Chain proxy endpoint
                    val result = deps.coreGateway.get("/wallet/balance")
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
            Text("Load Balance (raw)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                status.value = "Loading history..."
                output.value = ""
                CoroutineScope(Dispatchers.Main).launch {
                    // Example path; server contract may change:
                    val result = deps.coreGateway.get("/wallet/history")
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
            Text("Load History (raw)")
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
        Text(text = "Note: endpoints /wallet/* are placeholders until Core contract is fixed.")
    }
}