package com.fireguardedge.app

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

class MonitoringService : Service() {

    private val TAG = "MonitoringService"
    private lateinit var fusionManager: FusionManager
    private lateinit var audioCapture: AudioCapture

    private var latestVision: Pair<Int, Float>? = null
    private var latestAudio: Pair<Int, Float>? = null

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // Camera executor
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "MonitoringService created")

        fusionManager = FusionManager(this)

        // Init native model
        NativeBridge.nativeInitModels()

        createNotificationChannel()
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (!hasPermissions()) {
            Log.w(TAG, "Permissions missing — cannot start monitoring.")
            stopSelf()
            return START_NOT_STICKY
        }

        startCamera()
        startAudio()

        Log.i(TAG, "MonitoringService is running in foreground")
        return START_STICKY
    }

    // ---------------------------------------------------------------------------------------------
    // CAMERA PROCESSING
    // ---------------------------------------------------------------------------------------------
    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(cameraExecutor, CameraAnalyzer { bytes, w, h ->
                val r = fusionManager.handleFrameVision(bytes, w, h)
                latestVision = r
                fusionManager.evaluate(latestVision, latestAudio)
            })

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    FakeLifecycleOwner(),            // We run this service without activity UI
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    analyzer
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // ---------------------------------------------------------------------------------------------
    // AUDIO PROCESSING
    // ---------------------------------------------------------------------------------------------
    private fun startAudio() {
        audioCapture = AudioCapture { buffer ->
            val res = fusionManager.handleAudio(buffer)
            latestAudio = res
            fusionManager.evaluate(latestVision, latestAudio)
        }
        audioCapture.start()
    }

    // ---------------------------------------------------------------------------------------------
    // FOREGROUND NOTIFICATION
    // ---------------------------------------------------------------------------------------------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fireguard_monitor_channel",
                "FireGuard Edge Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = Notification.Builder(this, "fireguard_monitor_channel")
            .setContentTitle("FireGuard Edge — Monitoring")
            .setContentText("Fire & smoke detection running…")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(101, notification)
    }

    // ---------------------------------------------------------------------------------------------
    // PERMISSIONS
    // ---------------------------------------------------------------------------------------------
    private fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
    }

    // ---------------------------------------------------------------------------------------------
    // SERVICE EVENTS
    // ---------------------------------------------------------------------------------------------
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            audioCapture.stop()
        } catch (_: Exception) {}
        Log.i(TAG, "MonitoringService destroyed")
    }

    // ---------------------------------------------------------------------------------------------
    // Fake LifecycleOwner for CameraX (since this is a Service, not Activity)
    // ---------------------------------------------------------------------------------------------
    inner class FakeLifecycleOwner : androidx.lifecycle.LifecycleOwner {
        private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
        init {
            lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.RESUMED
        }
        override fun getLifecycle(): androidx.lifecycle.Lifecycle = lifecycleRegistry
    }
}