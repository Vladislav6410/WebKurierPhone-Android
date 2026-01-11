package com.webkurier.android.coreEndpoints

/**
 * EndpointsModel
 *
 * Non-secret endpoint configuration used by the Android client.
 * Source of truth can be:
 * - app assets (endpoints.json)
 * - build config (later)
 * - remote config (later; via Core)
 */
data class EndpointsModel(
    val environment: String,
    val core: CoreEndpoints,
    val phoneCore: PhoneCoreEndpoints,
    val webrtc: WebRtcEndpoints
) {
    data class CoreEndpoints(
        val restBaseUrl: String,
        val websocketUrl: String
    )

    data class PhoneCoreEndpoints(
        val restBaseUrl: String,
        val websocketUrl: String
    )

    data class WebRtcEndpoints(
        val signalingUrl: String
    )
}

/**
 * EndpointsModelDefaults
 *
 * Safe defaults used when endpoints cannot be loaded.
 * These are placeholders and must be overridden in real environments.
 */
object EndpointsModelDefaults {
    fun default(): EndpointsModel = EndpointsModel(
        environment = "development",
        core = EndpointsModel.CoreEndpoints(
            restBaseUrl = "https://core.webkurier.dev/api",
            websocketUrl = "wss://core.webkurier.dev/ws"
        ),
        phoneCore = EndpointsModel.PhoneCoreEndpoints(
            restBaseUrl = "https://phonecore.webkurier.dev/api",
            websocketUrl = "wss://phonecore.webkurier.dev/ws"
        ),
        webrtc = EndpointsModel.WebRtcEndpoints(
            signalingUrl = "wss://phonecore.webkurier.dev/webrtc"
        )
    )
}