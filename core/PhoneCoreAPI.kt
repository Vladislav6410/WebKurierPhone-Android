package com.webkurier.android.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

/**
 * PhoneCoreAPI
 *
 * Client-side gateway to WebKurierPhoneCore.
 *
 * Responsibilities:
 * - STT / TTS requests
 * - Translation requests
 * - Lessons data retrieval
 *
 * RULES:
 * - No business logic
 * - No local interpretation of results
 * - Server responses are authoritative
 */
class PhoneCoreAPI(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider
) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    /**
     * Text translation request
     */
    suspend fun translateText(payloadJson: String): Result<String> {
        return post("/translate/text", payloadJson)
    }

    /**
     * Speech-to-Text request (non-realtime)
     * Audio must be pre-encoded by AudioEngine
     */
    suspend fun speechToText(payloadJson: String): Result<String> {
        return post("/stt", payloadJson)
    }

    /**
     * Text-to-Speech request
     */
    suspend fun textToSpeech(payloadJson: String): Result<String> {
        return post("/tts", payloadJson)
    }

    /**
     * Lessons / courses request
     */
    suspend fun getLessons(path: String): Result<String> {
        return get("/lessons/$path")
    }

    /**
     * Generic GET
     */
    private suspend fun get(path: String): Result<String> {
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
     * Generic POST (JSON)
     */
    private suspend fun post(path: String, jsonBody: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val body = RequestBody.create(jsonMediaType, jsonBody)

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
     * Builds request with mandatory headers
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
     * Unified response handling
     */
    private fun handleResponse(response: okhttp3.Response): Result<String> {
        return if (response.isSuccessful) {
            Result.success(response.body?.string().orEmpty())
        } else {
            Result.failure(
                RuntimeException(
                    "PhoneCore error ${response.code}: ${response.message}"
                )
            )
        }
    }
}