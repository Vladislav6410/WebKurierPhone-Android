package com.webkurier.android.pilot

import com.webkurier.android.R

internal data class SimpleLesson(
    val titleRes: Int,
    val assetPath: String,
    val pdfUrl: String,
    val audioUrl: String? = null,
    val videoUrl: String? = null
)

/** One numbered slot (1–64) of the Student Pilot catalog. */
internal sealed interface CatalogLesson {
    val number: Int
    val week: Int get() = PilotLessonCatalog.weekOf(number)

    /** Existing, already published material. */
    data class Ready(override val number: Int, val lesson: SimpleLesson) : CatalogLesson

    /** No approved material yet: shown as "Урок N — в подготовке", cannot be opened. */
    data class InPreparation(override val number: Int) : CatalogLesson
}

/**
 * 8 weeks × 8 numbered lessons = 64. The introductory session is a separate
 * section outside the 1–64 numbering. Ready materials are kept unchanged.
 */
internal object PilotLessonCatalog {
    const val WEEK_COUNT = 8
    const val LESSONS_PER_WEEK = 8
    const val LESSON_COUNT = WEEK_COUNT * LESSONS_PER_WEEK

    fun weekOf(number: Int): Int {
        require(number in 1..LESSON_COUNT)
        return (number - 1) / LESSONS_PER_WEEK + 1
    }

    fun lessonsOfWeek(week: Int): IntRange {
        require(week in 1..WEEK_COUNT)
        val first = (week - 1) * LESSONS_PER_WEEK + 1
        return first until first + LESSONS_PER_WEEK
    }

    val intro = SimpleLesson(
        R.string.pilot_intro_lesson,
        "education/week01/intro.txt",
        "https://drive.google.com/file/d/1ulIXKzbicd67C6tE4JCmvPm3YD_pHmmA/view?usp=drivesdk"
    )

    private val readyLessons: Map<Int, SimpleLesson> = mapOf(
        1 to SimpleLesson(
            R.string.pilot_lesson_one_simple,
            "education/week01/lesson01.txt",
            "https://drive.google.com/file/d/1avmoQQ7U6H0rSzY5nwmww0qeT_4xlNQl/view?usp=drivesdk"
        ),
        2 to SimpleLesson(
            R.string.pilot_lesson_two_simple,
            "",
            "https://drive.google.com/file/d/1wW8gkm0pdsAsu2YkRaf4OciDX-UIT5sA/view?usp=drivesdk"
        ),
        3 to SimpleLesson(
            R.string.pilot_lesson_three_simple,
            "",
            "https://drive.google.com/file/d/1XNeUyApAymFx3zsH-WKScdwpTbeVM5Na/view?usp=drivesdk"
        ),
        4 to SimpleLesson(
            R.string.pilot_lesson_four_simple,
            "",
            "https://drive.google.com/file/d/1Pp1KeMZwwFWei2TJyp_6YIRmVI_VLsWz/view?usp=drivesdk"
        ),
        5 to SimpleLesson(
            R.string.pilot_lesson_five_simple,
            "",
            "https://drive.google.com/file/d/16Z-zfr4m27yFFGgLTu3IZOltGwfcqRoD/view?usp=drivesdk"
        ),
        6 to SimpleLesson(
            R.string.pilot_lesson_six_simple,
            "",
            "https://drive.google.com/file/d/1m_njgjTjSsuwFJvVrv5Yj-wJ2SsEmNjn/view?usp=drivesdk"
        ),
        7 to SimpleLesson(
            R.string.pilot_lesson_seven_simple,
            "",
            "https://drive.google.com/file/d/1oJfA4cGtBwEr2HfzSfzkp5sEMWS3WBDZ/view?usp=drivesdk"
        )
    )

    val lessons: List<CatalogLesson> = (1..LESSON_COUNT).map { number ->
        readyLessons[number]?.let { CatalogLesson.Ready(number, it) } ?: CatalogLesson.InPreparation(number)
    }

    init {
        require(readyLessons.keys.all { it in 1..LESSON_COUNT })
    }
}
