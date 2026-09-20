package com.webkurier.android.pilot

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MessageStatus { EMPTY, SENDING, UNAVAILABLE, RECEIVED, ERROR }

/** Owned by the app composition; its scope cancels suspended work on disposal. */
class PilotController(
    private val progressStore: CourseProgressStore,
    private val github: GitHubAuthService,
    private val copilot: CopilotService,
    private val conversationStore: ConversationStore = MemoryConversationStore()
) {
    val isGitHubConfigured = github.isConfigured
    val isCopilotConfigured = copilot.isConfigured
    private val mutableProgress = MutableStateFlow(progressStore.load())
    val progress = mutableProgress.asStateFlow()
    private val mutableConnection = MutableStateFlow(GitHubConnection())
    val connection = mutableConnection.asStateFlow()
    private val lessons = PilotCourse.days.associateWith { conversationStore.load(it).copy(status = MessageStatus.EMPTY) }.toMutableMap()
    private val mutableConversation = MutableStateFlow(lessons.getValue(progress.value.currentDay))
    val conversation = mutableConversation.asStateFlow()
    private val mutableRoute = MutableStateFlow(conversationStore.loadRoute())
    val route = mutableRoute.asStateFlow()

    fun navigate(route: PilotRoute) {
        conversationStore.saveRoute(route)
        mutableRoute.value = route
    }

    fun setDraft(text: String) {
        if (text.length > MAX_MESSAGE_LENGTH || conversation.value.status == MessageStatus.SENDING) return
        updateConversation(progress.value.currentDay, conversation.value.copy(draft = text))
    }

    private fun updateConversation(day: Int, value: Conversation) {
        lessons[day] = value
        conversationStore.save(day, value)
        if (progress.value.currentDay == day) mutableConversation.value = value
    }

    fun selectDay(day: Int) {
        if (day !in PilotCourse.days || mutableConversation.value.status == MessageStatus.SENDING) return
        updateProgress(mutableProgress.value.select(day))
        mutableConversation.value = lessons.getValue(day)
    }

    fun completeDay() = updateProgress(mutableProgress.value.completeCurrent())

    private fun updateProgress(value: CourseProgress) {
        progressStore.save(value)
        mutableProgress.value = value
    }

    suspend fun connect() {
        if (mutableConnection.value.status in setOf(ConnectionStatus.CONNECTING, ConnectionStatus.CONNECTED)) return
        mutableConnection.value = GitHubConnection(ConnectionStatus.CONNECTING)
        try {
            mutableConnection.value = when (val result = github.connect()) {
                is GitHubAuthResult.Connected -> GitHubConnection(ConnectionStatus.CONNECTED, result.identity)
                is GitHubAuthResult.Unavailable -> GitHubConnection(ConnectionStatus.ERROR, error = result.reason)
            }
        } catch (cancelled: CancellationException) {
            mutableConnection.value = GitHubConnection()
            throw cancelled
        } catch (_: Exception) {
            mutableConnection.value = GitHubConnection(ConnectionStatus.ERROR, error = ConnectionError.FAILED)
        }
    }

    suspend fun send(message: String = conversation.value.draft) {
        if (message.isBlank() || message.length > MAX_MESSAGE_LENGTH ||
            mutableConversation.value.status == MessageStatus.SENDING) return
        val day = progress.value.currentDay
        val before = conversation.value
        val request = CopilotRequest(day, message.trim(), connection.value.identity?.project,
            before.entries.filter { it.role != MessageRole.SYSTEM }.takeLast(12).toList())
        updateConversation(day, before.append(MessageRole.STUDENT, message.trim())
            .copy(status = MessageStatus.SENDING, draft = message))
        try {
            val current = when (val reply = copilot.send(request)) {
                CopilotReply.Unavailable -> lessons.getValue(day).append(MessageRole.SYSTEM, notice = SystemNotice.UNAVAILABLE)
                    .copy(status = MessageStatus.UNAVAILABLE)
                is CopilotReply.Message -> {
                    require(reply.text.isNotBlank())
                    lessons.getValue(day).append(MessageRole.COPILOT, reply.text)
                        .copy(status = MessageStatus.RECEIVED, draft = "")
                }
            }
            updateConversation(day, current)
        } catch (cancelled: CancellationException) {
            updateConversation(day, lessons.getValue(day).append(MessageRole.SYSTEM, notice = SystemNotice.CANCELLED)
                .copy(status = MessageStatus.EMPTY))
            throw cancelled
        } catch (_: Exception) {
            updateConversation(day, lessons.getValue(day).append(MessageRole.SYSTEM, notice = SystemNotice.ERROR)
                .copy(status = MessageStatus.ERROR))
        }
    }

    companion object { const val MAX_MESSAGE_LENGTH = 2000 }
}
