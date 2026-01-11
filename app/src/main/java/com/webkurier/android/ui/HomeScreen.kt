package com.webkurier.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * HomeScreen
 *
 * Main navigation hub.
 * Thin UI: routes user to features implemented via Core/PhoneCore.
 */
@Composable
fun HomeScreen(onNavigate: (AppRoute) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "WebKurierPhone — Android Client")

        Spacer(modifier = Modifier.height(16.dp))

        HomeButton("🌍 Translator") { onNavigate(AppRoute.Translator) }
        HomeButton("📞 Voice Call") { onNavigate(AppRoute.VoiceCall) }
        HomeButton("🎓 Lessons A1–C1") { onNavigate(AppRoute.Lessons) }
        HomeButton("💰 Wallet (WebCoin)") { onNavigate(AppRoute.Wallet) }
        HomeButton("🎨 DreamMaker") { onNavigate(AppRoute.DreamMaker) }
        HomeButton("💬 Romantic Agent") { onNavigate(AppRoute.Romantic) }
        HomeButton("🧑‍💼 HR Agent") { onNavigate(AppRoute.HR) }
        HomeButton("☕ Cafe") { onNavigate(AppRoute.Cafe) }

        Spacer(modifier = Modifier.height(16.dp))

        HomeButton("⚙️ Settings") { onNavigate(AppRoute.Settings) }
    }
}

@Composable
private fun HomeButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(text = label)
    }
}