package com.webkurier.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

/**
 * WebKurierTheme
 *
 * Minimal Material3 theme wrapper.
 * Later we can extend with dynamic colors (Material You) and dark/light palettes.
 */
@Composable
fun WebKurierTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = Typography(),
        content = content
    )
}