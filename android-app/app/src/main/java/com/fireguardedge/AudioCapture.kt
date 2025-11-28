package com.fireguardedge.app

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread

class AudioCapture(private val onAudioReady: (FloatArray) -> Unit) {
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    private var recorder: AudioRecord? = null
    private var isRunning = false

    fun start() {
        if (isRunning) return
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE)
        recorder?.startRecording()
        isRunning = true
        thread {
            val readBuffer = ShortArray(SAMPLE_RATE) // 1 second buffer
            while (isRunning) {
                var total = 0
                while (total < readBuffer.size) {
                    val read = recorder?.read(readBuffer, total, readBuffer.size - total) ?: 0
                    total += read
                }
                // Convert to float normalized -1..1
                val floatBuffer = FloatArray(readBuffer.size)
                for (i in readBuffer.indices) {
                    floatBuffer[i] = readBuffer[i] / 32768.0f
                }
                // callback on main thread
                Handler(Looper.getMainLooper()).post {
                    onAudioReady(floatBuffer)
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}