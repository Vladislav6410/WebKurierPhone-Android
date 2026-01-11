package com.webkurier.android.coreNet

/**
 * TokenProvider
 *
 * Abstraction for retrieving access token (encrypted storage).
 * Used by CoreGateway and PhoneCoreAPI.
 */
interface TokenProvider {
    fun getToken(): String
}