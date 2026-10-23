package com.webkurier.android.pilot

import com.webkurier.android.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PilotLessonCatalogTest {
    @Test fun catalogHasSixtyFourUniqueNumberedLessonsInEightWeeks() {
        val lessons = PilotLessonCatalog.lessons
        assertEquals(64, lessons.size)
        assertEquals((1..64).toList(), lessons.map { it.number })
        assertEquals(PilotCourse.weeks.size, PilotLessonCatalog.WEEK_COUNT)
        (1..8).forEach { week ->
            assertEquals(8, lessons.count { it.week == week })
            assertEquals(((week - 1) * 8 + 1)..(week * 8), PilotLessonCatalog.lessonsOfWeek(week))
        }
        assertEquals(1, PilotLessonCatalog.weekOf(1))
        assertEquals(1, PilotLessonCatalog.weekOf(8))
        assertEquals(2, PilotLessonCatalog.weekOf(9))
        assertEquals(8, PilotLessonCatalog.weekOf(64))
    }

    @Test fun existingLessonsOneToSevenKeepTheirMaterials() {
        val ready = PilotLessonCatalog.lessons.filterIsInstance<CatalogLesson.Ready>()
        assertEquals((1..7).toList(), ready.map { it.number })
        assertEquals(
            listOf(
                R.string.pilot_lesson_one_simple, R.string.pilot_lesson_two_simple, R.string.pilot_lesson_three_simple,
                R.string.pilot_lesson_four_simple, R.string.pilot_lesson_five_simple, R.string.pilot_lesson_six_simple,
                R.string.pilot_lesson_seven_simple
            ),
            ready.map { it.lesson.titleRes }
        )
        assertEquals("education/week01/lesson01.txt", ready.first().lesson.assetPath)
        assertTrue(ready.all { it.lesson.pdfUrl.startsWith("https://drive.google.com/file/d/") })
    }

    @Test fun missingLessonsAreInPreparationAndIntroIsOutsideNumbering() {
        val inPreparation = PilotLessonCatalog.lessons.filterIsInstance<CatalogLesson.InPreparation>()
        assertEquals((8..64).toList(), inPreparation.map { it.number })
        assertEquals(R.string.pilot_intro_lesson, PilotLessonCatalog.intro.titleRes)
        assertTrue(PilotLessonCatalog.lessons.none { it is CatalogLesson.Ready && it.lesson == PilotLessonCatalog.intro })
    }

    @Test(expected = IllegalArgumentException::class)
    fun lessonNumberOutsideCatalogIsRejected() { PilotLessonCatalog.weekOf(65) }

    @Test(expected = IllegalArgumentException::class)
    fun lessonNumberZeroIsRejected() { PilotLessonCatalog.weekOf(0) }
}
