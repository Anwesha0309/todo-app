package com.example.data

import android.content.Context
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskRepository(
    private val taskDao: TaskDao,
    private val reminderScheduler: ReminderScheduler,
    val gamificationRepository: GamificationRepository
) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskItem>> = taskDao.getPendingTasks()

    fun getTasksForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<TaskItem>> {
        return taskDao.getTasksForDateRange(startOfDay, endOfDay)
    }

    suspend fun getTaskById(id: Long): TaskItem? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: TaskItem): Long {
        val id = taskDao.insertTask(task)
        val created = task.copy(id = id)
        reminderScheduler.scheduleReminder(created)
        return id
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
        if (task.isCompleted || !task.hasReminder) {
            reminderScheduler.cancelReminder(task.id)
        } else {
            reminderScheduler.scheduleReminder(task)
        }
    }

    suspend fun deleteTask(task: TaskItem) {
        reminderScheduler.cancelReminder(task.id)
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        reminderScheduler.cancelReminder(id)
        taskDao.deleteTaskById(id)
    }

    /**
     * Toggles task completion state.
     * When completed:
     * 1. Updates task in database and cancels pending reminder
     * 2. Triggers gamification XP & streak calculation
     * 3. If recurring, automatically generates and inserts the next instance!
     */
    suspend fun toggleTaskCompleted(task: TaskItem) {
        val newCompleted = !task.isCompleted
        val completedAt = if (newCompleted) System.currentTimeMillis() else null
        val updated = task.copy(isCompleted = newCompleted, completedAtMillis = completedAt)
        taskDao.updateTask(updated)

        if (newCompleted) {
            reminderScheduler.cancelReminder(task.id)
            // Gamification XP & Streak reward
            gamificationRepository.onTaskCompleted(task)

            // Auto-generate next instance for recurring task
            if (task.isRecurring) {
                val nextInstance = task.computeNextOccurrence()
                if (nextInstance != null) {
                    insertTask(nextInstance)
                }
            }
        } else if (task.hasReminder) {
            reminderScheduler.scheduleReminder(updated)
        }
    }

    suspend fun toggleSubtask(task: TaskItem, subtaskId: String) {
        var didCompleteSubtask = false
        val updatedSubtasks = task.subtasks.map { subtask ->
            if (subtask.id == subtaskId) {
                val newDone = !subtask.isDone
                if (newDone) didCompleteSubtask = true
                subtask.copy(isDone = newDone)
            } else {
                subtask
            }
        }
        val updatedTask = task.copyWithSubtasks(updatedSubtasks)
        taskDao.updateTask(updatedTask)

        if (didCompleteSubtask) {
            gamificationRepository.onSubtaskCompleted()
        }
    }

    suspend fun seedSampleTasksIfEmpty() {
        val current = taskDao.getAllTasks().first()
        if (current.isEmpty()) {
            val cal = Calendar.getInstance()
            // Reset to start of today
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val todayStart = cal.timeInMillis

            // Tomorrow
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowStart = cal.timeInMillis

            // Day after tomorrow
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayAfterTomorrowStart = cal.timeInMillis

            val sampleTasks = listOf(
                TaskItem(
                    title = "Submit Research Proposal Draft",
                    description = "Include literature review and methodology sections for Professor Davis.",
                    category = TaskCategory.STUDY.name,
                    priority = TaskPriority.URGENT.name,
                    dueDateMillis = todayStart,
                    dueTimeHour = 16,
                    dueTimeMinute = 30,
                    estimatedMinutes = 90,
                    hasReminder = true,
                    reminderOffsetMinutes = 30,
                    recurrence = RecurrenceType.NONE.name,
                    subtasksRaw = TaskItem.encodeSubtasks(
                        listOf(
                            Subtask(title = "Proofread abstract & introduction", isDone = true),
                            Subtask(title = "Format citations in APA style", isDone = false),
                            Subtask(title = "Export PDF & upload to portal", isDone = false)
                        )
                    )
                ),
                TaskItem(
                    title = "Quarterly Sprint Review & Roadmap",
                    description = "Present progress metrics and Q3 deliverables with team leads.",
                    category = TaskCategory.WORK.name,
                    priority = TaskPriority.HIGH.name,
                    dueDateMillis = todayStart,
                    dueTimeHour = 11,
                    dueTimeMinute = 0,
                    estimatedMinutes = 60,
                    hasReminder = true,
                    reminderOffsetMinutes = 15,
                    recurrence = RecurrenceType.WEEKLY.name,
                    subtasksRaw = TaskItem.encodeSubtasks(
                        listOf(
                            Subtask(title = "Review slide deck", isDone = true),
                            Subtask(title = "Compile sprint velocity chart", isDone = false)
                        )
                    )
                ),
                TaskItem(
                    title = "Calculus Problem Set #4",
                    description = "Complete exercises 12-25 on multivariable integration.",
                    category = TaskCategory.STUDY.name,
                    priority = TaskPriority.MEDIUM.name,
                    dueDateMillis = tomorrowStart,
                    dueTimeHour = 14,
                    dueTimeMinute = 0,
                    estimatedMinutes = 120,
                    hasReminder = true,
                    reminderOffsetMinutes = 60,
                    recurrence = RecurrenceType.NONE.name,
                    subtasksRaw = TaskItem.encodeSubtasks(
                        listOf(
                            Subtask(title = "Problems 12-18", isDone = false),
                            Subtask(title = "Problems 19-25", isDone = false)
                        )
                    )
                ),
                TaskItem(
                    title = "Client Deliverable Architecture Review",
                    description = "Sync with DevOps on database migration and API gateway security.",
                    category = TaskCategory.WORK.name,
                    priority = TaskPriority.HIGH.name,
                    dueDateMillis = dayAfterTomorrowStart,
                    dueTimeHour = 10,
                    dueTimeMinute = 30,
                    estimatedMinutes = 45,
                    hasReminder = true,
                    reminderOffsetMinutes = 15,
                    recurrence = RecurrenceType.NONE.name
                ),
                TaskItem(
                    title = "Daily 30-Min Workout & Focus Walk",
                    description = "Take a mental break from screens, cardio & stretching.",
                    category = TaskCategory.HEALTH.name,
                    priority = TaskPriority.LOW.name,
                    dueDateMillis = todayStart,
                    dueTimeHour = 18,
                    dueTimeMinute = 0,
                    estimatedMinutes = 30,
                    hasReminder = false,
                    recurrence = RecurrenceType.DAILY.name,
                    isCompleted = false
                )
            )

            sampleTasks.forEach { task ->
                insertTask(task)
            }
        }
    }
}
