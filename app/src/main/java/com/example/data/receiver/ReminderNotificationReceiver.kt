package com.example.data.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.Oct15Application
import com.example.R

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val memoryId = intent.getStringExtra("memory_id") ?: ""
        val title = intent.getStringExtra("memory_title") ?: "Special Moment"
        val category = intent.getStringExtra("memory_category") ?: "Memory"
        val isCallAlert = intent.getBooleanExtra("is_call_alert", false)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_memory_id", memoryId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            memoryId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (isCallAlert) Oct15Application.CALL_REMINDER_CHANNEL_ID
        else Oct15Application.REMINDER_CHANNEL_ID

        val defaultSoundUri = if (isCallAlert) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (isCallAlert) "📞 OCT15 Call Reminder: $title" else "OCT15 — Anniversary Today ✨")
            .setContentText("An OCT15 memory is waiting for you: $title")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("An OCT15 memory is waiting for you: \"$title\". Relive this special moment from your life.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (isCallAlert) NotificationCompat.CATEGORY_CALL else NotificationCompat.CATEGORY_REMINDER)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(memoryId.hashCode().coerceAtLeast(1), notification)
    }

    companion object {
        fun sendImmediateTestNotification(context: Context, isCallAlert: Boolean = false) {
            val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
                putExtra("memory_id", "test_memory_${System.currentTimeMillis()}")
                putExtra("memory_title", if (isCallAlert) "First Love Anniversary (Call Alert)" else "First Love ❤️")
                putExtra("memory_category", "Love")
                putExtra("is_call_alert", isCallAlert)
            }
            context.sendBroadcast(intent)
        }
    }
}
