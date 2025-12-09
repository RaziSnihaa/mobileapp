package com.example.eyescroll

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnToggleEye: Button
    private lateinit var btnToggleRemote: Button
    private lateinit var tvGaze: TextView
    private lateinit var tvStatus: TextView
    private lateinit var sensitivitySeek: SeekBar
    private lateinit var tvSensitivity: TextView

    private var eyeTrackingEnabled = false
    private var remoteEnabled = false

    private var eyeTracker: EyeTracker? = null

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startEyeTrackingIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnToggleEye = findViewById(R.id.btn_toggle_eye)
        btnToggleRemote = findViewById(R.id.btn_toggle_remote)
        tvGaze = findViewById(R.id.tv_gaze)
        tvStatus = findViewById(R.id.tv_status)
        sensitivitySeek = findViewById(R.id.sensitivity_seek)
        tvSensitivity = findViewById(R.id.tv_sensitivity)

        btnToggleEye.setOnClickListener {
            eyeTrackingEnabled = !eyeTrackingEnabled
            btnToggleEye.text = if (eyeTrackingEnabled) "Désactiver Eye-tracking" else "Activer Eye-tracking"
            if (eyeTrackingEnabled) {
                ensureCameraPermission()
            } else {
                stopEyeTracking()
            }
        }

        btnToggleRemote.setOnClickListener {
            remoteEnabled = !remoteEnabled
            btnToggleRemote.text = if (remoteEnabled) "Désactiver Contrôle à distance" else "Activer Contrôle à distance"
            if (remoteEnabled) {
                startService(Intent(this, RemoteControlService::class.java))
                tvStatus.text = "Status: remote enabled"
            } else {
                stopService(Intent(this, RemoteControlService::class.java))
                tvStatus.text = "Status: idle"
            }
        }

        sensitivitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSensitivity.text = "Sensibilité: $progress"
                SensitivitySettings.setSensitivity(this@MainActivity, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // initialize EyeTracker
        eyeTracker = EyeTracker(this) { gaze ->
            // gaze: "UP" | "DOWN" | "CENTER"
            runOnUiThread {
                tvGaze.text = "Gaze: $gaze"
                // trigger scroll via AccessibilityService
                val svc = ScrollAccessibilityService.currentInstance
                if (svc != null && gaze != "CENTER") {
                    if (gaze == "UP") svc.performScroll(ScrollAccessibilityService.Direction.UP)
                    else if (gaze == "DOWN") svc.performScroll(ScrollAccessibilityService.Direction.DOWN)
                }
            }
        }

        // show accessibility settings hint
        tvStatus.text = "Status: please enable Accessibility service `EyeScroll` in Settings -> Accessibility"
    }

    private fun ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startEyeTrackingIfNeeded()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startEyeTrackingIfNeeded() {
        eyeTracker?.start()
        tvStatus.text = "Status: eye-tracking enabled"
    }

    private fun stopEyeTracking() {
        eyeTracker?.stop()
        tvStatus.text = "Status: idle"
    }

    override fun onDestroy() {
        super.onDestroy()
        eyeTracker?.stop()
    }
}
