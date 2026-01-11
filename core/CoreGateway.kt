package com.webkurier.android.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * CoreGateway
 *
 * Single, authoritative entry point for all communication
 * between Android client and WebKurierCore.
 *
 * RULES:
 * - Android is a thin client
 * - Core is authoritative
 * - No business logic here
 * - No secrets stored here
 */
class CoreGateway(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider
) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Generic GET request to WebKurierCore
     */
    suspend fun get(path: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = buildRequest(path)
                httpClient.newCall(request).execute().use { response ->
                    handleResponse(response)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Generic POST request (JSON payload)
     */
    suspend fun post(path: String, jsonBody: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val body = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("application/json"),
                    jsonBody
                )

                val request = buildRequest(path)
                    .newBuilder()
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    handleResponse(response)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Builds a request with mandatory headers
     */
    private fun buildRequest(path: String): Request {
        val token = tokenProvider.getToken()

        return Request.Builder()
            .url("$baseUrl$path")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .addHeader("X-Client", "android")
            .build()
    }

    /**
     * Centralized response handling
     */
    private fun handleResponse(response: Response): Result<String> {
        return if (response.isSuccessful) {
            Result.success(response.body()?.string().orEmpty())
        } else {
            Result.failure(
                RuntimeException(
                    "Core error ${response.code()} : ${response.message()}"
                )
            )
        }
    }
}

/**
 * TokenProvider
 *
 * Abstracts secure token access.
 * Implementation must use SecureStore.
 */
interface TokenProvider {
    fun getToken(): String
}