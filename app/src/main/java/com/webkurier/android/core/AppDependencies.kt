package com.webkurier.android.core

import android.content.Context
import com.webkurier.android.coreEndpoints.EndpointsLoader
import com.webkurier.android.coreEndpoints.EndpointsModel
import com.webkurier.android.coreEndpoints.EndpointsModelDefaults
import com.webkurier.android.coreEndpoints.EndpointSource
import com.webkurier.android.coreEndpoints.LocalAssetEndpointSource
import com.webkurier.android.coreEndpoints.StaticEndpointSource
import com.webkurier.android.coreEndpoints.tryLoadEndpoints
import com.webkurier.android.coreSecure.SecureStore
import com.webkurier.android.coreSecure.SecureTokenProvider
import com.webkurier.android.coreI18n.LocalizationManager
import com.webkurier.android.coreNet.CoreGateway
import com.webkurier.android.coreNet.PhoneCoreAPI
import com.webkurier.android.coreRtc.WebRTCClient
import com.webkurier.android.coreRtc.WebRTCClientStub

/**
 * AppDependencies
 *
 * Single composition root for Android client dependencies.
 *
 * This file intentionally keeps construction simple and explicit.
 * Later we can replace pieces with DI (Hilt/Koin) if needed.
 */
class AppDependencies(context: Context) {

    private val appContext: Context = context.applicationContext

    // --- Local preferences / crypto ---
    val secureStore: SecureStore = SecureStore(appContext)
    val tokenProvider: com.webkurier.android.coreNet.TokenProvider = SecureTokenProvider(secureStore)
    val i18n: LocalizationManager = LocalizationManager(appContext)

    // --- Endpoints ---
    val endpoints: EndpointsModel = tryLoadEndpoints(appContext)

    // --- Network gateways ---
    val coreGateway: CoreGateway = CoreGateway(
        baseUrl = endpoints.core.restBaseUrl,
        tokenProvider = tokenProvider
    )

    val phoneCoreApi: PhoneCoreAPI = PhoneCoreAPI(
        baseUrl = endpoints.phoneCore.restBaseUrl,
        tokenProvider = tokenProvider
    )

    // --- Realtime / WebRTC ---
    val webRtc: WebRTCClient = WebRTCClientStub()
}