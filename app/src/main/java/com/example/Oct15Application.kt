package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.example.data.local.Oct15Database
import com.example.data.local.PreferencesDataStore
import com.example.data.remote.FirebaseAuthService
import com.example.data.remote.FirestoreService
import com.example.data.repository.AuthRepository
import com.example.data.repository.MemoryRepository

class Oct15Application : Application() {

    lateinit var database: Oct15Database
        private set
    lateinit var preferencesDataStore: PreferencesDataStore
        private set
    lateinit var memoryRepository: MemoryRepository
        private set
    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Oct15Database.getInstance(this)
        preferencesDataStore = PreferencesDataStore(this)
        val firestoreService = FirestoreService()
        val authService = FirebaseAuthService(this)

        memoryRepository = MemoryRepository(
            memoryDao = database.memoryDao(),
            firestoreService = firestoreService,
            context = this
        )
        authRepository = AuthRepository(
            authService = authService,
            firestoreService = firestoreService,
            preferencesDataStore = preferencesDataStore
        )

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Standard memory anniversary channel
            val standardChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Memory Anniversaries & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when special moments and anniversaries arrive"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(standardChannel)

            // High-priority Call Alert reminder channel with Ringtone
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val callChannel = NotificationChannel(
                CALL_REMINDER_CHANNEL_ID,
                "Memory Call Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Plays alert ringtone on the memory anniversary date"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                setSound(ringtoneUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(callChannel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "oct15_memory_reminders"
        const val CALL_REMINDER_CHANNEL_ID = "oct15_call_reminders"
        lateinit var instance: Oct15Application
            private set
    }
}
