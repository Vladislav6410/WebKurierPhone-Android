package com.webkurier.android.pilot

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PilotAndroidTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun preferencesRestoreAfterRecreationAndRecoverFromWrongTypes() {
        val preferences = context.getSharedPreferences("pilot_course_progress", Context.MODE_PRIVATE)
        preferences.edit().clear().putString("current_day", "broken")
            .putInt("completed_days", 42).commit()
        val store = LocalCourseProgressStore(context)
        assertEquals(CourseProgress(), store.load())
        store.save(CourseProgress(2, setOf(1, 2)))
        assertEquals(CourseProgress(2, setOf(1, 2)), LocalCourseProgressStore(context).load())
        preferences.edit().clear().commit()
    }

    @Test fun browserReceivesOnlyConfiguredHttpsResult() {
        var intent: Intent? = null
        val browser = object : ContextWrapper(context) {
            override fun startActivity(value: Intent) { intent = value }
        }
        assertFalse(openWebsite(browser, WebsiteResult.NotConfigured))
        assertFalse(openWebsite(browser, WebsiteResult.Invalid))
        assertNull(intent)
        assertTrue(openWebsite(browser, validateWebsiteUrl("https://student.example.org/")))
        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertTrue(intent?.hasCategory(Intent.CATEGORY_BROWSABLE) == true)
        assertEquals("https://student.example.org/", intent?.dataString)
    }

    @Test fun unavailableOrBlockedBrowserReturnsFailureInsteadOfCrashing() {
        val missingBrowser = object : ContextWrapper(context) {
            override fun startActivity(value: Intent) { throw ActivityNotFoundException() }
        }
        val blockedBrowser = object : ContextWrapper(context) {
            override fun startActivity(value: Intent) { throw SecurityException() }
        }
        val website = validateWebsiteUrl("https://student.example.org/")
        assertFalse(openWebsite(missingBrowser, website))
        assertFalse(openWebsite(blockedBrowser, website))
    }
}
