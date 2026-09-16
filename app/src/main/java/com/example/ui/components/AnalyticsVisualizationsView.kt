package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GamificationState
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsVisualizationsView(
    tasks: List<TaskItem>,
    gamificationState: GamificationState,
    onOpenBadges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val executionRate = if (totalTasks > 0) (completedTasks * 100f / totalTasks).toInt() else 0

    // Weekly performance calculation (Last 7 days)
    val weeklyData = remember(tasks) {
        calculateWeeklyMetrics(tasks)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_visualizations_view")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Executive Brand & Intelligence Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analytics_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "Executive Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Operational Intelligence",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Analytics, velocity benchmarks & execution metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = gamificationState.levelTitle,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { onOpenBadges() }
                            ) {
                                Text(
                                    text = "${gamificationState.totalXp} XP Points",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Key Performance Indicators Grid
        item {
            Text(
                text = "Key Performance Indicators",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Execution Rate",
                    value = "$executionRate%",
                    subtitle = "$completedTasks of $totalTasks finished",
                    icon = Icons.Default.Assessment,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Active Streak",
                    value = "${gamificationState.currentStreak} Days",
                    subtitle = "Best: ${gamificationState.bestStreak} days record",
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Pending Queue",
                    value = "$pendingTasks",
                    subtitle = "Active commitments",
                    icon = Icons.Default.Schedule,
                    accentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Completed Total",
                    value = "${gamificationState.totalTasksCompleted}",
                    subtitle = "Lifetime deliveries",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Velocity Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_weekly_velocity"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Weekly Task Velocity",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Scheduled vs Completed instances over 7 days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scheduled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Native Canvas Weekly Bar Chart
                    WeeklyBarChart(
                        data = weeklyData,
                        primaryColor = MaterialTheme.colorScheme.primary,
                        scheduledColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        baselineColor = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
        }

        // Category Workload Allocation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_category_allocation"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Workload Distribution by Domain",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Proportional allocation across registered categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-Segmented Proportional Bar
                    CategorySegmentedBar(
                        tasks = tasks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Breakdown Items
                    CategoryBreakdownList(tasks = tasks)
                }
            }
        }

        // Priority Distribution & Risk Matrix
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_priority_matrix"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Priority Delivery Matrix",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Task volume and resolution progress per priority tier",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    PriorityTierProgress(
                        label = "Urgent Priority",
                        tierTasks = tasks.filter { it.taskPriority == TaskPriority.URGENT },
                        color = PriorityUrgent,
                        icon = Icons.Default.PriorityHigh
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PriorityTierProgress(
                        label = "High Priority",
                        tierTasks = tasks.filter { it.taskPriority == TaskPriority.HIGH },
                        color = PriorityHigh,
                        icon = Icons.Default.Flag
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PriorityTierProgress(
                        label = "Medium Priority",
                        tierTasks = tasks.filter { it.taskPriority == TaskPriority.MEDIUM },
                        color = PriorityMedium,
                        icon = Icons.Default.Remove
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PriorityTierProgress(
                        label = "Low Priority",
                        tierTasks = tasks.filter { it.taskPriority == TaskPriority.LOW },
                        color = PriorityLow,
                        icon = Icons.Default.ArrowDownward
                    )
                }
            }
        }

        // Cadence & Automation Metrics
        item {
            val recurringTasks = tasks.filter { it.isRecurring }
            val oneOffTasks = tasks.filter { !it.isRecurring }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_cadence_automation"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cadence Automation",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${recurringTasks.size} recurring routines • ${oneOffTasks.size} single commitments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${recurringTasks.count { it.isCompleted }} Done",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

data class DayMetric(
    val dayLabel: String,
    val totalCount: Int,
    val completedCount: Int
)

private fun calculateWeeklyMetrics(tasks: List<TaskItem>): List<DayMetric> {
    val calendar = Calendar.getInstance()
    // Reset to start of day
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val metrics = mutableListOf<DayMetric>()

    // Move back 6 days to start the 7-day window
    calendar.add(Calendar.DAY_OF_YEAR, -6)

    for (i in 0..6) {
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1) // revert back for label

        val dayTasks = tasks.filter { it.dueDateMillis in startOfDay until endOfDay }
        val completed = dayTasks.count { it.isCompleted }

        metrics.add(
            DayMetric(
                dayLabel = dateFormat.format(Date(startOfDay)),
                totalCount = dayTasks.size,
                completedCount = completed
            )
        )

        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return metrics
}

@Composable
private fun WeeklyBarChart(
    data: List<DayMetric>,
    primaryColor: Color,
    scheduledColor: Color,
    baselineColor: Color,
    modifier: Modifier = Modifier
) {
    val maxCount = (data.maxOfOrNull { it.totalCount } ?: 4).coerceAtLeast(4)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartHeight = size.height - 10f
                val chartWidth = size.width
                val barSpacing = chartWidth / data.size
                val barWidth = barSpacing * 0.42f

                // Draw horizontal benchmark guideline at 50% and 100%
                drawLine(
                    color = baselineColor.copy(alpha = 0.3f),
                    start = Offset(0f, chartHeight),
                    end = Offset(chartWidth, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = baselineColor.copy(alpha = 0.2f),
                    start = Offset(0f, chartHeight * 0.5f),
                    end = Offset(chartWidth, chartHeight * 0.5f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                data.forEachIndexed { index, metric ->
                    val centerX = (index * barSpacing) + (barSpacing / 2f)
                    val left = centerX - (barWidth / 2f)

                    // Draw total scheduled bar (background column)
                    val scheduledHeight = if (metric.totalCount > 0) {
                        (metric.totalCount.toFloat() / maxCount) * chartHeight
                    } else {
                        4f // minimum indicator
                    }
                    val scheduledTop = chartHeight - scheduledHeight

                    drawRoundRect(
                        color = scheduledColor,
                        topLeft = Offset(left, scheduledTop),
                        size = Size(barWidth, scheduledHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw completed bar on top
                    if (metric.completedCount > 0) {
                        val completedHeight = (metric.completedCount.toFloat() / maxCount) * chartHeight
                        val completedTop = chartHeight - completedHeight

                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(left, completedTop),
                            size = Size(barWidth, completedHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { metric ->
                Text(
                    text = metric.dayLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CategorySegmentedBar(
    tasks: List<TaskItem>,
    modifier: Modifier = Modifier
) {
    val total = tasks.size
    if (total == 0) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        )
        return
    }

    Row(modifier = modifier) {
        TaskCategory.entries.forEach { cat ->
            val count = tasks.count { it.taskCategory == cat }
            if (count > 0) {
                val weight = count.toFloat() / total
                val catColor = when (cat) {
                    TaskCategory.STUDY -> CategoryStudy
                    TaskCategory.WORK -> CategoryWork
                    TaskCategory.DEADLINE -> CategoryDeadline
                    TaskCategory.PERSONAL -> CategoryPersonal
                    TaskCategory.MEETING -> CategoryMeeting
                    TaskCategory.HEALTH -> CategoryHealth
                }
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .fillMaxHeight()
                        .background(catColor)
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownList(tasks: List<TaskItem>) {
    val total = tasks.size

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskCategory.entries.forEach { cat ->
            val catTasks = tasks.filter { it.taskCategory == cat }
            val count = catTasks.size
            val completed = catTasks.count { it.isCompleted }
            val percentage = if (total > 0) (count * 100 / total) else 0

            val catColor = when (cat) {
                TaskCategory.STUDY -> CategoryStudy
                TaskCategory.WORK -> CategoryWork
                TaskCategory.DEADLINE -> CategoryDeadline
                TaskCategory.PERSONAL -> CategoryPersonal
                TaskCategory.MEETING -> CategoryMeeting
                TaskCategory.HEALTH -> CategoryHealth
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cat.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$completed/$count completed",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$percentage%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = catColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityTierProgress(
    label: String,
    tierTasks: List<TaskItem>,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val total = tierTasks.size
    val completed = tierTasks.count { it.isCompleted }
    val progress = if (total > 0) (completed.toFloat() / total) else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = if (total > 0) "$completed / $total completed (${(progress * 100).toInt()}%)" else "0 commitments",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (total > 0) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    }
}
