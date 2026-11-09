package com.example.bluetoothmicrophone

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var streamer: AudioStreamer
    private lateinit var status: TextView
    private lateinit var start: Button
    private lateinit var stop: Button
    private val microphoneRequest = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        streamer = AudioStreamer(this)
        status = findViewById(R.id.statusText)
        start = findViewById(R.id.startButton)
        stop = findViewById(R.id.stopButton)
        start.setOnClickListener { requestAndStart() }
        stop.setOnClickListener { stopStreaming() }
    }

    private fun requestAndStart() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), microphoneRequest)
            return
        }
        val result = streamer.start { error ->
            runOnUiThread {
                status.text = error
                start.isEnabled = true
                stop.isEnabled = false
            }
        }
        when (result) {
            AudioStreamer.StartResult.STARTED -> {
                status.setText(R.string.status_live)
                start.isEnabled = false
                stop.isEnabled = true
            }
            AudioStreamer.StartResult.NO_SPEAKER -> status.setText(R.string.status_missing)
            AudioStreamer.StartResult.NO_PERMISSION -> status.setText(R.string.status_permission)
            AudioStreamer.StartResult.FAILED -> status.text = "خطا در راه‌اندازی صدا"
            AudioStreamer.StartResult.ALREADY_RUNNING -> Unit
        }
    }

    private fun stopStreaming() {
        streamer.stop()
        status.setText(R.string.status_stopped)
        start.isEnabled = true
        stop.isEnabled = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == microphoneRequest) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                requestAndStart()
            } else {
                status.setText(R.string.status_permission)
            }
        }
    }

    override fun onStop() {
        // Keep mic access limited to the visible activity; no foreground service.
        stopStreaming()
        super.onStop()
    }
}
