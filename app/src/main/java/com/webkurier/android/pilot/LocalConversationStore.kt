package com.webkurier.android.pilot

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Device-only history. Never stores authentication, project identity or pending requests. */
internal class LocalConversationStore(context: Context) : ConversationStore {
    private val preferences = context.getSharedPreferences("pilot_conversations", Context.MODE_PRIVATE)

    override fun loadRoute(): PilotRoute = PilotRoute.entries.firstOrNull {
        it.name == preferences.all["route"]
    } ?: PilotRoute.COURSE

    override fun saveRoute(route: PilotRoute) { preferences.edit().putString("route", route.name).apply() }

    override fun load(day: Int): Conversation {
        if (day !in PilotCourse.days) return Conversation()
        val raw = preferences.all["day_$day"] as? String ?: return Conversation()
        if (raw.length > MAX_STORED_LENGTH) return Conversation()
        return try {
            val root = JSONObject(raw)
            require(root.getInt("version") == 1)
            val draft = root.get("draft") as String
            require(draft.length <= PilotController.MAX_MESSAGE_LENGTH)
            val array = root.getJSONArray("entries")
            require(array.length() <= Conversation.MAX_HISTORY)
            var previous = 0L
            val entries = (0 until array.length()).map { index ->
                val entry = array.getJSONObject(index)
                val id = entry.getLong("id")
                require(id > previous && id < Long.MAX_VALUE - Conversation.MAX_HISTORY)
                previous = id
                val role = MessageRole.valueOf(entry.getString("role"))
                val text = entry.get("text") as String
                require(text.length <= Conversation.MAX_ENTRY_LENGTH)
                val notice = if (entry.has("notice")) SystemNotice.valueOf(entry.getString("notice")) else null
                require((role == MessageRole.SYSTEM) == (notice != null))
                require(if (role == MessageRole.SYSTEM) text.isEmpty() else text.isNotBlank())
                ConversationEntry(id, role, text, notice)
            }
            Conversation(entries = entries, draft = draft)
        } catch (_: Exception) { Conversation() }
    }

    override fun save(day: Int, conversation: Conversation) {
        if (day !in PilotCourse.days) return
        val entries = JSONArray()
        conversation.entries.takeLast(Conversation.MAX_HISTORY).forEach { entry ->
            entries.put(JSONObject().put("id", entry.id).put("role", entry.role.name)
                .put("text", entry.text.take(Conversation.MAX_ENTRY_LENGTH)).apply {
                    entry.notice?.let { put("notice", it.name) }
                })
        }
        val raw = JSONObject().put("version", 1)
            .put("draft", conversation.draft.take(PilotController.MAX_MESSAGE_LENGTH))
            .put("entries", entries).toString()
        preferences.edit().putString("day_$day", raw).apply()
    }

    companion object { const val MAX_STORED_LENGTH = 2_000_000 }
}
