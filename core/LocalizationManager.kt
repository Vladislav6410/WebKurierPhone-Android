package com.webkurier.android.core

import android.content.Context
import java.util.Locale

/**
 * LocalizationManager
 *
 * Stores and provides the current UI language for the Android client.
 *
 * RULES:
 * - Android stores only UI preference (non-authoritative)
 * - Server (Core/PhoneCore) remains authoritative for content logic
 * - Do not hardcode languages in UI screens: use this manager
 */
class LocalizationManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSupportedLanguages(): List<String> = SUPPORTED_LANGS

    fun getDefaultLanguage(): String = DEFAULT_LANG

    fun getCurrentLanguage(): String {
        val stored = prefs.getString(KEY_LANG, null)
        return stored ?: DEFAULT_LANG
    }

    fun setCurrentLanguage(lang: String) {
        if (!SUPPORTED_LANGS.contains(lang)) return
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun getCurrentLocale(): Locale {
        return Locale.forLanguageTag(getCurrentLanguage())
    }

    fun resetToDefault() {
        prefs.edit().remove(KEY_LANG).apply()
    }

    companion object {
        private const val PREFS_NAME = "webkurier_i18n"
        private const val KEY_LANG = "lang"

        private const val DEFAULT_LANG = "de"
        private val SUPPORTED_LANGS = listOf("de", "en", "pl", "ru", "uk")
    }
}