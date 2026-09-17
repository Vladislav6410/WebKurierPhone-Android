package com.webkurier.android.pilot

sealed interface GitHubAuthResult {
    data class Connected(val identity: GitHubIdentity) : GitHubAuthResult
    data class Unavailable(val reason: ConnectionError) : GitHubAuthResult
}

/** Future adapter must use the verified WebKurier session/OAuth contract. */
fun interface GitHubAuthService {
    suspend fun connect(): GitHubAuthResult
}

object UnconfiguredGitHubAuth : GitHubAuthService {
    override suspend fun connect() = GitHubAuthResult.Unavailable(ConnectionError.NOT_CONFIGURED)
}

data class CopilotRequest(val day: Int, val message: String, val project: StudentProject?)
sealed interface CopilotReply {
    data object Unavailable : CopilotReply
    data class Message(val text: String) : CopilotReply
}

/** Client boundary only; neither an invented HTTP contract nor a provider SDK. */
fun interface CopilotService {
    suspend fun send(request: CopilotRequest): CopilotReply
}

object UnconfiguredCopilot : CopilotService {
    override suspend fun send(request: CopilotRequest) = CopilotReply.Unavailable
}

interface CourseProgressStore {
    fun load(): CourseProgress
    fun save(progress: CourseProgress)
}
