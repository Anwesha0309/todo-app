package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.data.TaskDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        when (intent.action) {
            NotificationHelper.ACTION_REMINDER -> {
                val title = intent.getStringExtra(NotificationHelper.EXTRA_TASK_TITLE) ?: "Task Reminder"
                val category = intent.getStringExtra(NotificationHelper.EXTRA_TASK_CATEGORY) ?: "STUDY"
                val priority = intent.getStringExtra(NotificationHelper.EXTRA_TASK_PRIORITY) ?: "MEDIUM"
                val notes = intent.getStringExtra(NotificationHelper.EXTRA_TASK_NOTES) ?: ""

                NotificationHelper.showTaskReminder(
                    context = context,
                    taskId = taskId,
                    title = title,
                    category = category,
                    priority = priority,
                    notes = notes
                )
            }
            NotificationHelper.ACTION_COMPLETE_TASK -> {
                NotificationManagerCompat.from(context).cancel(taskId.toInt())
                val database = TaskDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    val task = database.taskDao().getTaskById(taskId)
                    if (task != null && !task.isCompleted) {
                        database.taskDao().setTaskCompleted(taskId, true, System.currentTimeMillis())
                        if (task.isRecurring) {
                            val nextInstance = task.computeNextOccurrence()
                            if (nextInstance != null) {
                                val nextId = database.taskDao().insertTask(nextInstance)
                                val scheduler = ReminderScheduler(context)
                                scheduler.scheduleReminder(nextInstance.copy(id = nextId))
                            }
                        }
                    }
                }
            }
        }
    }
}
