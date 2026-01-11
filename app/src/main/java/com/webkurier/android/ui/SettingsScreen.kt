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
 * SettingsScreen
 *
 * Minimal settings:
 * - UI language selection (local preference)
 * - Clear session (secure store)
 *
 * NOTE:
 * Server remains authoritative for account/session.
 * Clearing local tokens is a client-side logout helper.
 */
@Composable
fun SettingsScreen(deps: AppDependencies) {
    val currentLang = remember { mutableStateOf(deps.i18n.getCurrentLanguage()) }
    val status = remember { mutableStateOf("Idle") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Settings")

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Language (UI preference)")
        OutlinedTextField(
            value = currentLang.value,
            onValueChange = { currentLang.value = it },
            label = { Text("de / en / pl / ru / uk") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                deps.i18n.setCurrentLanguage(currentLang.value)
                status.value = "Language saved: ${deps.i18n.getCurrentLanguage()}"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save language")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Session")
        Button(
            onClick = {
                deps.secureStore.clearAll()
                status.value = "Local session cleared"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear local tokens")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Status: ${status.value}")

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Environment: ${deps.endpoints.environment}")
        Text(text = "Core: ${deps.endpoints.core.restBaseUrl}")
        Text(text = "PhoneCore: ${deps.endpoints.phoneCore.restBaseUrl}")
    }
}