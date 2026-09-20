package com.webkurier.android.pilot

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PilotConversationTest {
    private class Progress : CourseProgressStore {
        var value = CourseProgress()
        override fun load() = value
        override fun save(progress: CourseProgress) { value = progress }
    }
    private fun controller(store: ConversationStore = MemoryConversationStore(),
        service: CopilotService = CopilotService { CopilotReply.Message("Reply for ${it.day}") }) =
        PilotController(Progress(), UnconfiguredGitHubAuth, service, store)

    @Test fun studentAndReplyHaveStableUniqueOrderedIds() = runBlocking {
        val c = controller()
        c.send("First")
        c.send("Second")
        assertEquals(listOf(1L, 2L, 3L, 4L), c.conversation.value.entries.map { it.id })
        assertEquals(listOf(MessageRole.STUDENT, MessageRole.COPILOT, MessageRole.STUDENT, MessageRole.COPILOT),
            c.conversation.value.entries.map { it.role })
        assertEquals(listOf("First", "Reply for 1", "Second", "Reply for 1"), c.conversation.value.entries.map { it.text })
    }

    @Test fun allThreeLessonsRestoreTheirOwnHistoryAndDraft() = runBlocking {
        val c = controller()
        for (day in 1..3) {
            c.selectDay(day)
            assertTrue(c.conversation.value.entries.isEmpty())
            c.send("Question $day")
            c.setDraft("Draft $day")
        }
        for (day in listOf(2, 1, 3, 1)) {
            c.selectDay(day)
            assertEquals("Draft $day", c.conversation.value.draft)
            assertEquals(listOf("Question $day", "Reply for $day"), c.conversation.value.entries.map { it.text })
        }
    }

    @Test fun blankAndOversizedDraftsCannotCreateEntries() = runBlocking {
        val c = controller()
        c.setDraft("  ")
        c.send()
        assertTrue(c.conversation.value.entries.isEmpty())
        c.setDraft("x".repeat(2001))
        assertEquals("  ", c.conversation.value.draft)
        c.setDraft("x".repeat(2000))
        c.send()
        assertEquals(2000, c.conversation.value.entries.first().text.length)
        assertEquals("", c.conversation.value.draft)
    }

    @Test fun pendingRequestBlocksDuplicateDraftEditsAndLessonSwitch() = runBlocking {
        val response = CompletableDeferred<CopilotReply>()
        var calls = 0
        val c = controller(service = CopilotService { calls++; response.await() })
        c.selectDay(2)
        c.setDraft("Question")
        val job = launch(start = CoroutineStart.UNDISPATCHED) { c.send() }
        c.setDraft("Overwrite")
        c.send()
        c.selectDay(3)
        assertEquals(1, calls)
        assertEquals(2, c.progress.value.currentDay)
        assertEquals("Question", c.conversation.value.draft)
        assertEquals(1, c.conversation.value.entries.size)
        response.complete(CopilotReply.Message("Answer"))
        job.join()
        c.selectDay(3)
        assertTrue(c.conversation.value.entries.isEmpty())
        c.selectDay(2)
        assertEquals("Answer", c.conversation.value.entries.last().text)
    }

    @Test fun unavailableStoresSystemNoticeWithoutFabricatedReply() = runBlocking {
        val c = controller(service = UnconfiguredCopilot)
        c.setDraft("Help")
        c.send()
        assertEquals(listOf(MessageRole.STUDENT, MessageRole.SYSTEM), c.conversation.value.entries.map { it.role })
        assertEquals(SystemNotice.UNAVAILABLE, c.conversation.value.entries.last().notice)
        assertEquals("Help", c.conversation.value.draft)
    }

    @Test fun exceptionsNeverEnterHistoryAndRetryRemainsPossible() = runBlocking {
        var attempts = 0
        val c = controller(service = CopilotService {
            if (attempts++ == 0) error("private diagnostic") else CopilotReply.Message("Answer")
        })
        c.setDraft("Help")
        c.send()
        assertEquals(SystemNotice.ERROR, c.conversation.value.entries.last().notice)
        assertFalse(c.conversation.value.entries.any { "private" in it.text })
        c.send()
        assertEquals(MessageStatus.RECEIVED, c.conversation.value.status)
    }

    @Test fun cancellationRetainsDraftAndClearsPending() = runBlocking {
        val c = controller(service = CopilotService { CompletableDeferred<CopilotReply>().await() })
        c.setDraft("Help")
        val job = launch(start = CoroutineStart.UNDISPATCHED) { c.send() }
        job.cancelAndJoin()
        assertEquals(MessageStatus.EMPTY, c.conversation.value.status)
        assertEquals(SystemNotice.CANCELLED, c.conversation.value.entries.last().notice)
        assertEquals("Help", c.conversation.value.draft)
        c.selectDay(2)
        assertTrue(c.conversation.value.entries.isEmpty())
    }

    @Test fun contextIsBoundedLessonScopedAndExcludesSystemNotices() = runBlocking {
        val requests = mutableListOf<CopilotRequest>()
        val c = controller(service = CopilotService { requests += it; CopilotReply.Unavailable })
        repeat(16) { c.send("Question $it") }
        assertEquals(12, requests.last().history.size)
        assertTrue(requests.last().history.all { it.role == MessageRole.STUDENT })
        assertEquals("Question 14", requests.last().history.last().text)
        c.selectDay(3)
        c.send("Other lesson")
        assertEquals(3, requests.last().day)
        assertTrue(requests.last().history.isEmpty())
    }

    @Test fun boundedHistoryKeepsLatestEntriesWithoutReusingIds() = runBlocking {
        val c = controller()
        repeat(30) { c.send("Question $it") }
        val entries = c.conversation.value.entries
        assertEquals(40, entries.size)
        assertEquals((21L..60L).toList(), entries.map { it.id })
        assertEquals("Question 10", entries.first().text)
    }

    @Test fun repliesAreBoundedAndBlankRepliesBecomeSystemErrors() = runBlocking {
        val c = controller(service = CopilotService { CopilotReply.Message("a".repeat(9000)) })
        c.send("Help")
        assertEquals(8000, c.conversation.value.entries.last().text.length)
        val blank = controller(service = CopilotService { CopilotReply.Message(" ") })
        blank.send("Help")
        assertEquals(SystemNotice.ERROR, blank.conversation.value.entries.last().notice)
    }

    @Test fun immutableSnapshotsSurviveLaterSends() = runBlocking {
        val c = controller()
        c.send("First")
        val first = c.conversation.value
        c.send("Second")
        assertEquals(2, first.entries.size)
        assertEquals(4, c.conversation.value.entries.size)
    }

    @Test fun controllerRecreationRestoresHistoryAndDraftWithoutSending() = runBlocking {
        val store = MemoryConversationStore()
        val c = controller(store)
        c.send("First")
        c.setDraft("Next")
        val restored = controller(store, CopilotService { error("Must not be called") })
        assertEquals(c.conversation.value.entries, restored.conversation.value.entries)
        assertEquals("Next", restored.conversation.value.draft)
        assertEquals(MessageStatus.EMPTY, restored.conversation.value.status)
    }
}
