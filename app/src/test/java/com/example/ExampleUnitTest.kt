package com.example

import com.example.data.model.Memory
import com.example.data.receiver.ReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun memory_dateParsing_isCorrect() {
        val memory = Memory(date = "2022-10-15")
        val parsed = memory.parseLocalDate()
        assertNotNull(parsed)
        assertEquals(2022, parsed?.year)
        assertEquals(10, parsed?.monthValue)
        assertEquals(15, parsed?.dayOfMonth)
    }

    @Test
    fun anniversary_regularYears_isCorrect() {
        val memory = Memory(date = "2020-03-03")
        val anniversary2025 = memory.anniversaryInYear(2025)
        assertEquals(LocalDate.of(2025, 3, 3), anniversary2025)

        val years = memory.yearsSince(LocalDate.of(2025, 3, 3))
        assertEquals(5, years)
    }

    @Test
    fun anniversary_leapYearPolicy_february28Fallback() {
        // Leap day memory on Feb 29, 2024
        val leapMemory = Memory(date = "2024-02-29")

        // 2025 is not a leap year -> policy requires Feb 28
        val nonLeapAnniversary = leapMemory.anniversaryInYear(2025)
        assertEquals(LocalDate.of(2025, 2, 28), nonLeapAnniversary)

        // 2028 is a leap year -> remains Feb 29
        val leapAnniversary = leapMemory.anniversaryInYear(2028)
        assertEquals(LocalDate.of(2028, 2, 29), leapAnniversary)

        // Verify anniversary check on Feb 28, 2025 returns true
        assertTrue(leapMemory.isAnniversaryOn(LocalDate.of(2025, 2, 28)))
        assertFalse(leapMemory.isAnniversaryOn(LocalDate.of(2025, 3, 1)))
    }

    @Test
    fun anniversary_nextAnniversaryCalculation() {
        val memory = Memory(date = "2018-05-12")
        val fromDate = LocalDate.of(2025, 6, 1) // after May 12 in 2025
        val next = memory.nextAnniversary(fromDate)
        assertEquals(LocalDate.of(2026, 5, 12), next)
    }

    @Test
    fun reminderScheduler_triggerCalculation_returnsValidTimestamp() {
        val memory = Memory(
            date = "2022-10-15",
            reminderTime = "09:00",
            yearlyAnniversary = true,
            advanceReminderDays = 0
        )
        val trigger = ReminderScheduler.calculateNextTriggerMillis(memory)
        assertNotNull(trigger)
        assertTrue(trigger!! > 0L)
    }
}
