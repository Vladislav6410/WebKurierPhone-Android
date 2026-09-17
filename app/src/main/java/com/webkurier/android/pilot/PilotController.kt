package com.webkurier.android.pilot

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MessageStatus { EMPTY, SENDING, UNAVAILABLE, RECEIVED, ERROR }
data class Conversation(val status: MessageStatus = MessageStatus.EMPTY, val reply: String? = null)

/** Owned by the app composition; its scope cancels suspended work on disposal. */
class PilotController(
    private val progressStore: CourseProgressStore,
    private val github: GitHubAuthService,
    private val copilot: CopilotService
) {
    private val mutableProgress = MutableStateFlow(progressStore.load())
    val progress = mutableProgress.asStateFlow()
    private val mutableConnection = MutableStateFlow(GitHubConnection())
    val connection = mutableConnection.asStateFlow()
    private val mutableConversation = MutableStateFlow(Conversation())
    val conversation = mutableConversation.asStateFlow()

    fun selectDay(day: Int) {
        if (day !in PilotCourse.days || mutableConversation.value.status == MessageStatus.SENDING) return
        updateProgress(mutableProgress.value.select(day))
        mutableConversation.value = Conversation()
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

    suspend fun send(message: String) {
        if (message.isBlank() || message.length > MAX_MESSAGE_LENGTH ||
            mutableConversation.value.status == MessageStatus.SENDING) return
        mutableConversation.value = Conversation(MessageStatus.SENDING)
        try {
            val request = CopilotRequest(mutableProgress.value.currentDay, message.trim(), connection.value.identity?.project)
            mutableConversation.value = when (val reply = copilot.send(request)) {
                CopilotReply.Unavailable -> Conversation(MessageStatus.UNAVAILABLE)
                is CopilotReply.Message -> Conversation(MessageStatus.RECEIVED, reply.text)
            }
        } catch (cancelled: CancellationException) {
            mutableConversation.value = Conversation()
            throw cancelled
        } catch (_: Exception) {
            mutableConversation.value = Conversation(MessageStatus.ERROR)
        }
    }

    companion object { const val MAX_MESSAGE_LENGTH = 2000 }
}
