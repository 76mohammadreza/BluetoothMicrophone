package com.example.bluetoothmicrophone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder

class AudioStreamer(private val context: Context) {
    enum class StartResult { STARTED, ALREADY_RUNNING, NO_SPEAKER, NO_PERMISSION, FAILED }

    private val lock = Any()
    @Volatile private var running = false
    private var worker: Thread? = null
    private var recorder: AudioRecord? = null

    @SuppressLint("MissingPermission")
    fun start(onError: (String) -> Unit): StartResult {
        synchronized(lock) {
            if (running || worker?.isAlive == true) return StartResult.ALREADY_RUNNING
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return StartResult.NO_PERMISSION
            }
            val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val speaker = manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            } ?: return StartResult.NO_SPEAKER

            val rate = 44100
            val minInput = AudioRecord.getMinBufferSize(
                rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val minOutput = AudioTrack.getMinBufferSize(
                rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minInput <= 0 || minOutput <= 0) return StartResult.FAILED

            var input: AudioRecord? = null
            var output: AudioTrack? = null
            try {
                input = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    rate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minInput * 2
                )
                if (input.state != AudioRecord.STATE_INITIALIZED) return StartResult.FAILED
                output = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(rate)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minOutput * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                if (output.state != AudioTrack.STATE_INITIALIZED) return StartResult.FAILED
                if (!output.setPreferredDevice(speaker)) return StartResult.FAILED

                val activeInput = requireNotNull(input)
                val activeOutput = requireNotNull(output)
                recorder = activeInput
                running = true
                worker = Thread({
                    var failure: String? = null
                    try {
                        activeInput.startRecording()
                        activeOutput.play()
                        val buffer = ByteArray(maxOf(minInput, 2048))
                        while (running && !Thread.currentThread().isInterrupted) {
                            val count = activeInput.read(buffer, 0, buffer.size)
                            if (count < 0) {
                                if (running) failure = "خطا در دریافت صدای میکروفون"
                                break
                            }
                            var sent = 0
                            while (running && sent < count) {
                                val written = activeOutput.write(buffer, sent, count - sent)
                                if (written <= 0) {
                                    failure = "خطا در پخش صدا"
                                    break
                                }
                                sent += written
                            }
                            if (failure != null) break
                        }
                    } catch (e: Exception) {
                        if (running) failure = e.localizedMessage ?: "خطای انتقال صدا"
                    } finally {
                        running = false
                        try { activeInput.stop() } catch (_: Exception) {}
                        try { activeOutput.pause() } catch (_: Exception) {}
                        try { activeOutput.flush() } catch (_: Exception) {}
                        activeInput.release()
                        activeOutput.release()
                        synchronized(lock) {
                            if (recorder === activeInput) recorder = null
                            worker = null
                        }
                        if (failure != null) onError(failure)
                    }
                }, "BluetoothMicrophoneAudio").also { it.start() }
                // Worker now owns both objects and releases them.
                input = null
                output = null
                return StartResult.STARTED
            } catch (_: Exception) {
                return StartResult.FAILED
            } finally {
                input?.release()
                output?.release()
            }
        }
    }

    fun stop() {
        running = false
        // stop() unblocks any active AudioRecord.read() so the worker can exit.
        try { recorder?.stop() } catch (_: Exception) {}
    }
}
