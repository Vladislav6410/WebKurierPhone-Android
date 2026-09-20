package com.webkurier.android.pilot

enum class MessageRole { STUDENT, COPILOT, SYSTEM }
enum class SystemNotice { UNAVAILABLE, ERROR, CANCELLED }

data class ConversationEntry(
    val id: Long,
    val role: MessageRole,
    val text: String = "",
    val notice: SystemNotice? = null
)

data class Conversation(
    val status: MessageStatus = MessageStatus.EMPTY,
    val entries: List<ConversationEntry> = emptyList(),
    val draft: String = ""
) {
    fun append(role: MessageRole, text: String = "", notice: SystemNotice? = null): Conversation = copy(
        entries = (entries + ConversationEntry((entries.lastOrNull()?.id ?: 0) + 1, role,
            text.take(MAX_ENTRY_LENGTH), notice)).takeLast(MAX_HISTORY)
    )

    companion object {
        const val MAX_HISTORY = 40
        const val MAX_ENTRY_LENGTH = 8000
    }
}

interface ConversationStore {
    fun load(day: Int): Conversation
    fun save(day: Int, conversation: Conversation)
    fun loadRoute(): PilotRoute = PilotRoute.COURSE
    fun saveRoute(route: PilotRoute) {}
}

class MemoryConversationStore : ConversationStore {
    private val lessons = mutableMapOf<Int, Conversation>()
    override fun load(day: Int) = lessons[day] ?: Conversation()
    override fun save(day: Int, conversation: Conversation) { lessons[day] = conversation }
}
