package com.webkurier.android.pilot

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
        compose.activityRule.scenario.recreate()
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
        compose.onNodeWithText("Создадим ваш первый сайт").assertExists()
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
        compose.onNodeWithText("Создадим ваш первый сайт").assertExists()
    }

    @Test fun unavailableCopilotAllowsDraftButDoesNotSend() {
        compose.onNodeWithText("Copilot", useUnmergedTree = true).performClick()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Ваш вопрос или инструкция"))
        compose.onNodeWithText("Ваш вопрос или инструкция").performTextInput("Добавь кнопку")
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Отправить"))
        compose.onNodeWithText("Отправить").assertIsNotEnabled()
        compose.onNodeWithTag("pilot_content").performScrollToNode(hasText("Я выполнил(а) задание"))
        compose.onNodeWithText("Я выполнил(а) задание").assertIsEnabled().performClick()
        compose.onNodeWithText("Создадим ваш первый сайт").assertExists()
    }
}
