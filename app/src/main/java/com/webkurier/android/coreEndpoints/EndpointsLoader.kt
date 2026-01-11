package com.webkurier.android.coreEndpoints

import android.content.Context
import org.json.JSONObject

/**
 * EndpointsLoader
 *
 * Loads endpoints configuration from:
 * - assets/endpoints.json  (preferred, non-secret)
 * - defaults (fallback)
 *
 * NOTE:
 * - No secrets are stored here.
 * - In production, Hybrid/CI can package environment-specific assets.
 */
object EndpointsLoader {

    private const val ASSET_FILE = "endpoints.json"

    fun load(context: Context): EndpointsModel {
        return try {
            val jsonText = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
            parse(jsonText)
        } catch (_: Throwable) {
            EndpointsModelDefaults.default()
        }
    }

    private fun parse(jsonText: String): EndpointsModel {
        val root = JSONObject(jsonText)

        val env = root.optString("environment", "development")

        val core = root.getJSONObject("core")
        val phoneCore = root.getJSONObject("phoneCore")
        val webrtc = root.getJSONObject("webrtc")

        return EndpointsModel(
            environment = env,
            core = EndpointsModel.CoreEndpoints(
                restBaseUrl = core.getString("restBaseUrl"),
                websocketUrl = core.getString("websocketUrl")
            ),
            phoneCore = EndpointsModel.PhoneCoreEndpoints(
                restBaseUrl = phoneCore.getString("restBaseUrl"),
                websocketUrl = phoneCore.getString("websocketUrl")
            ),
            webrtc = EndpointsModel.WebRtcEndpoints(
                signalingUrl = webrtc.getString("signalingUrl")
            )
        )
    }
}

/**
 * Convenience wrapper used by AppDependencies.
 */
fun tryLoadEndpoints(context: Context): EndpointsModel = EndpointsLoader.load(context)