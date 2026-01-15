package com.webkurier.phone.models

data class MediaCleanerResultsSummary(
    val totalItems: Int,
    val photosCount: Int,
    val videosCount: Int,

    val screenshotsCount: Int,
    val duplicatesGroups: Int,
    val duplicatesItems: Int,

    val slimEligibleCount: Int,
    val slimEstimatedBytesSaved: Long,

    val scannedAt: Long
)