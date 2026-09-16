package com.example

import android.app.Application
import com.example.data.GamificationRepository
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.reminder.NotificationHelper
import com.example.reminder.ReminderScheduler

class TodoApplication : Application() {
    val database by lazy { TaskDatabase.getDatabase(this) }
    val reminderScheduler by lazy { ReminderScheduler(this) }
    val gamificationRepository by lazy { GamificationRepository(this) }
    val repository by lazy { TaskRepository(database.taskDao(), reminderScheduler, gamificationRepository) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
