package com.fireguardedge.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : ComponentActivity(), FusionManager.Listener {
    private val TAG = "MainActivity"
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var fusionManager: FusionManager
    private lateinit var logsAdapter: LogsAdapter

    // UI
    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var tvVisionLabel: TextView
    private lateinit var tvVisionConf: TextView
    private lateinit var tvAudioLabel: TextView
    private lateinit var tvAudioConf: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var logsRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        tvVisionLabel = findViewById(R.id.tvVisionLabel)
        tvVisionConf = findViewById(R.id.tvVisionConf)
        tvAudioLabel = findViewById(R.id.tvAudioLabel)
        tvAudioConf = findViewById(R.id.tvAudioConf)
        btnStart = findViewById(R.id.btnStartService)
        btnStop = findViewById(R.id.btnStopService)
        logsRecycler = findViewById(R.id.logsRecycler)

        logsAdapter = LogsAdapter(mutableListOf())
        logsRecycler.adapter = logsAdapter
        logsRecycler.layoutManager = LinearLayoutManager(this)

        fusionManager = FusionManager(this)
        fusionManager.listener = this

        // init native models
        val ok = NativeBridge.nativeInitModels()
        Log.d(TAG, "native init: $ok")

        btnStart.setOnClickListener {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            } else {
                startCameraPreview()
                val serviceIntent = Intent(this, MonitoringService::class.java)
                ContextCompat.startForegroundService(this, serviceIntent)
            }
        }
        btnStop.setOnClickListener {
            stopService(Intent(this, MonitoringService::class.java))
        }
    }

    private fun startCameraPreview() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Analyzer
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analyzer.setAnalyzer(ContextCompat.getMainExecutor(this), CameraAnalyzer { bytes, w, h ->
                Thread {
                    val v = fusionManager.handleFrameVision(bytes, w, h)
                    val a = null // audio handled by AudioCapture/Service
                    fusionManager.evaluate(v, a)
                }.start()
            })

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind failed: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // FusionManager.Listener implementations
    override fun onStatusUpdate(visionLabel: String, visionConf: Float, audioLabel: String, audioConf: Float) {
        runOnUiThread {
            tvVisionLabel.text = visionLabel
            tvVisionConf.text = "Conf: ${String.format("%.2f", visionConf)}"
            tvAudioLabel.text = audioLabel
            tvAudioConf.text = "Conf: ${String.format("%.2f", audioConf)}"
        }
    }

    override fun onDetection(label: String, score: Float) {
        runOnUiThread {
            // Add immediate log
            logsAdapter.add(Pair("DETECTED: $label (score=${String.format("%.2f", score)})", timeNow()))
        }
        // also show a notification (redundant but ensures visibility)
        NotificationUtils.showDetectionNotification(this, "FireGuard Edge — ALERT", "Possible fire detected: $label (score ${String.format("%.2f", score)})")
    }

    override fun onLogAdded(entry: Pair<String, String>) {
        runOnUiThread {
            logsAdapter.add(entry)
        }
    }

    private fun timeNow(): String {
        val t = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return t.format(java.util.Date())
    }

    // permissions
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}