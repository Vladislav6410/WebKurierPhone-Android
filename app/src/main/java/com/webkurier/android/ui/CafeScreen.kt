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

/**
 * CafeScreen
 *
 * Placeholder UI for CafeAgent / menu / ordering flows.
 * Real integration will be routed via Core.
 */
@Composable
fun CafeScreen(deps: AppDependencies) {
    val note = remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Cafe")

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "This is a placeholder screen. " +
                "Later: menu, order flow, payments, rewards, and Core routing."
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = note.value,
            onValueChange = { note.value = it },
            label = { Text("Notes (local)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* placeholder */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save note (placeholder)")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Current language: ${deps.i18n.getCurrentLanguage()}")
    }
}