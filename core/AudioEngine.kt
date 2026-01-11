package com.webkurier.android.core

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AudioEngine
 *
 * Minimal audio capture engine designed for:
 * - STT streaming / chunked upload
 * - WebRTC microphone source integration (later)
 *
 * RULES:
 * - Keep latency low
 * - Never log raw audio
 * - Must stop cleanly (avoid mic lock)
 */
class AudioEngine {

    data class Config(
        val sampleRateHz: Int = 16000,
        val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
        val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT,
        val bufferSizeFactor: Int = 2
    )

    private var recorder: AudioRecord? = null
    private var captureJob: Job? = null
    private var scope: CoroutineScope? = null

    private val isRunning = AtomicBoolean(false)

    /**
     * Starts microphone capture and invokes onPcmChunk with raw PCM bytes.
     *
     * onPcmChunk will be called on a background thread.
     */
    fun startCapture(
        config: Config = Config(),
        onPcmChunk: (ByteArray) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (isRunning.getAndSet(true)) return

        try {
            val minBuffer = AudioRecord.getMinBufferSize(
                config.sampleRateHz,
                config.channelConfig,
                config.audioFormat
            )

            val bufferSize = (minBuffer * config.bufferSizeFactor).coerceAtLeast(minBuffer)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                config.sampleRateHz,
                config.channelConfig,
                config.audioFormat,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                isRunning.set(false)
                onError(IllegalStateException("AudioRecord not initialized"))
                return
            }

            recorder = audioRecord
            scope = CoroutineScope(Dispatchers.IO)
            audioRecord.startRecording()

            captureJob = scope?.launch {
                val buffer = ByteArray(bufferSize)
                while (isRunning.get()) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        // Copy only the read segment to avoid leaking unused bytes
                        onPcmChunk(buffer.copyOfRange(0, read))
                    } else if (read < 0) {
                        onError(RuntimeException("AudioRecord read error: $read"))
                        stopCapture()
                        break
                    }
                }
            }
        } catch (t: Throwable) {
            isRunning.set(false)
            onError(t)
        }
    }

    /**
     * Stops capture and releases microphone.
     */
    fun stopCapture() {
        if (!isRunning.getAndSet(false)) return

        try {
            captureJob?.cancel()
            captureJob = null

            scope?.cancel()
            scope = null

            recorder?.apply {
                try { stop() } catch (_: Throwable) { /* ignore */ }
                release()
            }
            recorder = null
        } catch (_: Throwable) {
            // swallow; stop must be safe
        }
    }
}