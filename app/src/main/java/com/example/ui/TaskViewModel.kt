package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GamificationState
import com.example.data.RecurrenceType
import com.example.data.Subtask
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.data.TaskRepository
import com.example.reminder.NotificationHelper
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen(val title: String) {
    TASKS("Tasks & Agenda"),
    CALENDAR("Calendar View"),
    DEADLINES("Deadlines & Focus"),
    ANALYTICS("Analytics & Insights")
}

enum class TaskFilterStatus(val label: String) {
    ALL("All"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    OVERDUE("Overdue"),
    COMPLETED("Done")
}

data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val tasks: List<TaskItem>
)

data class TaskUiState(
    val currentScreen: AppScreen = AppScreen.TASKS,
    val allTasks: List<TaskItem> = emptyList(),
    val filteredTasks: List<TaskItem> = emptyList(),
    val selectedDateMillis: Long = getStartOfDay(System.currentTimeMillis()),
    val currentCalendarMonth: Calendar = Calendar.getInstance(),
    val searchQuery: String = "",
    val categoryFilter: TaskCategory? = null,
    val statusFilter: TaskFilterStatus = TaskFilterStatus.ALL,
    val isAddSheetOpen: Boolean = false,
    val editingTask: TaskItem? = null,
    val calendarDays: List<CalendarDay> = emptyList(),
    val todayTasksCount: Int = 0,
    val todayCompletedCount: Int = 0,
    val overdueCount: Int = 0,
    val upcomingDeadlinesCount: Int = 0,
    val totalFocusMinutesToday: Int = 0,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val isThemeDialogOpen: Boolean = false,
    val isBadgesDialogOpen: Boolean = false,
    val gamificationState: GamificationState = GamificationState()
)

fun getStartOfDay(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.TASKS)
    private val _selectedDateMillis = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    private val _currentCalendarMonth = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    })
    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow<TaskCategory?>(null)
    private val _statusFilter = MutableStateFlow(TaskFilterStatus.ALL)
    private val _isAddSheetOpen = MutableStateFlow(false)
    private val _editingTask = MutableStateFlow<TaskItem?>(null)
    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)
    private val _isThemeDialogOpen = MutableStateFlow(false)
    private val _isBadgesDialogOpen = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.seedSampleTasksIfEmpty()
        }
    }

    val uiState: StateFlow<TaskUiState> = combine(
        combine(
            repository.allTasks,
            _currentScreen,
            _selectedDateMillis,
            _currentCalendarMonth,
            _searchQuery
        ) { tasks, screen, selectedDate, currentMonth, search ->
            CombinedPrimary(tasks, screen, selectedDate, currentMonth, search)
        },
        combine(
            _categoryFilter,
            _statusFilter,
            _isAddSheetOpen,
            _editingTask,
            _themeMode
        ) { catFilter, status, isAddOpen, editing, theme ->
            CombinedSecondary(catFilter, status, isAddOpen, editing, theme)
        },
        combine(
            _isThemeDialogOpen,
            _isBadgesDialogOpen,
            repository.gamificationRepository.state
        ) { isThemeOpen, isBadgesOpen, gamState ->
            CombinedTertiary(isThemeOpen, isBadgesOpen, gamState)
        }
    ) { primary, secondary, tertiary ->
        val tasks = primary.tasks
        val screen = primary.screen
        val selectedDate = primary.selectedDate
        val currentMonth = primary.currentMonth
        val search = primary.search

        val catFilter = secondary.catFilter
        val status = secondary.status
        val isAddOpen = secondary.isAddOpen
        val editing = secondary.editing
        val theme = secondary.theme

        val isThemeOpen = tertiary.isThemeOpen
        val isBadgesOpen = tertiary.isBadgesOpen
        val gamState = tertiary.gamState

        val todayStart = getStartOfDay(System.currentTimeMillis())
        val todayEnd = todayStart + 24 * 60 * 60 * 1000L - 1

        val todayTasks = tasks.filter { it.dueDateMillis in todayStart..todayEnd }
        val todayCompleted = todayTasks.count { it.isCompleted }
        val overdueTasks = tasks.filter { it.isOverdue }
        val upcomingDeadlines = tasks.filter { !it.isCompleted && (it.taskPriority == TaskPriority.URGENT || it.taskPriority == TaskPriority.HIGH) }
        val focusMinsToday = todayTasks.filter { !it.isCompleted }.sumOf { it.estimatedMinutes }

        // Filtered tasks for list view
        val filtered = tasks.filter { task ->
            // Search query
            val matchesSearch = search.isBlank() ||
                    task.title.contains(search, ignoreCase = true) ||
                    task.description.contains(search, ignoreCase = true)

            // Category
            val matchesCategory = catFilter == null || task.category.equals(catFilter.name, ignoreCase = true)

            // Status filter
            val matchesStatus = when (status) {
                TaskFilterStatus.ALL -> true
                TaskFilterStatus.TODAY -> task.dueDateMillis in todayStart..todayEnd
                TaskFilterStatus.UPCOMING -> task.dueDateMillis > todayEnd && !task.isCompleted
                TaskFilterStatus.OVERDUE -> task.isOverdue
                TaskFilterStatus.COMPLETED -> task.isCompleted
            }

            matchesSearch && matchesCategory && matchesStatus
        }

        // Generate calendar days for current month
        val days = buildCalendarDays(currentMonth, selectedDate, tasks)

        TaskUiState(
            currentScreen = screen,
            allTasks = tasks,
            filteredTasks = filtered,
            selectedDateMillis = selectedDate,
            currentCalendarMonth = currentMonth,
            searchQuery = search,
            categoryFilter = catFilter,
            statusFilter = status,
            isAddSheetOpen = isAddOpen,
            editingTask = editing,
            calendarDays = days,
            todayTasksCount = todayTasks.size,
            todayCompletedCount = todayCompleted,
            overdueCount = overdueTasks.size,
            upcomingDeadlinesCount = upcomingDeadlines.size,
            totalFocusMinutesToday = focusMinsToday,
            themeMode = theme,
            isThemeDialogOpen = isThemeOpen,
            isBadgesDialogOpen = isBadgesOpen,
            gamificationState = gamState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    private fun buildCalendarDays(
        monthCal: Calendar,
        selectedDateMillis: Long,
        tasks: List<TaskItem>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val cal = monthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sunday = 1, Monday = 2
        val daysBefore = firstDayOfWeek - 1

        cal.add(Calendar.DAY_OF_MONTH, -daysBefore)

        val targetMonth = monthCal.get(Calendar.MONTH)
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val selectedStart = getStartOfDay(selectedDateMillis)

        // 6 weeks * 7 days = 42 grid cells
        for (i in 0 until 42) {
            val cellDate = cal.time
            val cellStart = getStartOfDay(cal.timeInMillis)
            val cellEnd = cellStart + 24 * 60 * 60 * 1000L - 1
            val dayTasks = tasks.filter { it.dueDateMillis in cellStart..cellEnd }

            days.add(
                CalendarDay(
                    date = cellDate,
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    isCurrentMonth = cal.get(Calendar.MONTH) == targetMonth,
                    isToday = cellStart == todayStart,
                    isSelected = cellStart == selectedStart,
                    tasks = dayTasks
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun openThemeDialog() {
        _isThemeDialogOpen.value = true
    }

    fun dismissThemeDialog() {
        _isThemeDialogOpen.value = false
    }

    fun openBadgesDialog() {
        _isBadgesDialogOpen.value = true
    }

    fun dismissBadgesDialog() {
        _isBadgesDialogOpen.value = false
    }

    fun dismissRewardToast() {
        repository.gamificationRepository.clearRewardEvent()
    }

    fun selectDate(millis: Long) {
        _selectedDateMillis.value = getStartOfDay(millis)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: TaskCategory?) {
        _categoryFilter.value = category
    }

    fun setStatusFilter(status: TaskFilterStatus) {
        _statusFilter.value = status
    }

    fun openAddTask(initialDateMillis: Long? = null) {
        if (initialDateMillis != null) {
            _selectedDateMillis.value = getStartOfDay(initialDateMillis)
        }
        _editingTask.value = null
        _isAddSheetOpen.value = true
    }

    fun openEditTask(task: TaskItem) {
        _editingTask.value = task
        _isAddSheetOpen.value = true
    }

    fun dismissAddEditSheet() {
        _isAddSheetOpen.value = false
        _editingTask.value = null
    }

    fun nextMonth() {
        val cal = _currentCalendarMonth.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _currentCalendarMonth.value = cal
    }

    fun previousMonth() {
        val cal = _currentCalendarMonth.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _currentCalendarMonth.value = cal
    }

    fun jumpToToday() {
        val now = System.currentTimeMillis()
        val todayStart = getStartOfDay(now)
        _selectedDateMillis.value = todayStart

        val cal = Calendar.getInstance().apply {
            timeInMillis = todayStart
            set(Calendar.DAY_OF_MONTH, 1)
        }
        _currentCalendarMonth.value = cal
    }

    fun saveTask(
        id: Long,
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDateMillis: Long,
        dueTimeHour: Int?,
        dueTimeMinute: Int?,
        estimatedMinutes: Int,
        hasReminder: Boolean,
        reminderOffsetMinutes: Int,
        subtasks: List<Subtask>,
        recurrence: RecurrenceType = RecurrenceType.NONE,
        recurrenceIntervalDays: Int = 1,
        recurrenceIntervalUnit: String = "DAYS"
    ) {
        viewModelScope.launch {
            val task = TaskItem(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category.name,
                priority = priority.name,
                dueDateMillis = getStartOfDay(dueDateMillis),
                dueTimeHour = dueTimeHour,
                dueTimeMinute = dueTimeMinute,
                estimatedMinutes = estimatedMinutes,
                hasReminder = hasReminder,
                reminderOffsetMinutes = reminderOffsetMinutes,
                subtasksRaw = TaskItem.encodeSubtasks(subtasks),
                recurrence = recurrence.name,
                recurrenceIntervalDays = recurrenceIntervalDays,
                recurrenceIntervalUnit = recurrenceIntervalUnit
            )

            if (id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
            dismissAddEditSheet()
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun toggleSubtask(task: TaskItem, subtaskId: String) {
        viewModelScope.launch {
            repository.toggleSubtask(task, subtaskId)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (_editingTask.value?.id == task.id) {
                dismissAddEditSheet()
            }
        }
    }

    fun rescheduleTaskToTomorrow(task: TaskItem) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.timeInMillis = task.dueDateMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val updated = task.copy(dueDateMillis = getStartOfDay(cal.timeInMillis))
            repository.updateTask(updated)
        }
    }

    fun testReminderNotification(context: Context, task: TaskItem) {
        NotificationHelper.showTaskReminder(
            context = context,
            taskId = if (task.id == 0L) 9999L else task.id,
            title = task.title,
            category = task.category,
            priority = task.priority,
            notes = task.description
        )
    }

    companion object {
        fun provideFactory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TaskViewModel(repository) as T
                }
            }
    }
}

private data class CombinedPrimary(
    val tasks: List<TaskItem>,
    val screen: AppScreen,
    val selectedDate: Long,
    val currentMonth: Calendar,
    val search: String
)

private data class CombinedSecondary(
    val catFilter: TaskCategory?,
    val status: TaskFilterStatus,
    val isAddOpen: Boolean,
    val editing: TaskItem?,
    val theme: AppThemeMode
)

private data class CombinedTertiary(
    val isThemeOpen: Boolean,
    val isBadgesOpen: Boolean,
    val gamState: GamificationState
)
