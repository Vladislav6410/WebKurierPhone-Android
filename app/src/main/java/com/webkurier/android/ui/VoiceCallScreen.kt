package com.webkurier.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.webkurier.android.core.AppDependencies
import com.webkurier.android.coreRtc.CallState
import com.webkurier.android.coreRtc.WebRTCClient
import com.webkurier.android.coreRtc.WebRTCError
import java.util.UUID

/**
 * VoiceCallScreen
 *
 * Minimal call UI wired to WebRTCClient (currently stub).
 * Real WebRTC + signaling integration will be added later.
 */
@Composable
fun VoiceCallScreen(deps: AppDependencies) {
    val targetUserId = remember { mutableStateOf("") }
    val callId = remember { mutableStateOf(UUID.randomUUID().toString()) }

    val stateText = remember { mutableStateOf(deps.webRtc.getState().toString()) }
    val errorText = remember { mutableStateOf("") }

    val micEnabled = remember { mutableStateOf(true) }
    val speakerEnabled = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val listener = object : WebRTCClient.Listener {
            override fun onStateChanged(state: CallState) {
                stateText.value = state.toString()
            }

            override fun onError(error: WebRTCError) {
                errorText.value = "${error.code}: ${error.message}"
            }
        }

        deps.webRtc.setListener(listener)

        onDispose {
            deps.webRtc.setListener(null)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Voice Call")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = targetUserId.value,
            onValueChange = { targetUserId.value = it },
            label = { Text("Target User ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = callId.value,
            onValueChange = { callId.value = it },
            label = { Text("Call ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                deps.webRtc.startOutgoingCall(
                    callId = callId.value,
                    targetUserId = targetUserId.value
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Call (stub)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { deps.webRtc.endCall() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("End Call")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                micEnabled.value = !micEnabled.value
                deps.webRtc.setMicEnabled(micEnabled.value)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (micEnabled.value) "Mic: ON" else "Mic: OFF")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                speakerEnabled.value = !speakerEnabled.value
                deps.webRtc.setSpeakerEnabled(speakerEnabled.value)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (speakerEnabled.value) "Speaker: ON" else "Speaker: OFF")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "State: ${stateText.value}")
        if (errorText.value.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Error: ${errorText.value}")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Note: Real WebRTC + signaling will be integrated via PhoneCore.")
    }
}