package com.example.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

data class Memory(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val date: String = "", // Format: "YYYY-MM-DD"
    val category: String = "Everyday",
    val customCategory: String? = null,
    val description: String = "",
    val imageUri: String? = null,
    val cutoutImageUri: String? = null,
    val location: String? = null,
    val people: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val reminderEnabled: Boolean = true,
    val yearlyAnniversary: Boolean = true,
    val reminderTime: String = "09:00", // "HH:mm"
    val advanceReminderDays: Int = 0, // 0 = on day, 1 = 1 day before, 7 = 1 week before
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    fun parseLocalDate(): LocalDate? {
        return try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Calculates the anniversary date for a given year.
     * Special handling: If the original memory was on Feb 29 and target year is not leap,
     * it falls back to Feb 28 according to our documented anniversary policy.
     */
    fun anniversaryInYear(targetYear: Int): LocalDate? {
        val original = parseLocalDate() ?: return null
        val isOriginalFeb29 = original.monthValue == 2 && original.dayOfMonth == 29
        val isTargetLeap = java.time.Year.of(targetYear).isLeap

        return if (isOriginalFeb29 && !isTargetLeap) {
            LocalDate.of(targetYear, 2, 28)
        } else {
            try {
                LocalDate.of(targetYear, original.monthValue, original.dayOfMonth)
            } catch (e: Exception) {
                LocalDate.of(targetYear, original.monthValue, 28)
            }
        }
    }

    /**
     * Checks if this memory's anniversary falls on the given date (month and day match).
     */
    fun isAnniversaryOn(targetDate: LocalDate): Boolean {
        val original = parseLocalDate() ?: return false
        val anniversary = anniversaryInYear(targetDate.year) ?: return false
        return anniversary == targetDate
    }

    /**
     * Number of years since the memory was originally made (relative to a given date).
     */
    fun yearsSince(targetDate: LocalDate = LocalDate.now()): Int {
        val original = parseLocalDate() ?: return 0
        return (targetDate.year - original.year).coerceAtLeast(0)
    }

    /**
     * Returns the next upcoming anniversary date from today.
     */
    fun nextAnniversary(fromDate: LocalDate = LocalDate.now()): LocalDate? {
        val original = parseLocalDate() ?: return null
        val thisYearAnniversary = anniversaryInYear(fromDate.year) ?: return null
        return if (!thisYearAnniversary.isBefore(fromDate)) {
            thisYearAnniversary
        } else {
            anniversaryInYear(fromDate.year + 1)
        }
    }

    fun formattedDate(): String {
        val parsed = parseLocalDate() ?: return date
        return parsed.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
}
