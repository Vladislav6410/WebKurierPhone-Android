package com.webkurier.phone.config

object MediaCleanerConfig {
    // Core API base (пример: https://core.webkurier.com)
    // В реальном проекте лучше прокинуть через BuildConfig / env, но пока держим просто.
    const val CORE_BASE_URL: String = "https://example.com"

    // Таймауты (мс)
    const val CONNECT_TIMEOUT_MS: Long = 10_000
    const val READ_TIMEOUT_MS: Long = 20_000
    const val WRITE_TIMEOUT_MS: Long = 20_000

    // MediaCleaner endpoints (в Core)
    const val PATH_CREATE_SESSION: String = "/api/v1/media-cleaner/session"
    const val PATH_SUBMIT_SUMMARY: String = "/api/v1/media-cleaner/summary"

    // Privacy-first: сканируем только метаданные
    const val METADATA_ONLY: Boolean = true
}