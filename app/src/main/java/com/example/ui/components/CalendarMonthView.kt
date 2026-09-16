package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.ui.CalendarDay
import com.example.ui.getStartOfDay
import com.example.ui.theme.CategoryDeadline
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryMeeting
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityUrgent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarMonthView(
    currentMonthCal: Calendar,
    calendarDays: List<CalendarDay>,
    selectedDateMillis: Long,
    allTasks: List<TaskItem>,
    onSelectDate: (Long) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    onAddTaskForDate: (Long) -> Unit,
    onToggleTaskComplete: (TaskItem) -> Unit,
    onToggleSubtask: (TaskItem, String) -> Unit,
    onTaskClick: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onTestNotification: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonthCal.time)
    val selectedDateFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))

    // Filter tasks for the selected date
    val selectedDayStart = getStartOfDay(selectedDateMillis)
    val selectedDayEnd = selectedDayStart + 24 * 60 * 60 * 1000L - 1
    val selectedDayTasks = allTasks.filter { it.dueDateMillis in selectedDayStart..selectedDayEnd }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_month_view")
    ) {
        // Month Navigation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header: Month Title & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Interactive Deadline Calendar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onJumpToToday,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("btn_jump_today")
                        ) {
                            Text(text = "Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onNextMonth,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Day of Week Headers
                val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    dayHeaders.forEachIndexed { index, header ->
                        val isWeekend = index == 0 || index == 6
                        Text(
                            text = header,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Days Grid (6 weeks * 7 days)
                for (weekIndex in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (dayIndex in 0 until 7) {
                            val cellIndex = weekIndex * 7 + dayIndex
                            if (cellIndex < calendarDays.size) {
                                val day = calendarDays[cellIndex]
                                CalendarDayCell(
                                    day = day,
                                    onClick = { onSelectDate(day.date.time) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Day Schedule & Deadlines Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedDateFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedDayTasks.size} commitment${if (selectedDayTasks.size != 1) "s" else ""} scheduled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = { onAddTaskForDate(selectedDateMillis) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_add_task_selected_date")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Task", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // List of tasks for the selected date
        if (selectedDayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No commitments for this day",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Add Task' above to schedule study or work deadlines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                selectedDayTasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleTaskComplete(task) },
                        onToggleSubtask = { subtaskId -> onToggleSubtask(task, subtaskId) },
                        onClick = { onTaskClick(task) },
                        onDelete = { onDeleteTask(task) },
                        onTestNotification = { onTestNotification(task) }
                    )
                }
                Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        day.isSelected -> Color.White
        day.isToday -> MaterialTheme.colorScheme.primary
        day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    }

    val cellBackground = when {
        day.isSelected -> MaterialTheme.colorScheme.primary
        day.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    val cellBorder = if (day.isToday && !day.isSelected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    } else null

    Column(
        modifier = modifier
            .padding(vertical = 2.dp, horizontal = 1.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (cellBorder != null) Modifier.background(cellBackground, RoundedCornerShape(10.dp)).then(
                    Modifier.padding(1.dp)
                ) else Modifier.background(cellBackground)
            )
            .clickable { onClick() }
            .testTag("cal_cell_${day.dayOfMonth}_${day.isCurrentMonth}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            fontSize = 13.sp,
            fontWeight = if (day.isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        // Task Indicator Dots (up to 3)
        if (day.tasks.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                val previewTasks = day.tasks.take(3)
                previewTasks.forEach { task ->
                    val dotColor = when {
                        day.isSelected -> Color.White.copy(alpha = 0.85f)
                        task.taskPriority == TaskPriority.URGENT -> PriorityUrgent
                        task.taskPriority == TaskPriority.HIGH -> PriorityHigh
                        task.taskCategory == TaskCategory.STUDY -> CategoryStudy
                        task.taskCategory == TaskCategory.WORK -> CategoryWork
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}
