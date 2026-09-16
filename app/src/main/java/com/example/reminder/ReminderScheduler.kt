package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.TaskItem

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(task: TaskItem) {
        if (!task.hasReminder || task.isCompleted) {
            cancelReminder(task.id)
            return
        }

        // Calculate reminder trigger time
        val targetMillis = task.exactDueTimeMillis - (task.reminderOffsetMinutes * 60 * 1000L)
        val now = System.currentTimeMillis()

        if (targetMillis <= now) {
            // Already in the past
            cancelReminder(task.id)
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = NotificationHelper.ACTION_REMINDER
            putExtra(NotificationHelper.EXTRA_TASK_ID, task.id)
            putExtra(NotificationHelper.EXTRA_TASK_TITLE, task.title)
            putExtra(NotificationHelper.EXTRA_TASK_CATEGORY, task.category)
            putExtra(NotificationHelper.EXTRA_TASK_PRIORITY, task.priority)
            putExtra(NotificationHelper.EXTRA_TASK_NOTES, task.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w("ReminderScheduler", "Could not schedule exact alarm: ${e.message}")
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e("ReminderScheduler", "Failed to schedule alarm fallback", ex)
            }
        }
    }

    fun cancelReminder(taskId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = NotificationHelper.ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
