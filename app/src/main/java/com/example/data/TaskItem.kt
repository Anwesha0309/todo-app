package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class RecurrenceType(val label: String, val shortLabel: String) {
    NONE("Does not repeat", "None"),
    DAILY("Every day", "Daily"),
    WEEKLY("Every week", "Weekly"),
    MONTHLY("Every month", "Monthly"),
    CUSTOM("Custom interval", "Custom");

    companion object {
        fun fromString(value: String?): RecurrenceType =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}

enum class TaskPriority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    URGENT("Urgent", 4);

    companion object {
        fun fromString(value: String): TaskPriority = entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
    }
}

enum class TaskCategory(val displayName: String, val iconCode: String) {
    STUDY("Study", "school"),
    WORK("Work", "work"),
    DEADLINE("Deadline", "flag"),
    PERSONAL("Personal", "person"),
    MEETING("Meeting", "groups"),
    HEALTH("Health", "favorite");

    companion object {
        fun fromString(value: String): TaskCategory = entries.find { it.name.equals(value, ignoreCase = true) } ?: STUDY
    }
}

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isDone: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = TaskCategory.STUDY.name,
    val priority: String = TaskPriority.MEDIUM.name,
    val dueDateMillis: Long, // Epoch millis of the due date day (at 00:00:00)
    val dueTimeHour: Int? = null, // 0..23, or null
    val dueTimeMinute: Int? = null, // 0..59, or null
    val estimatedMinutes: Int = 30, // For time-blocking / focus planning
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val hasReminder: Boolean = true,
    val reminderOffsetMinutes: Int = 15, // 0 = at time, 15 = 15 min before, 60 = 1 hr before, 1440 = 1 day before
    val reminderTimeMillis: Long? = null,
    val subtasksRaw: String = "", // Delimited subtask storage
    val recurrence: String = RecurrenceType.NONE.name,
    val recurrenceIntervalDays: Int = 1,
    val recurrenceIntervalUnit: String = "DAYS", // "DAYS" or "WEEKS"
    val createdAt: Long = System.currentTimeMillis()
) {
    val recurrenceType: RecurrenceType
        get() = RecurrenceType.fromString(recurrence)

    val isRecurring: Boolean
        get() = recurrenceType != RecurrenceType.NONE

    val recurrenceLabel: String
        get() = when (recurrenceType) {
            RecurrenceType.NONE -> ""
            RecurrenceType.DAILY -> "Daily"
            RecurrenceType.WEEKLY -> "Weekly"
            RecurrenceType.MONTHLY -> "Monthly"
            RecurrenceType.CUSTOM -> {
                val unit = if (recurrenceIntervalUnit.equals("WEEKS", ignoreCase = true)) {
                    if (recurrenceIntervalDays == 1) "week" else "weeks"
                } else {
                    if (recurrenceIntervalDays == 1) "day" else "days"
                }
                "Every $recurrenceIntervalDays $unit"
            }
        }
    val subtasks: List<Subtask>
        get() {
            if (subtasksRaw.isBlank()) return emptyList()
            return subtasksRaw.split("|||").mapNotNull { part ->
                val tokens = part.split(":::")
                if (tokens.size >= 3) {
                    Subtask(
                        id = tokens[0],
                        title = tokens[1],
                        isDone = tokens[2].toBooleanStrictOrNull() ?: false
                    )
                } else null
            }
        }

    val taskCategory: TaskCategory
        get() = TaskCategory.fromString(category)

    val taskPriority: TaskPriority
        get() = TaskPriority.fromString(priority)

    val formattedDueDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return sdf.format(Date(dueDateMillis))
        }

    val formattedDueTime: String?
        get() {
            if (dueTimeHour == null || dueTimeMinute == null) return null
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, dueTimeHour)
                set(Calendar.MINUTE, dueTimeMinute)
            }
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            return sdf.format(cal.time)
        }

    val exactDueTimeMillis: Long
        get() {
            val cal = Calendar.getInstance().apply {
                timeInMillis = dueDateMillis
                if (dueTimeHour != null && dueTimeMinute != null) {
                    set(Calendar.HOUR_OF_DAY, dueTimeHour)
                    set(Calendar.MINUTE, dueTimeMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                } else {
                    // Default to end of day if no specific hour given
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }
            }
            return cal.timeInMillis
        }

    val isOverdue: Boolean
        get() {
            if (isCompleted) return false
            return System.currentTimeMillis() > exactDueTimeMillis
        }

    fun copyWithSubtasks(list: List<Subtask>): TaskItem {
        val encoded = list.joinToString("|||") { "${it.id}:::${it.title.replace(":::", " ").replace("|||", " ")}:::${it.isDone}" }
        return this.copy(subtasksRaw = encoded)
    }

    /**
     * Generates the next instance for recurring tasks.
     * Preserves time, category, priority, duration, reminder settings.
     * Resets subtasks so the checklist can be done anew.
     */
    fun computeNextOccurrence(): TaskItem? {
        if (!isRecurring) return null

        val cal = Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
        }

        when (recurrenceType) {
            RecurrenceType.DAILY -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            RecurrenceType.WEEKLY -> {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            RecurrenceType.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
            }
            RecurrenceType.CUSTOM -> {
                val step = recurrenceIntervalDays.coerceAtLeast(1)
                if (recurrenceIntervalUnit.equals("WEEKS", ignoreCase = true)) {
                    cal.add(Calendar.DAY_OF_YEAR, step * 7)
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, step)
                }
            }
            RecurrenceType.NONE -> return null
        }

        // Fresh subtask copies with unchecked state
        val resetSubtasks = subtasks.map { subtask ->
            subtask.copy(id = UUID.randomUUID().toString(), isDone = false)
        }

        return this.copy(
            id = 0L, // New task ID will be generated by Room
            dueDateMillis = cal.timeInMillis,
            isCompleted = false,
            completedAtMillis = null,
            subtasksRaw = encodeSubtasks(resetSubtasks),
            createdAt = System.currentTimeMillis()
        )
    }

    companion object {
        fun encodeSubtasks(list: List<Subtask>): String {
            return list.joinToString("|||") { "${it.id}:::${it.title.replace(":::", " ").replace("|||", " ")}:::${it.isDone}" }
        }
    }
}
