package com.webkurier.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.webkurier.android.pilot.PilotApp
import com.webkurier.android.pilot.PilotDependencies
import com.webkurier.android.ui.WebKurierTheme

/** Student pilot entry point. Legacy screens remain available in the ui package. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebKurierTheme {
                val dependencies = remember { PilotDependencies(applicationContext) }
                PilotApp(dependencies.controller, dependencies.website)
            }
        }
    }
}
