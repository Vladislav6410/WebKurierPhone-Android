package com.webkurier.android.pilot

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.webkurier.android.R
import kotlinx.coroutines.launch

@Composable
fun PilotApp(controller: PilotController, website: WebsiteResult) {
    val progress by controller.progress.collectAsState()
    val connection by controller.connection.collectAsState()
    val conversation by controller.conversation.collectAsState()
    val route by controller.route.collectAsState()
    val context = LocalContext.current
    var selectedLesson by remember { mutableStateOf<SimpleLesson?>(null) }
    val listState = remember(route, progress.currentDay, selectedLesson?.assetPath) { LazyListState() }
    val draft = conversation.draft
    val scope = rememberCoroutineScope()
    BackHandler(selectedLesson != null || route != PilotRoute.COURSE) {
        if (selectedLesson != null) selectedLesson = null else controller.navigate(PilotRoute.COURSE)
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        bottomBar = {
            NavigationBar {
                Row(Modifier.fillMaxWidth().selectableGroup()) {
                    PilotRoute.entries.forEach { destination ->
                        Text(
                            text = stringResource(routeLabel(destination)),
                            textAlign = TextAlign.Center,
                            color = if (route == destination) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f).heightIn(min = 64.dp)
                                .selectable(selected = route == destination, role = Role.Tab, onClick = { controller.navigate(destination) })
                                .padding(horizontal = 8.dp, vertical = 16.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).testTag("pilot_content"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(stringResource(R.string.pilot_brand), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.pilot_version), style = MaterialTheme.typography.labelLarge)
            }
            when (route) {
                PilotRoute.COURSE -> {
                    val openedLesson = selectedLesson
                    if (openedLesson != null) {
                        item {
                            Text(stringResource(openedLesson.titleRes), style = MaterialTheme.typography.titleLarge)
                            Text(loadLessonText(context, openedLesson.assetPath))
                        }
                        item {
                            Button(
                                onClick = { selectedLesson = null },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(stringResource(R.string.pilot_back_to_week)) }
                        }
                    } else {
                        item {
                            Text(stringResource(R.string.pilot_week_one), style = MaterialTheme.typography.titleLarge)
                            Text(stringResource(R.string.pilot_week_one_simple_intro))
                        }
                        items(simpleWeekOneLessons) { lesson ->
                            PilotCard {
                                Text(stringResource(lesson.titleRes), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.pilot_available))
                                var pdfOpenFailed by remember(lesson.pdfUrl) { mutableStateOf(false) }
                                if (lesson.assetPath.isNotBlank()) {
                                    Button(
                                        onClick = { selectedLesson = lesson },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(stringResource(R.string.pilot_read_in_app)) }
                                }
                                Button(
                                    onClick = { pdfOpenFailed = !openLessonPdf(context, lesson.pdfUrl) },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.pilot_open_pdf)) }
                                if (pdfOpenFailed) Text(stringResource(R.string.pilot_browser_missing))
                            }
                        }
                    }
                }
                PilotRoute.COPILOT -> {
                    item {
                        Text(stringResource(R.string.pilot_teacher), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(dayTitle(progress.currentDay)))
                        Text(stringResource(dayTask(progress.currentDay)))
                    }
                    item { ProjectContext(connection) }
                    item {
                        Text(stringResource(R.string.pilot_conversation), style = MaterialTheme.typography.titleMedium)
                        if (conversation.entries.isEmpty()) Text(stringResource(R.string.pilot_empty_conversation))
                    }
                    items(conversation.entries, key = { "message_${progress.currentDay}_${it.id}" }) { entry ->
                        ConversationMessage(entry)
                    }
                    item {
                        if (conversation.status == MessageStatus.SENDING) Text(stringResource(R.string.pilot_sending),
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    item {
                        if (!controller.isCopilotConfigured) Text(stringResource(R.string.pilot_ai_unavailable))
                        OutlinedTextField(
                            value = draft,
                            onValueChange = controller::setDraft,
                            enabled = conversation.status != MessageStatus.SENDING,
                            label = { Text(stringResource(R.string.pilot_message)) },
                            supportingText = { Text(stringResource(R.string.pilot_message_limit)) },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            enabled = controller.isCopilotConfigured && draft.isNotBlank() && conversation.status != MessageStatus.SENDING,
                            onClick = { scope.launch { controller.send() } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(if (conversation.status == MessageStatus.SENDING) R.string.pilot_sending else R.string.pilot_send)) }
                    }
                    item { WebsiteAction(website) }
                    item {
                        Text(stringResource(R.string.pilot_local_progress))
                        Button(
                            enabled = progress.state(progress.currentDay) != CourseState.COMPLETED,
                            onClick = { controller.completeDay(); controller.navigate(PilotRoute.COURSE) }
                        ) { Text(stringResource(R.string.pilot_complete)) }
                    }
                }
                PilotRoute.PROJECT -> {
                    item { ConnectionCard(connection, controller.isGitHubConfigured) { scope.launch { controller.connect() } } }
                    item { ProjectContext(connection) }
                    item {
                        Text(stringResource(dayTitle(progress.currentDay)), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(dayTask(progress.currentDay)))
                        Button(onClick = { controller.navigate(PilotRoute.COPILOT) }) { Text(stringResource(R.string.pilot_ask)) }
                    }
                    item { WebsiteAction(website) }
                    item { AppManagementCard() }
                }
            }
        }
    }
}

@Composable
private fun ConversationMessage(entry: ConversationEntry) {
    val color = when (entry.role) {
        MessageRole.STUDENT -> MaterialTheme.colorScheme.primaryContainer
        MessageRole.COPILOT -> MaterialTheme.colorScheme.secondaryContainer
        MessageRole.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
    }
    Card(colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.fillMaxWidth()
        .testTag("message_${entry.role}_${entry.id}")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(when (entry.role) {
                MessageRole.STUDENT -> R.string.pilot_role_student
                MessageRole.COPILOT -> R.string.pilot_role_copilot
                MessageRole.SYSTEM -> R.string.pilot_role_system
            }), style = MaterialTheme.typography.labelLarge)
            Text(entry.notice?.let { stringResource(when (it) {
                SystemNotice.UNAVAILABLE -> R.string.pilot_not_sent
                SystemNotice.ERROR -> R.string.pilot_message_error
                SystemNotice.CANCELLED -> R.string.pilot_message_cancelled
            }) } ?: entry.text, modifier = if (entry.role == MessageRole.SYSTEM)
                Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier)
        }
    }
}

@Composable
private fun ConnectionCard(connection: GitHubConnection, configured: Boolean, onConnect: () -> Unit) {
    PilotCard {
        Text(stringResource(when (connection.status) {
            ConnectionStatus.NOT_CONNECTED -> R.string.pilot_not_connected
            ConnectionStatus.CONNECTING -> R.string.pilot_connecting
            ConnectionStatus.CONNECTED -> R.string.pilot_connected
            ConnectionStatus.ERROR -> R.string.pilot_connection_error
        }), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Text(stringResource(R.string.pilot_github_intro))
        connection.identity?.let { Text(stringResource(R.string.pilot_identity, it.login)) }
        if (!configured && connection.error == null) Text(stringResource(R.string.pilot_auth_unavailable))
        connection.error?.let {
            Text(stringResource(if (it == ConnectionError.NOT_CONFIGURED) R.string.pilot_auth_unavailable else R.string.pilot_auth_failed))
        }
        if (connection.status != ConnectionStatus.CONNECTED) {
            Button(onClick = onConnect, enabled = connection.status != ConnectionStatus.CONNECTING) {
                Text(stringResource(if (connection.status == ConnectionStatus.ERROR) R.string.pilot_retry else R.string.pilot_connect))
            }
        }
    }
}

@Composable
private fun ProjectContext(connection: GitHubConnection) {
    val project = connection.identity?.project
    PilotCard {
        if (project == null) {
            Text(stringResource(R.string.pilot_context_missing))
            Text(stringResource(R.string.pilot_no_project))
        } else {
            Text(stringResource(R.string.pilot_repository, project.repository))
            project.description?.let { Text(it) }
        }
    }
}

@Composable
private fun WebsiteAction(website: WebsiteResult) {
    val context = LocalContext.current
    var browserFailed by remember(website) { mutableStateOf(false) }
    Button(
        enabled = website is WebsiteResult.Ready,
        onClick = {
            if (website is WebsiteResult.Ready) {
                browserFailed = !openWebsite(context, website)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.pilot_view_site)) }
    when {
        browserFailed -> Text(stringResource(R.string.pilot_browser_missing))
        website == WebsiteResult.NotConfigured -> Text(stringResource(R.string.pilot_site_missing))
        website == WebsiteResult.Invalid -> Text(stringResource(R.string.pilot_site_invalid))
    }
}

internal fun openWebsite(context: Context, website: WebsiteResult): Boolean {
    if (website !is WebsiteResult.Ready) return false
    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website.url)).addCategory(Intent.CATEGORY_BROWSABLE))
        true
    } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }
}

internal fun openLessonPdf(context: Context, url: String): Boolean =
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE))
        true
    } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }

private const val UPDATE_URL = "https://github.com/Vladislav6410/WebKurierPhone-Android/releases"\nprivate const val CONTENT_ARCHITECTURE_URL = "https://drive.google.com/file/d/1mSR7njhyl2XJv0yN0-ZjhVHr2RCZZDCw/view?usp=drivesdk"

@Composable
private fun AppManagementCard() {
    val context = LocalContext.current
    var actionFailed by remember { mutableStateOf(false) }
    PilotCard {
        Text(stringResource(R.string.pilot_app_management), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.pilot_app_management_note))
        Button(
            onClick = { actionFailed = !openContentArchitecture(context) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.pilot_content_architecture)) }
        Button(
            onClick = { actionFailed = !openUpdateChannel(context) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.pilot_check_update)) }
        Button(
            onClick = { actionFailed = !openUninstallConfirmation(context) },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.pilot_uninstall_app)) }
        if (actionFailed) Text(stringResource(R.string.pilot_system_action_failed))
    }
}

internal fun openContentArchitecture(context: Context): Boolean =
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(CONTENT_ARCHITECTURE_URL)).addCategory(Intent.CATEGORY_BROWSABLE)
        )
        true
    } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }

internal fun openUpdateChannel(context: Context): Boolean =
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(UPDATE_URL)).addCategory(Intent.CATEGORY_BROWSABLE)
        )
        true
    } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }

internal fun openUninstallConfirmation(context: Context): Boolean =
    try {
        context.startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${context.packageName}")))
        true
    } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }

@Composable
private fun PilotCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

private fun routeLabel(route: PilotRoute) = when (route) {
    PilotRoute.COURSE -> R.string.pilot_course
    PilotRoute.COPILOT -> R.string.pilot_copilot
    PilotRoute.PROJECT -> R.string.pilot_project
}

private fun dayTitle(day: Int) = when (day) {
    1 -> R.string.pilot_day_one
    2 -> R.string.pilot_day_two
    else -> R.string.pilot_day_three
}

private fun dayGoal(day: Int) = when (day) {
    1 -> R.string.pilot_goal_one
    2 -> R.string.pilot_goal_two
    else -> R.string.pilot_goal_three
}

private fun dayTask(day: Int) = when (day) {
    1 -> R.string.pilot_task_one
    2 -> R.string.pilot_task_two
    else -> R.string.pilot_task_three
}

private fun stateLabel(state: CourseState) = when (state) {
    CourseState.AVAILABLE -> R.string.pilot_available
    CourseState.CURRENT -> R.string.pilot_current
    CourseState.COMPLETED -> R.string.pilot_completed
    CourseState.LOCKED -> R.string.pilot_locked
}


private data class SimpleLesson(
    val titleRes: Int,
    val assetPath: String,
    val pdfUrl: String
)

private val simpleWeekOneLessons = listOf(
    SimpleLesson(
        R.string.pilot_intro_lesson,
        "education/week01/intro.txt",
        "https://drive.google.com/file/d/1ulIXKzbicd67C6tE4JCmvPm3YD_pHmmA/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_one_simple,
        "education/week01/lesson01.txt",
        "https://drive.google.com/file/d/1avmoQQ7U6H0rSzY5nwmww0qeT_4xlNQl/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_two_simple,
        "",
        "https://drive.google.com/file/d/1wW8gkm0pdsAsu2YkRaf4OciDX-UIT5sA/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_three_simple,
        "",
        "https://drive.google.com/file/d/1XNeUyApAymFx3zsH-WKScdwpTbeVM5Na/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_four_simple,
        "",
        "https://drive.google.com/file/d/1Pp1KeMZwwFWei2TJyp_6YIRmVI_VLsWz/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_five_simple,
        "",
        "https://drive.google.com/file/d/16Z-zfr4m27yFFGgLTu3IZOltGwfcqRoD/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_six_simple,
        "",
        "https://drive.google.com/file/d/1m_njgjTjSsuwFJvVrv5Yj-wJ2SsEmNjn/view?usp=drivesdk"
    ),
    SimpleLesson(
        R.string.pilot_lesson_seven_simple,
        "",
        "https://drive.google.com/file/d/1oJfA4cGtBwEr2HfzSfzkp5sEMWS3WBDZ/view?usp=drivesdk"
    )
)
private fun loadLessonText(context: Context, assetPath: String): String =
    runCatching {
        context.assets.open(assetPath).bufferedReader().use { it.readText() }
    }.getOrElse {
        "Материал урока пока недоступен."
    }
