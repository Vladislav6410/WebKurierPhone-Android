package com.webkurier.android.pilot

import java.net.URI

enum class CourseState { AVAILABLE, CURRENT, COMPLETED, LOCKED }
enum class PilotRoute { COURSE, COPILOT, PROJECT }

data class PilotWeek(val number: Int, val state: CourseState)

object PilotCourse {
    val days = (1..3).toList()
    val weeks = (1..8).map { PilotWeek(it, if (it == 1) CourseState.AVAILABLE else CourseState.LOCKED) }
}

/** Self-reported device progress, never evidence of a server/project change. */
data class CourseProgress(val currentDay: Int = 1, val completedDays: Set<Int> = emptySet()) {
    init {
        require(currentDay in PilotCourse.days)
        require(completedDays.all { it in PilotCourse.days })
    }

    fun state(day: Int): CourseState = when {
        day !in PilotCourse.days -> CourseState.LOCKED
        day in completedDays -> CourseState.COMPLETED
        day == currentDay -> CourseState.CURRENT
        else -> CourseState.AVAILABLE
    }

    fun select(day: Int): CourseProgress = if (day in PilotCourse.days) copy(currentDay = day) else this
    fun completeCurrent(): CourseProgress = copy(completedDays = completedDays + currentDay)
}

/** Tolerate missing, old or incorrectly typed device preferences without trusting them. */
fun restoreCourseProgress(currentDay: Any?, completedDays: Any?): CourseProgress = CourseProgress(
    currentDay = (currentDay as? Int)?.takeIf { it in PilotCourse.days } ?: 1,
    completedDays = (completedDays as? Set<*>).orEmpty()
        .mapNotNull { (it as? String)?.toIntOrNull() }
        .filter { it in PilotCourse.days }.toSet()
)

data class StudentProject(val repository: String, val description: String? = null)
data class GitHubIdentity(val login: String, val project: StudentProject? = null)

enum class ConnectionStatus { NOT_CONNECTED, CONNECTING, CONNECTED, ERROR }
enum class ConnectionError { NOT_CONFIGURED, FAILED }

data class GitHubConnection(
    val status: ConnectionStatus = ConnectionStatus.NOT_CONNECTED,
    val identity: GitHubIdentity? = null,
    val error: ConnectionError? = null
) {
    init {
        require((status == ConnectionStatus.CONNECTED) == (identity != null))
        require(identity == null || identity.login.isNotBlank())
        require((status == ConnectionStatus.ERROR) == (error != null))
    }
}

sealed interface WebsiteResult {
    data object NotConfigured : WebsiteResult
    data object Invalid : WebsiteResult
    data class Ready internal constructor(val url: String) : WebsiteResult
}

/** Only operator-supplied public HTTPS URLs; no credentials, query secrets or app schemes. */
fun validateWebsiteUrl(raw: String?): WebsiteResult {
    if (raw.isNullOrBlank()) return WebsiteResult.NotConfigured
    return try {
        val uri = URI(raw.trim())
        val host = uri.host?.trimEnd('.').orEmpty()
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() ||
            !host.contains('.') || host.contains(':') || uri.rawUserInfo != null || uri.rawQuery != null ||
            uri.rawFragment != null || uri.port !in listOf(-1, 443) ||
            host.endsWith(".localhost", ignoreCase = true) ||
            host.split('.').all { it.matches(Regex("(?i)(0x[0-9a-f]+|[0-9]+)")) }
        ) WebsiteResult.Invalid else WebsiteResult.Ready(uri.toASCIIString().replaceRange(0, uri.scheme.length, "https"))
    } catch (_: Exception) {
        WebsiteResult.Invalid
    }
}
