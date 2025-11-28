package com.fireguardedge.app

import android.content.Context
import android.media.RingtoneManager
import android.os.Vibrator
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FusionManager(private val context: Context) {
    private val TAG = "FusionManager"

    // thresholds
    private val VISION_CONF_THRESHOLD = 0.6f
    private val AUDIO_CONF_THRESHOLD = 0.6f
    private val FUSION_SCORE_THRESHOLD = 0.65f

    // hysteresis counters
    private var positiveCount = 0
    private val REQUIRED_CONSECUTIVE = 3

    private val dateFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // logs
    private val logs = mutableListOf<Pair<String,String>>()

    interface Listener {
        fun onStatusUpdate(visionLabel: String, visionConf: Float, audioLabel: String, audioConf: Float)
        fun onDetection(label: String, score: Float)
        fun onLogAdded(entry: Pair<String,String>)
    }

    var listener: Listener? = null

    // label mapping - adjust according to your model labels
    private val visionLabels = arrayOf("normal", "smoke", "fire")
    private val audioLabels = arrayOf("ambient", "fire_crackle")

    fun handleFrameVision(imageBytes: ByteArray, w: Int, h: Int) : Pair<Int,Float> {
        val result = NativeBridge.nativeRunVision(imageBytes, w, h)
        val label = result[0].toInt()
        val conf = result[1]
        return Pair(label, conf)
    }

    fun handleAudio(floatAudio: FloatArray): Pair<Int,Float> {
        val result = NativeBridge.nativeRunAudio(floatAudio, floatAudio.size)
        val label = result[0].toInt()
        val conf = result[1]
        return Pair(label, conf)
    }

    fun evaluate(visionPair: Pair<Int,Float>?, audioPair: Pair<Int,Float>?) {
        val vLabel = visionPair?.first ?: -1
        val vConf = visionPair?.second ?: 0f
        val aLabel = audioPair?.first ?: -1
        val aConf = audioPair?.second ?: 0f

        val vLabelStr = if (vLabel in visionLabels.indices) visionLabels[vLabel] else "unknown"
        val aLabelStr = if (aLabel in audioLabels.indices) audioLabels[aLabel] else "unknown"

        listener?.onStatusUpdate(vLabelStr, vConf, aLabelStr, aConf)

        val visionPositive = (vLabel == 1 || vLabel == 2) && vConf >= VISION_CONF_THRESHOLD
        val audioPositive = (aLabel == 1) && aConf >= AUDIO_CONF_THRESHOLD // audio label 1 = fire_crackle

        val vScore = if (visionPositive) vConf else 0f
        val aScore = if (audioPositive) aConf else 0f
        val score = 0.6f * vScore + 0.4f * aScore

        // logging
        val logMsg = "V:$vLabelStr(${String.format("%.2f",vConf)}) A:$aLabelStr(${String.format("%.2f",aConf)}) S:${String.format("%.2f", score)}"
        addLog(logMsg)

        if (score > FUSION_SCORE_THRESHOLD) positiveCount++ else positiveCount = 0

        if (positiveCount >= REQUIRED_CONSECUTIVE) {
            // detected
            listener?.onDetection("fire", score)
            triggerAlarm()
            positiveCount = 0
        }
    }

    private fun triggerAlarm() {
        Log.w(TAG, "FIRE DETECTED - triggering alarm")
        // vibration
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(500)
        } catch (_: Exception) {}
        // play default alarm sound
        try {
            val alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val r = RingtoneManager.getRingtone(context, alarm)
            r.play()
        } catch (_: Exception) {}
        // show notification
        NotificationUtils.showDetectionNotification(context, "FireGuard Edge — ALERT", "Possible fire detected. Check immediately.")
    }

    private fun addLog(message: String) {
        val ts = dateFmt.format(Date())
        val entry = Pair(message, ts)
        logs.add(0, entry)
        listener?.onLogAdded(entry)
    }

    fun getLogs(): List<Pair<String,String>> = logs
}