package com.webkurier.android.coreNet

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
 * Client gateway to WebKurierPhoneCore (STT/TTS/Translation/Lessons).
 *
 * RULES:
 * - No business logic
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

    suspend fun translateText(payloadJson: String): Result<String> =
        post("/translate/text", payloadJson)

    suspend fun speechToText(payloadJson: String): Result<String> =
        post("/stt", payloadJson)

    suspend fun textToSpeech(payloadJson: String): Result<String> =
        post("/tts", payloadJson)

    suspend fun getLessons(path: String): Result<String> =
        get("/lessons/$path")

    private suspend fun get(path: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = buildRequest(path)
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Result.success(response.body?.string().orEmpty())
                    } else {
                        Result.failure(RuntimeException("PhoneCore error ${response.code}: ${response.message}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun post(path: String, jsonBody: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val body = RequestBody.create(jsonMediaType, jsonBody)
                val request = buildRequest(path).newBuilder().post(body).build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Result.success(response.body?.string().orEmpty())
                    } else {
                        Result.failure(RuntimeException("PhoneCore error ${response.code}: ${response.message}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildRequest(path: String): Request {
        val token = tokenProvider.getToken()
        return Request.Builder()
            .url("$baseUrl$path")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/json")
            .addHeader("X-Client", "android")
            .build()
    }
}