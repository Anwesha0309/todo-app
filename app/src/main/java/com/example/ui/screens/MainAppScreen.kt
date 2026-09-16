package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.ui.AppScreen
import com.example.ui.TaskFilterStatus
import com.example.ui.TaskViewModel
import com.example.ui.components.AddEditTaskSheet
import com.example.ui.components.AnalyticsVisualizationsView
import com.example.ui.components.BadgesAchievementsDialog
import com.example.ui.components.CalendarMonthView
import com.example.ui.components.DeadlinesFocusView
import com.example.ui.components.GamificationProgressHeader
import com.example.ui.components.NotificationPermissionBanner
import com.example.ui.components.TaskCard
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.components.XpToastBanner
import com.example.ui.theme.CategoryDeadline
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryMeeting
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryWork
import com.example.ui.theme.PriorityUrgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )
                                .testTag("app_header_logo")
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Todo & Calendar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Study & Work Deadline Planner",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Custom Themes Dialog button
                        IconButton(
                            onClick = { viewModel.openThemeDialog() },
                            modifier = Modifier.testTag("btn_open_theme_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Custom Themes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Badges & Achievements button
                        IconButton(
                            onClick = { viewModel.openBadgesDialog() },
                            modifier = Modifier.testTag("btn_open_badges_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Badges & Streaks",
                                tint = Color(0xFFF59E0B)
                            )
                        }

                        // Search icon button
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) viewModel.setSearchQuery("")
                            },
                            modifier = Modifier.testTag("btn_toggle_search")
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = "Search Tasks",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Test Notification Trigger
                        IconButton(
                            onClick = {
                                val sample = uiState.allTasks.firstOrNull { !it.isCompleted }
                                    ?: TaskItem(
                                        title = "Sample Deadline Reminder",
                                        description = "Your automated notification triggered on time!",
                                        dueDateMillis = System.currentTimeMillis(),
                                        category = "STUDY",
                                        priority = "URGENT"
                                    )
                                viewModel.testReminderNotification(context, sample)
                            },
                            modifier = Modifier.testTag("btn_test_notification")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Test Notification",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Search field (when expanded)
                AnimatedVisibility(visible = isSearchExpanded) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search commitments, courses, deliverables...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("input_search_query")
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.TASKS,
                    onClick = { viewModel.setScreen(AppScreen.TASKS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.TASKS) Icons.Default.FormatListBulleted else Icons.Outlined.FormatListBulleted,
                            contentDescription = "Tasks & Agenda"
                        )
                    },
                    label = { Text("Agenda") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_item_tasks")
                )

                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.CALENDAR,
                    onClick = { viewModel.setScreen(AppScreen.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.CALENDAR) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Calendar View"
                        )
                    },
                    label = { Text("Calendar") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_item_calendar")
                )

                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.DEADLINES,
                    onClick = { viewModel.setScreen(AppScreen.DEADLINES) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.DEADLINES) Icons.Default.Timer else Icons.Outlined.Timer,
                            contentDescription = "Deadlines & Focus"
                        )
                    },
                    label = { Text("Deadlines") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_item_deadlines")
                )

                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.ANALYTICS,
                    onClick = { viewModel.setScreen(AppScreen.ANALYTICS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.ANALYTICS) Icons.Default.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Analytics & Insights"
                        )
                    },
                    label = { Text("Insights") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_item_analytics")
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddTask() },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task") },
                text = { Text("New Commitment", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_task")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Gamification XP Reward Notification Banner
            XpToastBanner(
                rewardEvent = uiState.gamificationState.latestRewardEvent,
                onDismiss = { viewModel.dismissRewardToast() }
            )

            // Notification Permission Banner (on Android 13+)
            NotificationPermissionBanner()

            when (uiState.currentScreen) {
                AppScreen.TASKS -> {
                    TasksAgendaView(
                        uiState = uiState,
                        viewModel = viewModel,
                        onTaskClick = { task -> viewModel.openEditTask(task) },
                        onToggleComplete = { task -> viewModel.toggleTaskCompleted(task) },
                        onToggleSubtask = { task, id -> viewModel.toggleSubtask(task, id) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onTestNotification = { task -> viewModel.testReminderNotification(context, task) }
                    )
                }

                AppScreen.CALENDAR -> {
                    CalendarMonthView(
                        currentMonthCal = uiState.currentCalendarMonth,
                        calendarDays = uiState.calendarDays,
                        selectedDateMillis = uiState.selectedDateMillis,
                        allTasks = uiState.allTasks,
                        onSelectDate = { millis -> viewModel.selectDate(millis) },
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onJumpToToday = { viewModel.jumpToToday() },
                        onAddTaskForDate = { millis -> viewModel.openAddTask(millis) },
                        onToggleTaskComplete = { task -> viewModel.toggleTaskCompleted(task) },
                        onToggleSubtask = { task, id -> viewModel.toggleSubtask(task, id) },
                        onTaskClick = { task -> viewModel.openEditTask(task) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onTestNotification = { task -> viewModel.testReminderNotification(context, task) }
                    )
                }

                AppScreen.DEADLINES -> {
                    DeadlinesFocusView(
                        tasks = uiState.allTasks,
                        onToggleTaskComplete = { task -> viewModel.toggleTaskCompleted(task) },
                        onToggleSubtask = { task, id -> viewModel.toggleSubtask(task, id) },
                        onTaskClick = { task -> viewModel.openEditTask(task) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onRescheduleToTomorrow = { task -> viewModel.rescheduleTaskToTomorrow(task) },
                        onTestNotification = { task -> viewModel.testReminderNotification(context, task) }
                    )
                }

                AppScreen.ANALYTICS -> {
                    AnalyticsVisualizationsView(
                        tasks = uiState.allTasks,
                        gamificationState = uiState.gamificationState,
                        onOpenBadges = { viewModel.openBadgesDialog() }
                    )
                }
            }
        }
    }

    // Custom Theme Selection Dialog
    if (uiState.isThemeDialogOpen) {
        ThemeSelectionDialog(
            currentTheme = uiState.themeMode,
            onSelectTheme = { theme -> viewModel.setThemeMode(theme) },
            onDismiss = { viewModel.dismissThemeDialog() }
        )
    }

    // Badges & Achievements Dialog
    if (uiState.isBadgesDialogOpen) {
        BadgesAchievementsDialog(
            gamificationState = uiState.gamificationState,
            onDismiss = { viewModel.dismissBadgesDialog() }
        )
    }

    // Add / Edit Task Modal Sheet
    if (uiState.isAddSheetOpen) {
        AddEditTaskSheet(
            initialTask = uiState.editingTask,
            defaultDateMillis = uiState.selectedDateMillis,
            onDismiss = { viewModel.dismissAddEditSheet() },
            onSave = { id, title, desc, cat, prio, date, hour, min, est, reminder, offset, subs, rec, recInt, recUnit ->
                viewModel.saveTask(id, title, desc, cat, prio, date, hour, min, est, reminder, offset, subs, rec, recInt, recUnit)
            },
            onDelete = { task -> viewModel.deleteTask(task) }
        )
    }
}

@Composable
fun TasksAgendaView(
    uiState: com.example.ui.TaskUiState,
    viewModel: TaskViewModel,
    onTaskClick: (TaskItem) -> Unit,
    onToggleComplete: (TaskItem) -> Unit,
    onToggleSubtask: (TaskItem, String) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onTestNotification: (TaskItem) -> Unit
) {
    val statusScrollState = rememberScrollState()
    val categoryScrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasks_agenda_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Gamification Streak & Level Header
        item {
            GamificationProgressHeader(
                gamificationState = uiState.gamificationState,
                onOpenBadges = { viewModel.openBadgesDialog() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Hero Progress Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_study_work_banner),
                            contentDescription = "Workstation banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient scrim overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(14.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Daily Productivity",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Smart automated alerts & deadline tracker",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // Stats row below banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.todayCompletedCount}/${uiState.todayTasksCount}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Today's Done",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.upcomingDeadlinesCount}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "High Priority",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.overdueCount}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.overdueCount > 0) PriorityUrgent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Overdue",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Status Filter Chips (All, Today, Upcoming, Overdue, Done)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(statusScrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilterStatus.entries.forEach { status ->
                    val isSelected = uiState.statusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setStatusFilter(status) },
                        label = {
                            Text(
                                text = when (status) {
                                    TaskFilterStatus.ALL -> "All (${uiState.allTasks.size})"
                                    TaskFilterStatus.TODAY -> "Today (${uiState.todayTasksCount})"
                                    TaskFilterStatus.UPCOMING -> "Upcoming"
                                    TaskFilterStatus.OVERDUE -> "Overdue (${uiState.overdueCount})"
                                    TaskFilterStatus.COMPLETED -> "Done"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_status_${status.name}")
                    )
                }
            }
        }

        // Category Filter Chips (Study, Work, Personal, etc.)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "All Categories" chip
                FilterChip(
                    selected = uiState.categoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("All Categories", fontSize = 12.sp) }
                )

                TaskCategory.entries.forEach { cat ->
                    val isSelected = uiState.categoryFilter == cat
                    val catColor = when (cat) {
                        TaskCategory.STUDY -> CategoryStudy
                        TaskCategory.WORK -> CategoryWork
                        TaskCategory.DEADLINE -> CategoryDeadline
                        TaskCategory.PERSONAL -> CategoryPersonal
                        TaskCategory.MEETING -> CategoryMeeting
                        TaskCategory.HEALTH -> CategoryHealth
                    }

                    val catIcon = when (cat) {
                        TaskCategory.STUDY -> Icons.Default.School
                        TaskCategory.WORK -> Icons.Default.Work
                        TaskCategory.DEADLINE -> Icons.Default.Timer
                        TaskCategory.PERSONAL -> Icons.Default.Person
                        TaskCategory.MEETING -> Icons.Default.Groups
                        TaskCategory.HEALTH -> Icons.Default.Favorite
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setCategoryFilter(if (isSelected) null else cat)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(cat.displayName, fontSize = 12.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor.copy(alpha = 0.2f),
                            selectedLabelColor = catColor
                        )
                    )
                }
            }
        }

        // Task Items List
        if (uiState.filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .testTag("empty_state_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_all_done),
                            contentDescription = "All done illustration",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.statusFilter == TaskFilterStatus.OVERDUE) "No overdue deadlines!" else "No commitments found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.statusFilter == TaskFilterStatus.OVERDUE)
                                "You're completely on track with your study and work schedule!"
                            else
                                "Tap 'New Commitment' to add your upcoming exams, assignments, or work sprints.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.filteredTasks,
                key = { it.id }
            ) { task ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onToggleSubtask = { subtaskId -> onToggleSubtask(task, subtaskId) },
                        onClick = { onTaskClick(task) },
                        onDelete = { onDeleteTask(task) },
                        onTestNotification = { onTestNotification(task) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB and NavigationBar
        }
    }
}
