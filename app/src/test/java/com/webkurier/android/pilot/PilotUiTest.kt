package com.webkurier.android.pilot

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.webkurier.android.MainActivity
import com.webkurier.android.ui.WebKurierTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w320dp-h640dp")
class PilotUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun resetProgress() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("pilot_course_progress", Context.MODE_PRIVATE).edit().clear().commit()
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("pilot_conversations", Context.MODE_PRIVATE).edit().clear().commit()
        compose.activityRule.scenario.recreate()
    }

    @Test fun futureMediaButtonsAreHiddenUntilAFileIsConfigured() {
        compose.onNodeWithText("Слушать MP3").assertDoesNotExist()
        compose.onNodeWithText("Смотреть видео").assertDoesNotExist()
        org.junit.Assert.assertTrue(isApprovedLessonMediaUrl("https://drive.google.com/file/d/example/view"))
        org.junit.Assert.assertTrue(isApprovedLessonMediaUrl("https://www.dropbox.com/s/example/audio.mp3"))
        org.junit.Assert.assertFalse(isApprovedLessonMediaUrl("https://drive.google.com.attacker.example/file"))
        org.junit.Assert.assertFalse(isApprovedLessonMediaUrl("javascript:alert(1)"))
    }

    @Test fun exactlyThreeTabsAndCopilotBackReturnsToSelectedCourse() {
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).assertCountEquals(3)
        compose.onNodeWithText("Мой курс").assertIsSelected()
        compose.onNodeWithText("Copilot", useUnmergedTree = true).performClick().assertIsSelected()
        compose.onNodeWithText("День 1 — Моя первая страница").assertExists()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Мой курс").assertIsSelected()
    }

    @Test fun weekOneShowsIntroFiveTheoryAndTwoPracticeLessons() {
        val labels = listOf(
            "Ознакомительное занятие",
            "Урок 1 — Компьютер как система",
            "Урок 2 — Операционная система, программы, Input и Output",
            "Урок 3 — Файлы, папки и первый terminal",
            "Урок 4 — Hardware, интерфейсы и безопасное подключение",
            "Урок 5 — Инженерный подход к новой задаче",
            "Урок 6 — Практика: ноутбук и desktop PC изнутри",
            "Урок 7 — Практика: телемост «Что находится внутри компьютера?»"
        )
        labels.forEach { label ->
            compose.onNodeWithTag("pilot_content").performScrollToNode(hasText(label))
            compose.onNodeWithText(label).assertExists()
        }
    }

    @Test fun recreationRestoresLessonRouteDraftHistoryAndProgress() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        LocalCourseProgressStore(context).save(CourseProgress(2, setOf(1)))
        val store = LocalConversationStore(context)
        store.saveRoute(PilotRoute.COPILOT)
        store.save(2, Conversation(draft = "Draft two").append(MessageRole.STUDENT, "Question two")
            .append(MessageRole.COPILOT, "Answer two"))
        store.save(1, Conversation(draft = "Other draft").append(MessageRole.STUDENT, "Other history"))
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Copilot", useUnmergedTree = true).assertIsSelected()
        compose.onNodeWithText("День 2 — Изменяем страницу").assertExists()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Question two"))
        compose.onNodeWithText("Question two").assertExists()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Draft two"))
        compose.onNodeWithText("Draft two").assertExists()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("День 2 — Изменяем страницу").assertExists()
        org.junit.Assert.assertEquals(CourseProgress(2, setOf(1)), LocalCourseProgressStore(context).load())
    }

    @Test fun studentCopilotAndSystemRolesRemainReachableAtLargeFont() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = LocalConversationStore(context)
        store.save(1, Conversation().append(MessageRole.STUDENT, "Student question")
            .append(MessageRole.COPILOT, "Service answer")
            .append(MessageRole.SYSTEM, notice = SystemNotice.UNAVAILABLE))
        store.saveRoute(PilotRoute.COPILOT)
        compose.activityRule.scenario.onActivity { activity ->
            val restored = PilotDependencies(activity)
            activity.setContent {
                CompositionLocalProvider(LocalDensity provides Density(activity.resources.displayMetrics.density, 1.5f)) {
                    WebKurierTheme { PilotApp(restored.controller, restored.website) }
                }
            }
        }
        for (label in listOf("Вы", "Ответ Copilot", "Система", "Ваш вопрос или инструкция")) {
            compose.onNodeWithTag("pilot_content").performScrollToNode(hasText(label))
            compose.onNodeWithText(label).assertExists()
        }
    }

    @Test fun projectNavigationExplainsUnconfiguredGitHubAndSite() {
        compose.onNodeWithText("Мой проект / GitHub").performClick()
        compose.onNodeWithText("GitHub не подключён").assertExists()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Подключить GitHub"))
        compose.onNodeWithText("Подключить GitHub").performClick()
        compose.onNodeWithText("Не удалось подключить GitHub").assertExists()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Посмотреть результат на сайте"))
        compose.onNodeWithText("Посмотреть результат на сайте").assertIsNotEnabled()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Неделя 1").assertExists()
    }

    @Test fun projectShowsSafeUpdateAndUninstallControls() {
        compose.onNodeWithText("Мой проект / GitHub").performClick()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Проверить обновление"))
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Архитектура контента Phase 1"))
        compose.onNodeWithText("Архитектура контента Phase 1").assertIsEnabled()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Проверить обновление"))
        compose.onNodeWithText("Проверить обновление").assertIsEnabled()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Удалить приложение"))
        compose.onNodeWithText("Удалить приложение").assertIsEnabled()
    }

    @Test fun navigationAndResultStayReachableWithLargeFont() {
        compose.activityRule.scenario.onActivity { activity ->
            val dependencies = PilotDependencies(activity)
            activity.setContent {
                CompositionLocalProvider(LocalDensity provides Density(activity.resources.displayMetrics.density, 1.5f)) {
                    WebKurierTheme { PilotApp(dependencies.controller, dependencies.website) }
                }
            }
        }
        compose.onNodeWithText("Мой проект / GitHub").performClick()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Посмотреть результат на сайте"))
        compose.onNodeWithText("Посмотреть результат на сайте").assertIsNotEnabled()
        compose.onNodeWithText("Мой курс").performClick()
        compose.onNodeWithText("Неделя 1").assertExists()
    }

    @Test fun unavailableCopilotAllowsDraftButDoesNotSend() {
        compose.onNodeWithText("Copilot", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Ваш вопрос или инструкция"))
        compose.onNodeWithText("Ваш вопрос или инструкция").performTextInput("Добавь кнопку")
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Отправить"))
        compose.onNodeWithText("Отправить").assertIsNotEnabled()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Я выполнил(а) задание"))
        compose.onNodeWithText("Я выполнил(а) задание").assertIsEnabled().performClick()
        compose.onNodeWithText("Неделя 1").assertExists()
    }
}
