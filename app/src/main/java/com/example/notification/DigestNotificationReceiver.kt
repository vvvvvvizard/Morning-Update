package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.util.Calendar

class DigestNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("DigestNotification", "Alarm triggered for Morning Digest.")
        showDigestNotification(context)
        // Reschedule for next day!
        scheduleDailyNotification(context)
    }

    private fun showDigestNotification(context: Context) {
        val channelId = "morning_digest_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Morning Digest",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Delivers your compiled morning AI news digest & email summary at 7:00 AM."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Good Morning! Your AI Digest is ready ☀️")
            .setContentText("Headlines, executive summaries, sentiments, and new emails since yesterday are updated.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(700, notification)
    }

    companion object {
        fun scheduleDailyNotification(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DigestNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }

                if (canScheduleExact) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                    Log.d("DigestNotification", "Succesfully scheduled daily 7:00 AM exact alarm.")
                } else {
                    Log.d("DigestNotification", "Exact alarms permission not granted. Falling back to inexact alarm.")
                    fallbackToInexactAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                Log.e("DigestNotification", "Failed to schedule exact alarm due to permission constraints. Falling back to inexact alarm.", e)
                fallbackToInexactAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
            }
        }

        private fun fallbackToInexactAlarm(
            alarmManager: AlarmManager,
            triggerAtMillis: Long,
            operation: PendingIntent
        ) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        operation
                    )
                }
                Log.d("DigestNotification", "Successfully scheduled fallback inexact alarm.")
            } catch (e: Exception) {
                Log.e("DigestNotification", "Failed to schedule fallback inexact alarm.", e)
            }
        }
    }
}
