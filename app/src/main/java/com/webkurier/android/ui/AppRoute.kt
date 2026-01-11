package com.webkurier.android.ui

/**
 * AppRoute
 *
 * Minimal navigation model for the app.
 * Keeps navigation stable and testable without external nav dependencies.
 */
enum class AppRoute {
    Home,
    Translator,
    VoiceCall,
    Lessons,
    Wallet,
    DreamMaker,
    Romantic,
    HR,
    Cafe,
    Settings
}

/**
 * AppNavState
 *
 * Simple state holder used by MainActivity.
 */
class AppNavState(initial: AppRoute) {
    var current: AppRoute = initial
        private set

    fun navigate(route: AppRoute) {
        current = route
    }
}

fun rememberAppNavState(initial: AppRoute): AppNavState {
    // Compose-friendly lightweight state holder.
    // If you later want state restoration, we can switch to rememberSaveable.
    return AppNavState(initial)
}