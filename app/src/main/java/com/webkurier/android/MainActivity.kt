package com.webkurier.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.webkurier.android.core.AppDependencies
import com.webkurier.android.ui.AppRoute
import com.webkurier.android.ui.AppScaffold
import com.webkurier.android.ui.CafeScreen
import com.webkurier.android.ui.DreamMakerScreen
import com.webkurier.android.ui.HRScreen
import com.webkurier.android.ui.HomeScreen
import com.webkurier.android.ui.LessonsScreen
import com.webkurier.android.ui.RomanticScreen
import com.webkurier.android.ui.SettingsScreen
import com.webkurier.android.ui.TranslatorScreen
import com.webkurier.android.ui.VoiceCallScreen
import com.webkurier.android.ui.WalletScreen
import com.webkurier.android.ui.WebKurierTheme
import com.webkurier.android.ui.rememberAppNavState

/**
 * MainActivity
 *
 * Entry point of WebKurierPhone-Android.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WebKurierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val deps = remember { AppDependencies(this) }
                    val navState = rememberAppNavState(initial = AppRoute.Home)

                    AppScaffold(
                        currentRoute = navState.current,
                        onNavigate = { navState.navigate(it) }
                    ) {
                        when (navState.current) {
                            AppRoute.Home ->
                                HomeScreen(onNavigate = { navState.navigate(it) })

                            AppRoute.Translator ->
                                TranslatorScreen(deps = deps)

                            AppRoute.VoiceCall ->
                                VoiceCallScreen(deps = deps)

                            AppRoute.Lessons ->
                                LessonsScreen(deps = deps)

                            AppRoute.Wallet ->
                                WalletScreen(deps = deps)

                            AppRoute.DreamMaker ->
                                DreamMakerScreen(deps = deps)

                            AppRoute.Romantic ->
                                RomanticScreen(deps = deps)

                            AppRoute.HR ->
                                HRScreen(deps = deps)

                            AppRoute.Cafe ->
                                CafeScreen(deps = deps)

                            AppRoute.Settings ->
                                SettingsScreen(deps = deps)
                        }
                    }
                }
            }
        }
    }
}

