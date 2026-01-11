package com.webkurier.android.coreRtc

/**
 * WebRTCClient
 *
 * Interface-first WebRTC wrapper.
 * Real implementation will be added later (org.webrtc).
 *
 * Goals now:
 * - stable API for UI layer
 * - call state model
 * - stub implementation for early UI development
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

sealed class CallState {
    data object Idle : CallState()
    data object Connecting : CallState()
    data object Ringing : CallState()
    data object Connected : CallState()
    data object Reconnecting : CallState()
    data object Ended : CallState()
}

data class WebRTCError(
    val code: String,
    val message: String,
    val retryable: Boolean = false
)

/**
 * WebRTCClientStub
 *
 * Safe placeholder. Use for UI wiring before WebRTC dependency integration.
 */
class WebRTCClientStub : WebRTCClient {

    private var state: CallState = CallState.Idle
    private var listener: WebRTCClient.Listener? = null

    override fun getState(): CallState = state

    override fun startOutgoingCall(callId: String, targetUserId: String) {
        setState(CallState.Connecting)
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