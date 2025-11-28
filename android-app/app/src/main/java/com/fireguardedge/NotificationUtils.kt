package com.fireguardedge.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationUtils {
    const val CHANNEL_ID_MONITOR = "fireguard_monitor_channel"
    const val CHANNEL_ID_ALERT = "fireguard_alert_channel"
    const val ALERT_NOTIFICATION_ID = 202

    fun createAlertChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID_ALERT, "FireGuard Alerts", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Alerts when fire is detected"
            nm.createNotificationChannel(channel)
        }
    }

    fun showDetectionNotification(ctx: Context, title: String, text: String) {
        createAlertChannel(ctx)
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val tone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID_ALERT)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(tone)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()
        nm.notify(ALERT_NOTIFICATION_ID, notif)
    }
}