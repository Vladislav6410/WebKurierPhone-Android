package com.webkurier.android.pilot

import android.content.Context
import com.webkurier.android.R

/** Explicit composition root, following the existing AppDependencies pattern. */
class PilotDependencies(context: Context) {
    private val appContext = context.applicationContext
    val website: WebsiteResult = validateWebsiteUrl(appContext.getString(R.string.pilot_website_url))
    val controller = PilotController(
        LocalCourseProgressStore(appContext), UnconfiguredGitHubAuth, UnconfiguredCopilot
    )
}

private class LocalCourseProgressStore(context: Context) : CourseProgressStore {
    private val preferences = context.getSharedPreferences("pilot_course_progress", Context.MODE_PRIVATE)

    override fun load(): CourseProgress = CourseProgress(
        currentDay = preferences.getInt("current_day", 1).takeIf { it in PilotCourse.days } ?: 1,
        completedDays = preferences.getStringSet("completed_days", emptySet()).orEmpty()
            .mapNotNull { it.toIntOrNull() }.filter { it in PilotCourse.days }.toSet()
    )

    override fun save(progress: CourseProgress) {
        preferences.edit().putInt("current_day", progress.currentDay)
            .putStringSet("completed_days", progress.completedDays.map { it.toString() }.toSet()).apply()
    }
}
