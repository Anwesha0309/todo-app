package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val isUnlocked: Boolean
)

data class XpRewardEvent(
    val amount: Int,
    val reason: String,
    val didLevelUp: Boolean = false,
    val newLevel: Int = 1,
    val newBadge: Badge? = null
)

data class GamificationState(
    val totalXp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Associate Analyst",
    val currentLevelMinXp: Int = 0,
    val nextLevelTargetXp: Int = 100,
    val levelProgress: Float = 0f,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalTasksCompleted: Int = 0,
    val badges: List<Badge> = emptyList(),
    val latestRewardEvent: XpRewardEvent? = null
)

class GamificationRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("gamification_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<GamificationState> = _state.asStateFlow()

    private fun loadState(): GamificationState {
        val totalXp = prefs.getInt(KEY_XP, 25) // start with 25 welcome XP
        val streak = prefs.getInt(KEY_STREAK, 1)
        val bestStreak = prefs.getInt(KEY_BEST_STREAK, 1)
        val totalCompleted = prefs.getInt(KEY_TOTAL_COMPLETED, 0)
        val unlockedBadgeIds = prefs.getStringSet(KEY_BADGES, emptySet()) ?: emptySet()

        return calculateState(totalXp, streak, bestStreak, totalCompleted, unlockedBadgeIds, null)
    }

    private fun calculateState(
        totalXp: Int,
        streak: Int,
        bestStreak: Int,
        totalCompleted: Int,
        unlockedIds: Set<String>,
        rewardEvent: XpRewardEvent?
    ): GamificationState {
        val (level, title, minXp, maxXp) = getLevelInfo(totalXp)
        val progress = if (maxXp > minXp) {
            ((totalXp - minXp).toFloat() / (maxXp - minXp).toFloat()).coerceIn(0f, 1f)
        } else 1f

        val allBadges = getAllBadgesList(unlockedIds)

        return GamificationState(
            totalXp = totalXp,
            level = level,
            levelTitle = title,
            currentLevelMinXp = minXp,
            nextLevelTargetXp = maxXp,
            levelProgress = progress,
            currentStreak = streak,
            bestStreak = bestStreak,
            totalTasksCompleted = totalCompleted,
            badges = allBadges,
            latestRewardEvent = rewardEvent
        )
    }

    private fun getLevelInfo(xp: Int): Quadruple<Int, String, Int, Int> {
        return when {
            xp < 100 -> Quadruple(1, "Beginner", 0, 100)
            xp < 250 -> Quadruple(2, "Steady Planner", 100, 250)
            xp < 500 -> Quadruple(3, "Goal Getter", 250, 500)
            xp < 900 -> Quadruple(4, "Productivity Pro", 500, 900)
            xp < 1500 -> Quadruple(5, "Master Organizer", 900, 1500)
            else -> Quadruple(6, "Achiever", 1500, 2500)
        }
    }

    fun onTaskCompleted(task: TaskItem): XpRewardEvent {
        val baseReward = when (task.taskPriority) {
            TaskPriority.LOW -> 15
            TaskPriority.MEDIUM -> 25
            TaskPriority.HIGH -> 40
            TaskPriority.URGENT -> 60
        }
        val recurrenceBonus = if (task.isRecurring) 15 else 0
        val totalEarned = baseReward + recurrenceBonus

        val oldState = _state.value
        val oldLevel = oldState.level
        val newXp = oldState.totalXp + totalEarned
        val newTotalCompleted = oldState.totalTasksCompleted + 1

        // Update streaks
        val (newStreak, newBestStreak) = updateStreak()

        // Check new badges
        val currentUnlocked = prefs.getStringSet(KEY_BADGES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        var newlyUnlockedBadge: Badge? = null

        fun tryUnlock(id: String) {
            if (!currentUnlocked.contains(id)) {
                currentUnlocked.add(id)
                newlyUnlockedBadge = getAllBadgesList(currentUnlocked).find { it.id == id }
            }
        }

        // Check badge conditions
        tryUnlock(BADGE_FIRST_STEP)
        if (newStreak >= 3) tryUnlock(BADGE_STREAK_3)
        if (newStreak >= 7) tryUnlock(BADGE_STREAK_7)
        if (task.taskPriority == TaskPriority.URGENT) tryUnlock(BADGE_URGENT_SLAYER)
        if (task.isRecurring) tryUnlock(BADGE_ROUTINE_MASTER)
        if (newXp >= 500) tryUnlock(BADGE_CENTURION)

        val (newLevel, _, _, _) = getLevelInfo(newXp)
        val didLevelUp = newLevel > oldLevel

        // Save
        prefs.edit()
            .putInt(KEY_XP, newXp)
            .putInt(KEY_STREAK, newStreak)
            .putInt(KEY_BEST_STREAK, newBestStreak)
            .putInt(KEY_TOTAL_COMPLETED, newTotalCompleted)
            .putStringSet(KEY_BADGES, currentUnlocked)
            .apply()

        val reason = buildString {
            append("+${totalEarned} XP • ${task.taskPriority.label} Priority")
            if (task.isRecurring) append(" (+15 Routine Bonus)")
        }

        val rewardEvent = XpRewardEvent(
            amount = totalEarned,
            reason = reason,
            didLevelUp = didLevelUp,
            newLevel = newLevel,
            newBadge = newlyUnlockedBadge
        )

        _state.value = calculateState(newXp, newStreak, newBestStreak, newTotalCompleted, currentUnlocked, rewardEvent)
        return rewardEvent
    }

    fun onSubtaskCompleted(): XpRewardEvent {
        val earned = 5
        val oldState = _state.value
        val oldLevel = oldState.level
        val newXp = oldState.totalXp + earned
        val currentUnlocked = prefs.getStringSet(KEY_BADGES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        var newlyUnlocked: Badge? = null

        if (!currentUnlocked.contains(BADGE_SUBTASK_MASTER)) {
            currentUnlocked.add(BADGE_SUBTASK_MASTER)
            newlyUnlocked = getAllBadgesList(currentUnlocked).find { it.id == BADGE_SUBTASK_MASTER }
        }

        val (newLevel, _, _, _) = getLevelInfo(newXp)
        val didLevelUp = newLevel > oldLevel

        prefs.edit()
            .putInt(KEY_XP, newXp)
            .putStringSet(KEY_BADGES, currentUnlocked)
            .apply()

        val reward = XpRewardEvent(
            amount = earned,
            reason = "+5 XP: Action Item Completed",
            didLevelUp = didLevelUp,
            newLevel = newLevel,
            newBadge = newlyUnlocked
        )

        _state.value = calculateState(newXp, oldState.currentStreak, oldState.bestStreak, oldState.totalTasksCompleted, currentUnlocked, reward)
        return reward
    }

    fun clearRewardEvent() {
        _state.value = _state.value.copy(latestRewardEvent = null)
    }

    private fun updateStreak(): Pair<Int, Int> {
        val now = System.currentTimeMillis()
        val todayEpochDay = TimeUnit.MILLISECONDS.toDays(now)
        val lastDay = prefs.getLong(KEY_LAST_ACTIVE_DAY, 0L)
        var streak = prefs.getInt(KEY_STREAK, 0)
        var bestStreak = prefs.getInt(KEY_BEST_STREAK, 0)

        if (lastDay == todayEpochDay) {
            // Already counted today
            return Pair(streak.coerceAtLeast(1), bestStreak.coerceAtLeast(streak))
        }

        if (lastDay == todayEpochDay - 1) {
            // Consecutive day
            streak += 1
        } else if (lastDay == 0L) {
            streak = 1
        } else {
            // Missed a day
            streak = 1
        }

        if (streak > bestStreak) {
            bestStreak = streak
        }

        prefs.edit()
            .putLong(KEY_LAST_ACTIVE_DAY, todayEpochDay)
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_BEST_STREAK, bestStreak)
            .apply()

        return Pair(streak, bestStreak)
    }

    companion object {
        private const val KEY_XP = "key_xp"
        private const val KEY_STREAK = "key_streak"
        private const val KEY_BEST_STREAK = "key_best_streak"
        private const val KEY_TOTAL_COMPLETED = "key_total_completed"
        private const val KEY_LAST_ACTIVE_DAY = "key_last_active_day"
        private const val KEY_BADGES = "key_unlocked_badges"

        const val BADGE_FIRST_STEP = "badge_first_step"
        const val BADGE_STREAK_3 = "badge_streak_3"
        const val BADGE_STREAK_7 = "badge_streak_7"
        const val BADGE_URGENT_SLAYER = "badge_urgent_slayer"
        const val BADGE_SUBTASK_MASTER = "badge_subtask_master"
        const val BADGE_ROUTINE_MASTER = "badge_routine_master"
        const val BADGE_CENTURION = "badge_centurion"

        fun getAllBadgesList(unlockedIds: Set<String>): List<Badge> {
            return listOf(
                Badge(
                    id = BADGE_FIRST_STEP,
                    name = "Initial Execution",
                    description = "Delivered initial project commitment",
                    iconKey = "check",
                    isUnlocked = unlockedIds.contains(BADGE_FIRST_STEP)
                ),
                Badge(
                    id = BADGE_STREAK_3,
                    name = "Velocity Builder",
                    description = "Maintained 3-day active consistency streak",
                    iconKey = "trending_up",
                    isUnlocked = unlockedIds.contains(BADGE_STREAK_3)
                ),
                Badge(
                    id = BADGE_STREAK_7,
                    name = "Operational Cadence",
                    description = "Achieved 7 consecutive days of execution",
                    iconKey = "speed",
                    isUnlocked = unlockedIds.contains(BADGE_STREAK_7)
                ),
                Badge(
                    id = BADGE_URGENT_SLAYER,
                    name = "Critical Delivery",
                    description = "Successfully executed an Urgent priority deadline",
                    iconKey = "priority_high",
                    isUnlocked = unlockedIds.contains(BADGE_URGENT_SLAYER)
                ),
                Badge(
                    id = BADGE_ROUTINE_MASTER,
                    name = "Cadence Architect",
                    description = "Automated and completed recurring workflows",
                    iconKey = "repeat",
                    isUnlocked = unlockedIds.contains(BADGE_ROUTINE_MASTER)
                ),
                Badge(
                    id = BADGE_SUBTASK_MASTER,
                    name = "Precision Execution",
                    description = "Deconstructed and completed detailed action items",
                    iconKey = "checklist",
                    isUnlocked = unlockedIds.contains(BADGE_SUBTASK_MASTER)
                ),
                Badge(
                    id = BADGE_CENTURION,
                    name = "Master Milestone",
                    description = "Surpassed 500+ verified performance points",
                    iconKey = "military_tech",
                    isUnlocked = unlockedIds.contains(BADGE_CENTURION)
                )
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
