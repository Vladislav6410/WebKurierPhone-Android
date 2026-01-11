package com.webkurier.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * AppScaffold
 *
 * Shared UI shell: top bar + lightweight navigation buttons.
 * This is intentionally minimal and can later be replaced with
 * Navigation-Compose or a bottom navigation.
 */
@Composable
fun AppScaffold(
    currentRoute: AppRoute,
    onNavigate: (AppRoute) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "WebKurierPhone • ${currentRoute.name}")
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NavRow(onNavigate = onNavigate)
            content()
        }
    }
}

@Composable
private fun NavRow(onNavigate: (AppRoute) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        NavBtn("Home") { onNavigate(AppRoute.Home) }
        NavBtn("Translator") { onNavigate(AppRoute.Translator) }
        NavBtn("Call") { onNavigate(AppRoute.VoiceCall) }
        NavBtn("Lessons") { onNavigate(AppRoute.Lessons) }
        NavBtn("Wallet") { onNavigate(AppRoute.Wallet) }
    }
}

@Composable
private fun NavBtn(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
    }
}