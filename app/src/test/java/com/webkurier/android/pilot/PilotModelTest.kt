package com.webkurier.android.pilot

import org.junit.Assert.*
import org.junit.Test

class PilotModelTest {
    @Test fun weekOneHasExactlyThreeAccessibleDays() {
        assertEquals(listOf(1, 2, 3), PilotCourse.days)
        assertEquals(PilotWeek(1, CourseState.AVAILABLE), PilotCourse.weeks.first())
        val progress = CourseProgress()
        assertEquals(CourseState.CURRENT, progress.state(1))
        assertEquals(CourseState.AVAILABLE, progress.state(2))
        assertEquals(CourseState.AVAILABLE, progress.state(3))
    }

    @Test fun laterWeeksStayLockedAndHaveNoLessons() {
        assertEquals((2..8).toList(), PilotCourse.weeks.drop(1).map { it.number })
        assertTrue(PilotCourse.weeks.drop(1).all { it.state == CourseState.LOCKED })
        assertEquals(CourseState.LOCKED, CourseProgress().state(4))
        assertEquals(CourseProgress(), CourseProgress().select(4))
    }

    @Test fun selectingAndCompletingDayDoesNotCompleteOtherDaysOrUnlockWeeks() {
        val progress = CourseProgress().select(2).completeCurrent()
        assertEquals(2, progress.currentDay)
        assertEquals(setOf(2), progress.completedDays)
        assertEquals(CourseState.COMPLETED, progress.state(2))
        assertEquals(CourseState.AVAILABLE, progress.state(1))
        assertEquals(CourseState.CURRENT, progress.select(3).state(3))
        assertEquals(progress, progress.completeCurrent())
        assertTrue(PilotCourse.weeks.drop(1).all { it.state == CourseState.LOCKED })
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCurrentDayIsRejected() { CourseProgress(currentDay = 4) }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCompletedDayIsRejected() { CourseProgress(completedDays = setOf(8)) }

    @Test fun missingWebsiteIsExplicitlyUnconfigured() {
        listOf(null, "", "  ").forEach { assertEquals(WebsiteResult.NotConfigured, validateWebsiteUrl(it)) }
    }

    @Test fun publicHttpsWebsiteCanBeOpened() {
        val result = validateWebsiteUrl(" https://student.example.org/lesson/index.html ")
        assertEquals("https://student.example.org/lesson/index.html", (result as WebsiteResult.Ready).url)
        assertTrue(validateWebsiteUrl("https://student.example.org:443/") is WebsiteResult.Ready)
    }

    @Test fun unsafeOrMalformedWebsiteIsRejected() {
        listOf(
            "http://student.example.org", "javascript:alert(1)", "file:///tmp/index.html",
            "intent://student.example.org", "//student.example.org", "student.example.org",
            "https://", "https://user:password@student.example.org", "https://student.example.org:8080",
            "https://student.example.org/?token=example", "https://student.example.org/#example",
            "https://localhost/", "https://site.localhost/", "https://127.0.0.1/",
            "https://[::1]/", "https://[::ffff:127.0.0.1]/",
            "https://student.example.org/a b", "https://student.example.org\\@other.example.org"
        ).forEach { assertEquals(it, WebsiteResult.Invalid, validateWebsiteUrl(it)) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun connectedStateRequiresIdentity() { GitHubConnection(ConnectionStatus.CONNECTED) }

    @Test(expected = IllegalArgumentException::class)
    fun disconnectedStateCannotExposeStaleIdentity() {
        GitHubConnection(identity = GitHubIdentity("student"))
    }
}
