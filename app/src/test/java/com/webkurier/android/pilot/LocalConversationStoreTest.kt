package com.webkurier.android.pilot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalConversationStoreTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val preferences get() = context.getSharedPreferences("pilot_conversations", Context.MODE_PRIVATE)
    @Before fun clear() { preferences.edit().clear().commit() }

    @Test fun allLessonsAndRouteSurviveNewStoreInstance() {
        val store = LocalConversationStore(context)
        for (day in 1..3) store.save(day, Conversation(draft = "Draft $day")
            .append(MessageRole.STUDENT, "Question $day").append(MessageRole.COPILOT, "Answer $day")
            .append(MessageRole.SYSTEM, notice = SystemNotice.ERROR))
        store.saveRoute(PilotRoute.COPILOT)
        val restored = LocalConversationStore(context)
        for (day in 1..3) {
            assertEquals(store.load(day), restored.load(day))
            assertEquals("Draft $day", restored.load(day).draft)
            assertEquals(3, restored.load(day).entries.size)
        }
        assertEquals(PilotRoute.COPILOT, restored.loadRoute())
    }

    @Test fun pendingStatusIsNeverRestoredOrRetried() {
        val store = LocalConversationStore(context)
        store.save(1, Conversation(status = MessageStatus.SENDING, draft = "Pending")
            .append(MessageRole.STUDENT, "Pending"))
        val restored = LocalConversationStore(context).load(1)
        assertEquals(MessageStatus.EMPTY, restored.status)
        assertEquals("Pending", restored.draft)
        assertEquals(MessageRole.STUDENT, restored.entries.single().role)
    }

    @Test fun malformedAndWronglyTypedValuesRecoverWithoutDamagingOtherLessons() {
        val store = LocalConversationStore(context)
        store.save(2, Conversation(draft = "Keep"))
        val invalid = listOf("{", "[]", "{}", "null", "{\"version\":2}",
            "{\"version\":1,\"draft\":42,\"entries\":[]}")
        for (raw in invalid) {
            preferences.edit().putString("day_1", raw).commit()
            assertEquals(Conversation(), store.load(1))
            assertEquals("Keep", store.load(2).draft)
        }
        preferences.edit().putInt("day_1", 42).putInt("route", 42).commit()
        assertEquals(Conversation(), store.load(1))
        assertEquals(PilotRoute.COURSE, store.loadRoute())
        preferences.edit().putString("route", "TRANSLATOR").commit()
        assertEquals(PilotRoute.COURSE, store.loadRoute())
        assertEquals(Conversation(), store.load(8))
    }

    private fun entry(id: Long = 1, role: String = "STUDENT", text: String = "Hello") =
        JSONObject().put("id", id).put("role", role).put("text", text)
    private fun write(entries: JSONArray, draft: String = "") {
        preferences.edit().putString("day_1", JSONObject().put("version", 1)
            .put("draft", draft).put("entries", entries).toString()).commit()
    }

    @Test fun duplicateDescendingAndOverflowIdsAreRejected() {
        val store = LocalConversationStore(context)
        for (ids in listOf(listOf(1L, 1L), listOf(2L, 1L), listOf(0L), listOf(-1L), listOf(Long.MAX_VALUE))) {
            val array = JSONArray()
            ids.forEach { array.put(entry(it)) }
            write(array)
            assertEquals(Conversation(), store.load(1))
        }
    }

    @Test fun unknownRolesNoticesAndInvalidTextAreRejected() {
        val store = LocalConversationStore(context)
        for (invalid in listOf(entry(role = "OTHER"), entry(text = " "), entry(role = "SYSTEM"),
            entry().put("notice", "ERROR"), entry(role = "SYSTEM", text = "").put("notice", "OTHER"),
            entry(text = "x".repeat(8001)))) {
            write(JSONArray().put(invalid))
            assertEquals(Conversation(), store.load(1))
        }
    }

    @Test fun oversizedStoredPayloadDraftAndHistoryRecoverSafely() {
        val store = LocalConversationStore(context)
        preferences.edit().putString("day_1", "x".repeat(LocalConversationStore.MAX_STORED_LENGTH + 1)).commit()
        assertEquals(Conversation(), store.load(1))
        write(JSONArray(), "x".repeat(2001))
        assertEquals(Conversation(), store.load(1))
        val array = JSONArray()
        repeat(41) { array.put(entry(it + 1L)) }
        write(array)
        assertEquals(Conversation(), store.load(1))
    }

    @Test fun saveEnforcesHistoryAndTextBounds() {
        val store = LocalConversationStore(context)
        store.save(1, Conversation(draft = "d".repeat(3000), entries = (1L..50L).map {
            ConversationEntry(it, MessageRole.COPILOT, "a".repeat(9000))
        }))
        val restored = LocalConversationStore(context).load(1)
        assertEquals(40, restored.entries.size)
        assertEquals(11L, restored.entries.first().id)
        assertTrue(restored.entries.all { it.text.length == 8000 })
        assertEquals(2000, restored.draft.length)
    }
}
