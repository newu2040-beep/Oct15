package com.example.data.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.Memory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderScheduler {

    fun scheduleReminder(context: Context, memory: Memory) {
        if (!memory.reminderEnabled) {
            cancelReminder(context, memory.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            val triggerMillis = calculateNextTriggerMillis(memory) ?: return

            val intent = Intent(context, ReminderNotificationReceiver::class.java).apply {
                putExtra("memory_id", memory.id)
                putExtra("memory_title", memory.title)
                putExtra("memory_date", memory.date)
                putExtra("memory_category", memory.category)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                memory.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
            Log.d("ReminderScheduler", "Scheduled reminder for ${memory.title} at epoch $triggerMillis")
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to schedule reminder", e)
        }
    }

    fun cancelReminder(context: Context, memoryId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            memoryId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun calculateNextTriggerMillis(memory: Memory): Long? {
        val parsedDate = memory.parseLocalDate() ?: return null
        val timeParts = memory.reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        val reminderLocalTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))

        val now = LocalDateTime.now()
        val currentYear = now.year

        // Determine target date
        val anniversaryDate = if (memory.yearlyAnniversary) {
            var target = memory.anniversaryInYear(currentYear) ?: parsedDate
            val triggerDateTime = LocalDateTime.of(target, reminderLocalTime).minusDays(memory.advanceReminderDays.toLong())
            if (triggerDateTime.isBefore(now)) {
                target = memory.anniversaryInYear(currentYear + 1) ?: parsedDate
            }
            target
        } else {
            parsedDate
        }

        val finalTriggerDateTime = LocalDateTime.of(anniversaryDate, reminderLocalTime)
            .minusDays(memory.advanceReminderDays.toLong())

        val zonedDateTime = finalTriggerDateTime.atZone(ZoneId.systemDefault())
        return zonedDateTime.toInstant().toEpochMilli()
    }
}
