package com.webkurier.android.core

/**
 * WebRTCClient
 *
 * Minimal interface-first WebRTC client wrapper.
 * Real WebRTC implementation will be added later (org.webrtc).
 *
 * Goals for this file now:
 * - define stable API for UI layer
 * - define call states
 * - avoid heavy dependencies until Gradle is set
 *
 * RULES:
 * - WebRTC signaling is handled via PhoneCore (server-side)
 * - Android handles device media + UI states
 * - No business logic
 */
interface WebRTCClient {

    fun getState(): CallState

    fun startOutgoingCall(callId: String, targetUserId: String)
    fun acceptIncomingCall(callId: String)
    fun declineIncomingCall(callId: String)

    fun endCall()

    fun setMicEnabled(enabled: Boolean)
    fun setSpeakerEnabled(enabled: Boolean)

    fun setListener(listener: Listener?)

    interface Listener {
        fun onStateChanged(state: CallState)
        fun onError(error: WebRTCError)
    }
}

/**
 * CallState
 *
 * Client-visible state machine.
 */
sealed class CallState {
    data object Idle : CallState()
    data object Connecting : CallState()
    data object Ringing : CallState()
    data object Connected : CallState()
    data object Reconnecting : CallState()
    data object Ended : CallState()
}

/**
 * WebRTCError
 *
 * Redacted, user-safe error payload.
 */
data class WebRTCError(
    val code: String,
    val message: String,
    val retryable: Boolean = false
)

/**
 * Stub implementation.
 *
 * This keeps the project structured and allows UI development
 * before WebRTC dependency integration.
 */
class WebRTCClientStub : WebRTCClient {

    private var state: CallState = CallState.Idle
    private var listener: WebRTCClient.Listener? = null

    override fun getState(): CallState = state

    override fun startOutgoingCall(callId: String, targetUserId: String) {
        setState(CallState.Connecting)
        // Real implementation will:
        // - connect signaling
        // - create peer connection
        // - negotiate SDP
    }

    override fun acceptIncomingCall(callId: String) {
        setState(CallState.Connecting)
    }

    override fun declineIncomingCall(callId: String) {
        setState(CallState.Ended)
    }

    override fun endCall() {
        setState(CallState.Ended)
    }

    override fun setMicEnabled(enabled: Boolean) {
        // stub
    }

    override fun setSpeakerEnabled(enabled: Boolean) {
        // stub
    }

    override fun setListener(listener: WebRTCClient.Listener?) {
        this.listener = listener
    }

    private fun setState(newState: CallState) {
        state = newState
        listener?.onStateChanged(newState)
    }
}