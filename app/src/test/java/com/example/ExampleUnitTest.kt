package com.example

import com.example.data.RecurrenceType
import com.example.data.Subtask
import com.example.data.TaskCategory
import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.ui.theme.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

  @Test
  fun testThemeModes_atLeastThreeThemesAvailable() {
    val themes = AppThemeMode.entries
    assertTrue("Should provide at least three distinct visual themes", themes.size >= 3)
    assertTrue(themes.any { it == AppThemeMode.LIGHT })
    assertTrue(themes.any { it == AppThemeMode.DARK })
    assertTrue(themes.any { it == AppThemeMode.CLASSIC })
  }

  @Test
  fun testTaskPriority_levelsAndFromString() {
    assertEquals(4, TaskPriority.URGENT.level)
    assertEquals(3, TaskPriority.HIGH.level)
    assertEquals(2, TaskPriority.MEDIUM.level)
    assertEquals(1, TaskPriority.LOW.level)

    assertEquals(TaskPriority.URGENT, TaskPriority.fromString("URGENT"))
    assertEquals(TaskPriority.HIGH, TaskPriority.fromString("HIGH"))
    assertEquals(TaskPriority.MEDIUM, TaskPriority.fromString("unknown"))
  }

  @Test
  fun testRecurringTask_dailyNextOccurrence() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.OCTOBER, 10, 0, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val initialDate = cal.timeInMillis

    val task = TaskItem(
      id = 42L,
      title = "Review Lecture Notes",
      category = TaskCategory.STUDY.name,
      priority = TaskPriority.HIGH.name,
      dueDateMillis = initialDate,
      recurrence = RecurrenceType.DAILY.name,
      isCompleted = true,
      subtasksRaw = TaskItem.encodeSubtasks(
        listOf(Subtask(title = "Chapter 4", isDone = true))
      )
    )

    val next = task.computeNextOccurrence()
    assertNotNull(next)
    assertEquals(0L, next!!.id)
    assertFalse(next.isCompleted)
    assertEquals(1, next.subtasks.size)
    assertFalse(next.subtasks[0].isDone) // Resets subtasks for next occurrence

    val expectedCal = Calendar.getInstance().apply {
      timeInMillis = initialDate
      add(Calendar.DAY_OF_YEAR, 1)
    }
    assertEquals(expectedCal.timeInMillis, next.dueDateMillis)
  }

  @Test
  fun testRecurringTask_weeklyNextOccurrence() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.OCTOBER, 10, 0, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val initialDate = cal.timeInMillis

    val task = TaskItem(
      id = 10L,
      title = "Weekly Team Sync",
      category = TaskCategory.WORK.name,
      priority = TaskPriority.MEDIUM.name,
      dueDateMillis = initialDate,
      recurrence = RecurrenceType.WEEKLY.name
    )

    val next = task.computeNextOccurrence()
    assertNotNull(next)
    val expectedCal = Calendar.getInstance().apply {
      timeInMillis = initialDate
      add(Calendar.DAY_OF_YEAR, 7)
    }
    assertEquals(expectedCal.timeInMillis, next!!.dueDateMillis)
  }

  @Test
  fun testRecurringTask_monthlyNextOccurrence() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.JANUARY, 15, 0, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val initialDate = cal.timeInMillis

    val task = TaskItem(
      id = 11L,
      title = "Monthly Tuition Payment",
      dueDateMillis = initialDate,
      recurrence = RecurrenceType.MONTHLY.name
    )

    val next = task.computeNextOccurrence()
    assertNotNull(next)
    val expectedCal = Calendar.getInstance().apply {
      timeInMillis = initialDate
      add(Calendar.MONTH, 1)
    }
    assertEquals(expectedCal.timeInMillis, next!!.dueDateMillis)
  }

  @Test
  fun testRecurringTask_customIntervalDays() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.MAY, 1, 0, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val initialDate = cal.timeInMillis

    val task = TaskItem(
      id = 12L,
      title = "Water Plants",
      dueDateMillis = initialDate,
      recurrence = RecurrenceType.CUSTOM.name,
      recurrenceIntervalDays = 3,
      recurrenceIntervalUnit = "DAYS"
    )

    val next = task.computeNextOccurrence()
    assertNotNull(next)
    val expectedCal = Calendar.getInstance().apply {
      timeInMillis = initialDate
      add(Calendar.DAY_OF_YEAR, 3)
    }
    assertEquals(expectedCal.timeInMillis, next!!.dueDateMillis)
  }

  @Test
  fun testNonRecurringTask_returnsNullNextOccurrence() {
    val task = TaskItem(
      id = 15L,
      title = "One-time exam",
      dueDateMillis = System.currentTimeMillis(),
      recurrence = RecurrenceType.NONE.name
    )
    assertNull(task.computeNextOccurrence())
  }
}

