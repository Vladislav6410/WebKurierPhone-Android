package com.webkurier.android.coreSecure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.webkurier.android.coreNet.TokenProvider

/**
 * SecureStore
 *
 * Encrypted on-device storage for session artifacts.
 * Uses Android Keystore via MasterKey + EncryptedSharedPreferences.
 *
 * RULES:
 * - Never store secrets in plain SharedPreferences
 * - Never log token values
 */
class SecureStore(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String {
        return prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
    }

    fun clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun setRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String {
        return prefs.getString(KEY_REFRESH_TOKEN, "") ?: ""
    }

    fun clearRefreshToken() {
        prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
    }

    fun setUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, "") ?: ""
    }

    fun clearUserId() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "webkurier_secure_store"

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
    }
}

/**
 * SecureTokenProvider
 *
 * Default TokenProvider that reads from SecureStore.
 */
class SecureTokenProvider(
    private val secureStore: SecureStore
) : TokenProvider {
    override fun getToken(): String = secureStore.getAccessToken()
}