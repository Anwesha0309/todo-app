package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subtask
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.ui.theme.CategoryDeadline
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryMeeting
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityUrgent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: TaskItem,
    onToggleComplete: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedSubtasks by remember { mutableStateOf(false) }

    val categoryColor = when (task.taskCategory) {
        TaskCategory.STUDY -> CategoryStudy
        TaskCategory.WORK -> CategoryWork
        TaskCategory.DEADLINE -> CategoryDeadline
        TaskCategory.PERSONAL -> CategoryPersonal
        TaskCategory.MEETING -> CategoryMeeting
        TaskCategory.HEALTH -> CategoryHealth
    }

    val priorityColor = when (task.taskPriority) {
        TaskPriority.URGENT -> PriorityUrgent
        TaskPriority.HIGH -> PriorityHigh
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.LOW -> PriorityLow
    }

    val priorityVector = when (task.taskPriority) {
        TaskPriority.URGENT -> Icons.Default.PriorityHigh
        TaskPriority.HIGH -> Icons.Default.Flag
        TaskPriority.MEDIUM -> Icons.Default.Remove
        TaskPriority.LOW -> Icons.Default.ArrowDownward
    }

    val priorityXp = when (task.taskPriority) {
        TaskPriority.URGENT -> if (task.isRecurring) "+75 XP" else "+60 XP"
        TaskPriority.HIGH -> if (task.isRecurring) "+55 XP" else "+40 XP"
        TaskPriority.MEDIUM -> if (task.isRecurring) "+40 XP" else "+25 XP"
        TaskPriority.LOW -> if (task.isRecurring) "+30 XP" else "+15 XP"
    }

    val categoryVector = when (task.taskCategory) {
        TaskCategory.STUDY -> Icons.Default.School
        TaskCategory.WORK -> Icons.Default.Work
        TaskCategory.DEADLINE -> Icons.Default.Timer
        TaskCategory.PERSONAL -> Icons.Default.Person
        TaskCategory.MEETING -> Icons.Default.Groups
        TaskCategory.HEALTH -> Icons.Default.Favorite
    }

    val cardBorder = if (task.isOverdue && !task.isCompleted) {
        BorderStroke(1.5.dp, PriorityUrgent)
    } else if (task.taskPriority == TaskPriority.URGENT && !task.isCompleted) {
        BorderStroke(1.dp, PriorityUrgent.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    val containerColor = if (task.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    } else if (task.taskPriority == TaskPriority.URGENT) {
        PriorityUrgent.copy(alpha = 0.04f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Visual Priority Accent Line on left edge
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(if (task.isCompleted) priorityColor.copy(alpha = 0.3f) else priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Top Row: Category Pill, Priority (if High/Urgent), Overdue Tag, and Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Category Pill
                        Surface(
                            color = categoryColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = categoryVector,
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = task.taskCategory.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = categoryColor
                                )
                            }
                        }

                        // Priority Badge shown for High and Urgent priority
                        if (task.taskPriority == TaskPriority.URGENT || task.taskPriority == TaskPriority.HIGH) {
                            Surface(
                                color = priorityColor.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = priorityVector,
                                        contentDescription = null,
                                        tint = priorityColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = task.taskPriority.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = priorityColor
                                    )
                                }
                            }
                        }

                        // Recurring Task Pill
                        if (task.isRecurring) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = task.recurrenceLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Overdue Badge
                        if (task.isOverdue && !task.isCompleted) {
                            Surface(
                                color = PriorityUrgent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Overdue",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Top Right Quick Actions: Edit and Delete buttons (48dp touch targets)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_edit_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit task",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_delete_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete task",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Middle Row: Large Checkbox (48dp touch target) and Title / Description
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onToggleComplete() }
                            .testTag("task_checkbox_target_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("task_checkbox_${task.id}")
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onClick() }
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtasks summary (if any)
            if (task.subtasks.isNotEmpty()) {
                val completedCount = task.subtasks.count { it.isDone }
                val totalCount = task.subtasks.size
                val progress = completedCount.toFloat() / totalCount.coerceAtLeast(1)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { expandedSubtasks = !expandedSubtasks }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Checklist ($completedCount/$totalCount)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (expandedSubtasks) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Subtasks",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    AnimatedVisibility(visible = expandedSubtasks) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            task.subtasks.forEach { subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleSubtask(subtask.id) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(
                                            1.dp,
                                            if (subtask.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        ),
                                        color = if (subtask.isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        if (subtask.isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subtask.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None
                                        ),
                                        color = if (subtask.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Bottom Info Row: Due Date, Time, Duration, and Reminder tag
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Due Date Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Due Date",
                        tint = if (task.isOverdue && !task.isCompleted) PriorityUrgent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.formattedDueDate + (task.formattedDueTime?.let { " at $it" } ?: ""),
                        fontSize = 12.sp,
                        fontWeight = if (task.isOverdue && !task.isCompleted) FontWeight.Bold else FontWeight.Normal,
                        color = if (task.isOverdue && !task.isCompleted) PriorityUrgent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Estimated focus time
                if (task.estimatedMinutes > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Estimated Focus",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${task.estimatedMinutes} min",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Reminder status badge
                if (task.hasReminder) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Reminder set",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val offsetLabel = when (task.reminderOffsetMinutes) {
                            0 -> "At due time"
                            15 -> "15m before"
                            30 -> "30m before"
                            60 -> "1h before"
                            1440 -> "1d before"
                            else -> "${task.reminderOffsetMinutes}m before"
                        }
                        Text(
                            text = offsetLabel,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
}

