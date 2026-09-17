package com.webkurier.android.pilot

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.webkurier.android.R
import kotlinx.coroutines.launch

@Composable
fun PilotApp(dependencies: PilotDependencies) {
    val controller = dependencies.controller
    val progress by controller.progress.collectAsState()
    val connection by controller.connection.collectAsState()
    val conversation by controller.conversation.collectAsState()
    var route by rememberSaveable { mutableStateOf(PilotRoute.COURSE) }
    // Drafts stay in memory only and are scoped to the lesson.
    var draft by remember(progress.currentDay) { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    BackHandler(route != PilotRoute.COURSE) { route = PilotRoute.COURSE }

    Scaffold(
        bottomBar = {
            NavigationBar {
                PilotRoute.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = route == destination,
                        onClick = { route = destination },
                        icon = { Text(stringResource(routeLabel(destination))) }
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
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
                    item {
                        Text(stringResource(R.string.pilot_welcome), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.pilot_journey))
                    }
                    item { ConnectionCard(connection) { scope.launch { controller.connect() } } }
                    item {
                        Text(stringResource(R.string.pilot_week_one), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.pilot_available))
                        Text(stringResource(R.string.pilot_local_progress))
                    }
                    items(PilotCourse.days) { day ->
                        PilotCard {
                            Text(stringResource(dayTitle(day)), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(stateLabel(progress.state(day))))
                            Text(stringResource(dayGoal(day)))
                            Button(
                                enabled = conversation.status != MessageStatus.SENDING,
                                onClick = { controller.selectDay(day); route = PilotRoute.COPILOT }
                            ) { Text(stringResource(R.string.pilot_continue)) }
                        }
                    }
                    item { WebsiteAction(dependencies.website) }
                    item { Text(stringResource(R.string.pilot_roadmap), style = MaterialTheme.typography.titleLarge) }
                    items(PilotCourse.weeks.drop(1)) { week ->
                        PilotCard {
                            Text(stringResource(R.string.pilot_week, week.number))
                            Text(stringResource(R.string.pilot_locked))
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
                        PilotCard {
                            Text(stringResource(R.string.pilot_ai_unavailable))
                            Text(stringResource(R.string.pilot_conversation), style = MaterialTheme.typography.titleMedium)
                            Text(conversation.reply ?: stringResource(when (conversation.status) {
                                MessageStatus.EMPTY -> R.string.pilot_empty_conversation
                                MessageStatus.SENDING -> R.string.pilot_sending
                                MessageStatus.UNAVAILABLE -> R.string.pilot_not_sent
                                MessageStatus.ERROR -> R.string.pilot_message_error
                                MessageStatus.RECEIVED -> R.string.pilot_empty_conversation
                            }))
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { if (it.length <= PilotController.MAX_MESSAGE_LENGTH) draft = it },
                            enabled = conversation.status != MessageStatus.SENDING,
                            label = { Text(stringResource(R.string.pilot_message)) },
                            supportingText = { Text(stringResource(R.string.pilot_message_limit)) },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            enabled = draft.isNotBlank() && conversation.status != MessageStatus.SENDING,
                            onClick = { scope.launch { controller.send(draft) } },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.pilot_send)) }
                    }
                    item { WebsiteAction(dependencies.website) }
                    item {
                        Text(stringResource(R.string.pilot_local_progress))
                        Button(
                            enabled = progress.state(progress.currentDay) != CourseState.COMPLETED,
                            onClick = { controller.completeDay(); route = PilotRoute.COURSE }
                        ) { Text(stringResource(R.string.pilot_complete)) }
                    }
                }
                PilotRoute.PROJECT -> {
                    item { ConnectionCard(connection) { scope.launch { controller.connect() } } }
                    item { ProjectContext(connection) }
                    item {
                        Text(stringResource(dayTitle(progress.currentDay)), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(dayTask(progress.currentDay)))
                        Button(onClick = { route = PilotRoute.COPILOT }) { Text(stringResource(R.string.pilot_ask)) }
                    }
                    item { WebsiteAction(dependencies.website) }
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(connection: GitHubConnection, onConnect: () -> Unit) {
    PilotCard {
        Text(stringResource(when (connection.status) {
            ConnectionStatus.NOT_CONNECTED -> R.string.pilot_not_connected
            ConnectionStatus.CONNECTING -> R.string.pilot_connecting
            ConnectionStatus.CONNECTED -> R.string.pilot_connected
            ConnectionStatus.ERROR -> R.string.pilot_connection_error
        }), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.pilot_github_intro))
        connection.identity?.let { Text(stringResource(R.string.pilot_identity, it.login)) }
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
                browserFailed = try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website.url)).addCategory(Intent.CATEGORY_BROWSABLE))
                    false
                } catch (_: ActivityNotFoundException) { true } catch (_: SecurityException) { true }
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
