package com.webkurier.android.pilot

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PilotControllerTest {
    private class MemoryProgressStore(var saved: CourseProgress = CourseProgress()) : CourseProgressStore {
        override fun load() = saved
        override fun save(progress: CourseProgress) { saved = progress }
    }

    private fun controller(
        store: CourseProgressStore = MemoryProgressStore(),
        github: GitHubAuthService = UnconfiguredGitHubAuth,
        copilot: CopilotService = UnconfiguredCopilot
    ) = PilotController(store, github, copilot)

    @Test fun progressRestoresWithoutInventingAuthentication() {
        val store = MemoryProgressStore()
        val first = controller(store)
        first.selectDay(3)
        first.completeDay()
        val restored = controller(store)
        assertEquals(CourseProgress(3, setOf(3)), restored.progress.value)
        assertEquals(ConnectionStatus.NOT_CONNECTED, restored.connection.value.status)
        assertNull(restored.connection.value.identity)
    }

    @Test fun invalidLessonNavigationKeepsCurrentContext() {
        val controller = controller()
        controller.selectDay(2)
        controller.selectDay(8)
        assertEquals(2, controller.progress.value.currentDay)
    }

    @Test fun unconfiguredGitHubReturnsExplicitErrorWithoutIdentity() = runBlocking {
        val controller = controller()
        controller.connect()
        assertEquals(ConnectionStatus.ERROR, controller.connection.value.status)
        assertEquals(ConnectionError.NOT_CONFIGURED, controller.connection.value.error)
        assertNull(controller.connection.value.identity)
    }

    @Test fun connectingIsObservableAndRepeatedClicksDoNotStartAnotherRequest() = runBlocking {
        val response = CompletableDeferred<GitHubAuthResult>()
        var calls = 0
        val controller = controller(github = GitHubAuthService { calls++; response.await() })
        val job = launch(start = CoroutineStart.UNDISPATCHED) { controller.connect() }
        assertEquals(ConnectionStatus.CONNECTING, controller.connection.value.status)
        controller.connect()
        assertEquals(1, calls)
        val identity = GitHubIdentity("student", StudentProject("class/first-page"))
        response.complete(GitHubAuthResult.Connected(identity))
        job.join()
        assertEquals(GitHubConnection(ConnectionStatus.CONNECTED, identity), controller.connection.value)
        controller.connect()
        assertEquals(1, calls)
    }

    @Test fun failedConnectionCanBeRetriedAndDoesNotExposeException() = runBlocking {
        var calls = 0
        val controller = controller(github = GitHubAuthService {
            if (calls++ == 0) error("private diagnostic")
            GitHubAuthResult.Connected(GitHubIdentity("student"))
        })
        controller.connect()
        assertEquals(ConnectionError.FAILED, controller.connection.value.error)
        controller.connect()
        assertEquals(ConnectionStatus.CONNECTED, controller.connection.value.status)
        assertNull(controller.connection.value.identity?.project)
    }

    @Test fun cancelledConnectionResetsAndPropagatesCancellation() = runBlocking {
        val response = CompletableDeferred<GitHubAuthResult>()
        val controller = controller(github = GitHubAuthService { response.await() })
        val job = launch(start = CoroutineStart.UNDISPATCHED) { controller.connect() }
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        assertEquals(GitHubConnection(), controller.connection.value)
    }

    @Test fun unconfiguredCopilotNeverPretendsToChangeProject() = runBlocking {
        val controller = controller()
        controller.send("Добавь кнопку")
        assertEquals(Conversation(MessageStatus.UNAVAILABLE), controller.conversation.value)
        assertTrue(controller.progress.value.completedDays.isEmpty())
    }

    @Test fun blankAndOversizedMessagesAreNotSent() = runBlocking {
        var calls = 0
        val controller = controller(copilot = CopilotService { calls++; CopilotReply.Unavailable })
        controller.send("   ")
        controller.send("x".repeat(PilotController.MAX_MESSAGE_LENGTH + 1))
        assertEquals(0, calls)
        assertEquals(Conversation(), controller.conversation.value)
    }

    @Test fun exactLengthLimitIsAcceptedAndMissingProjectStaysAbsent() = runBlocking {
        var sent: CopilotRequest? = null
        val controller = controller(copilot = CopilotService { sent = it; CopilotReply.Unavailable })
        controller.send("x".repeat(PilotController.MAX_MESSAGE_LENGTH))
        assertEquals(PilotController.MAX_MESSAGE_LENGTH, sent?.message?.length)
        assertNull(sent?.project)
    }

    @Test fun requestUsesSelectedLessonAndVerifiedProjectAndGuardsConcurrentChanges() = runBlocking {
        val response = CompletableDeferred<CopilotReply>()
        val project = StudentProject("class/first-page")
        val requests = mutableListOf<CopilotRequest>()
        val controller = controller(
            github = GitHubAuthService { GitHubAuthResult.Connected(GitHubIdentity("student", project)) },
            copilot = CopilotService { requests += it; response.await() }
        )
        controller.connect()
        controller.selectDay(2)
        val job = launch(start = CoroutineStart.UNDISPATCHED) { controller.send(" Add text ") }
        controller.send("Duplicate")
        controller.selectDay(3)
        assertEquals(MessageStatus.SENDING, controller.conversation.value.status)
        assertEquals(2, controller.progress.value.currentDay)
        assertEquals(listOf(CopilotRequest(2, "Add text", project)), requests)
        response.complete(CopilotReply.Message("Verified reply"))
        job.join()
        assertEquals(Conversation(MessageStatus.RECEIVED, "Verified reply"), controller.conversation.value)
        controller.selectDay(3)
        assertEquals(Conversation(), controller.conversation.value)
    }

    @Test fun failedCopilotRequestShowsSafeError() = runBlocking {
        val controller = controller(copilot = CopilotService { error("private diagnostic") })
        controller.send("Help")
        assertEquals(Conversation(MessageStatus.ERROR), controller.conversation.value)
    }

    @Test fun cancelledCopilotRequestDoesNotStaySending() = runBlocking {
        val response = CompletableDeferred<CopilotReply>()
        val controller = controller(copilot = CopilotService { response.await() })
        val job = launch(start = CoroutineStart.UNDISPATCHED) { controller.send("Help") }
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        assertEquals(Conversation(), controller.conversation.value)
    }
}
