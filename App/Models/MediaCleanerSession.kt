package com.webkurier.phone.models

data class MediaCleanerSession(
    val sessionId: String,
    val createdAt: Long,
    val userId: String? = null,
    val deviceId: String? = null
)